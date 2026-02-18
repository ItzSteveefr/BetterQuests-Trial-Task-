package dev.itzstevee.quests.data;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

public class PlayerDataCache {

    private final MongoService mongoService;
    private final AsyncLoadingCache<UUID, PlayerQuestData> cache;
    private final Logger logger;

    public PlayerDataCache(MongoService mongoService, Logger logger) {
        this.mongoService = mongoService;
        this.logger = logger;
        this.cache = Caffeine.newBuilder()
                .maximumSize(500)
                .buildAsync((uuid, executor) -> mongoService.loadPlayer(uuid));
    }

    public CompletableFuture<PlayerQuestData> get(UUID uuid) {
        return cache.get(uuid);
    }

    public PlayerQuestData getIfPresent(UUID uuid) {
        CompletableFuture<PlayerQuestData> future = cache.getIfPresent(uuid);
        if (future == null) {
            return null;
        }
        return future.getNow(null);
    }

    public void put(UUID uuid, PlayerQuestData data) {
        cache.put(uuid, CompletableFuture.completedFuture(data));
    }

    public void invalidate(UUID uuid) {
        cache.synchronous().invalidate(uuid);
    }

    public void saveAll() {
        ConcurrentMap<UUID, CompletableFuture<PlayerQuestData>> map = cache.asMap();
        for (Map.Entry<UUID, CompletableFuture<PlayerQuestData>> entry : map.entrySet()) {
            CompletableFuture<PlayerQuestData> future = entry.getValue();
            if (future.isDone() && !future.isCompletedExceptionally()) {
                PlayerQuestData data = future.getNow(null);
                if (data != null) {
                    mongoService.savePlayer(data).exceptionally(ex -> {
                        logger.warning("Failed to save player data for " + entry.getKey() + ": " + ex.getMessage());
                        return null;
                    });
                }
            }
        }
    }

    public void saveAllSync() {
        ConcurrentMap<UUID, CompletableFuture<PlayerQuestData>> map = cache.asMap();
        for (Map.Entry<UUID, CompletableFuture<PlayerQuestData>> entry : map.entrySet()) {
            CompletableFuture<PlayerQuestData> future = entry.getValue();
            if (future.isDone() && !future.isCompletedExceptionally()) {
                PlayerQuestData data = future.getNow(null);
                if (data != null) {
                    try {
                        mongoService.savePlayer(data).join();
                    } catch (Exception e) {
                        logger.warning("Failed to save player data sync for " + entry.getKey() + ": " + e.getMessage());
                    }
                }
            }
        }
    }
}
