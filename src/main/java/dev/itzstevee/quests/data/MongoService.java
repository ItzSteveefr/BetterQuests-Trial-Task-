package dev.itzstevee.quests.data;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.IndexOptions;
import dev.itzstevee.quests.config.ConfigManager;
import org.bson.Document;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class MongoService {

    private final ConfigManager configManager;
    private final Logger logger;
    private MongoClient mongoClient;
    private MongoCollection<Document> playerDataCollection;

    public MongoService(ConfigManager configManager, Logger logger) {
        this.configManager = configManager;
        this.logger = logger;
    }

    public boolean connect() {
        try {
            ConnectionString connectionString = new ConnectionString(configManager.getMongoUri());
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .build();

            mongoClient = MongoClients.create(settings);
            MongoDatabase database = mongoClient.getDatabase(configManager.getMongoDatabase());
            playerDataCollection = database.getCollection("player_data");

            CountDownLatch latch = new CountDownLatch(1);
            AtomicBoolean success = new AtomicBoolean(false);

            database.listCollectionNames().subscribe(new Subscriber<String>() {
                @Override
                public void onSubscribe(Subscription s) {
                    s.request(1);
                }

                @Override
                public void onNext(String s) {
                    success.set(true);
                }

                @Override
                public void onError(Throwable t) {
                    logger.severe("MongoDB connection failed: " + t.getMessage());
                    latch.countDown();
                }

                @Override
                public void onComplete() {
                    success.set(true);
                    latch.countDown();
                }
            });

            boolean completed = latch.await(configManager.getConnectionTimeoutMs(), TimeUnit.MILLISECONDS);
            if (!completed || !success.get()) {
                logger.severe("MongoDB connection timed out or failed.");
                return false;
            }

            toFuture(playerDataCollection.createIndex(new Document("playerName", 1)));

            logger.info("MongoDB connected successfully.");
            return true;
        } catch (Exception e) {
            logger.severe("MongoDB connection error: " + e.getMessage());
            return false;
        }
    }

    public void disconnect() {
        if (mongoClient != null) {
            mongoClient.close();
            logger.info("MongoDB disconnected.");
        }
    }

    public CompletableFuture<PlayerQuestData> loadPlayer(UUID uuid) {
        return toFuture(playerDataCollection.find(Filters.eq("_id", uuid.toString())).first())
                .thenApply(doc -> {
                    if (doc == null) {
                        return new PlayerQuestData(uuid, "", new HashMap<>(), new HashSet<>(), new HashSet<>());
                    }
                    return fromDocument(doc);
                });
    }

    public CompletableFuture<Void> savePlayer(PlayerQuestData data) {
        Document doc = toDocument(data);
        ReplaceOptions options = new ReplaceOptions().upsert(true);
        return toFuture(playerDataCollection.replaceOne(Filters.eq("_id", data.getUuid().toString()), doc, options))
                .thenApply(result -> null);
    }

    public CompletableFuture<Void> setProgress(UUID uuid, String questId, int value) {
        return toFuture(playerDataCollection.updateOne(
                Filters.eq("_id", uuid.toString()),
                Updates.combine(
                        Updates.set("progress." + questId, value),
                        Updates.set("lastUpdated", new Date())
                )
        )).thenApply(result -> null);
    }

    public CompletableFuture<Void> resetQuest(UUID uuid, String questId) {
        return toFuture(playerDataCollection.updateOne(
                Filters.eq("_id", uuid.toString()),
                Updates.combine(
                        Updates.unset("progress." + questId),
                        Updates.pull("completedQuests", questId),
                        Updates.pull("activeQuests", questId),
                        Updates.set("lastUpdated", new Date())
                )
        )).thenApply(result -> null);
    }

    public MongoCollection<Document> getPlayerDataCollection() {
        return playerDataCollection;
    }

    private Document toDocument(PlayerQuestData data) {
        Map<String, Object> progressMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : data.getProgress().entrySet()) {
            progressMap.put(entry.getKey(), entry.getValue());
        }
        return new Document("_id", data.getUuid().toString())
                .append("playerName", data.getPlayerName())
                .append("progress", new Document(progressMap))
                .append("completedQuests", new ArrayList<>(data.getCompletedQuests()))
                .append("activeQuests", new ArrayList<>(data.getActiveQuests()))
                .append("lastUpdated", new Date());
    }

    @SuppressWarnings("unchecked")
    private PlayerQuestData fromDocument(Document doc) {
        UUID uuid = UUID.fromString(doc.getString("_id"));
        String playerName = doc.getString("playerName");

        Map<String, Integer> progress = new HashMap<>();
        Document progressDoc = doc.get("progress", Document.class);
        if (progressDoc != null) {
            for (Map.Entry<String, Object> entry : progressDoc.entrySet()) {
                if (entry.getValue() instanceof Number number) {
                    progress.put(entry.getKey(), number.intValue());
                }
            }
        }

        HashSet<String> completedQuests = new HashSet<>();
        java.util.List<String> completedList = doc.getList("completedQuests", String.class);
        if (completedList != null) {
            completedQuests.addAll(completedList);
        }

        HashSet<String> activeQuests = new HashSet<>();
        java.util.List<String> activeList = doc.getList("activeQuests", String.class);
        if (activeList != null) {
            activeQuests.addAll(activeList);
        }

        return new PlayerQuestData(uuid, playerName, progress, completedQuests, activeQuests);
    }

    public static <T> CompletableFuture<T> toFuture(Publisher<T> publisher) {
        CompletableFuture<T> future = new CompletableFuture<>();
        publisher.subscribe(new Subscriber<T>() {
            private T result;

            @Override
            public void onSubscribe(Subscription s) {
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(T item) {
                result = item;
            }

            @Override
            public void onError(Throwable t) {
                future.completeExceptionally(t);
            }

            @Override
            public void onComplete() {
                future.complete(result);
            }
        });
        return future;
    }
}
