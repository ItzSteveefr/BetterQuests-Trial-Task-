package dev.itzstevee.quests.listener;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BlockPlaceListener implements Listener {

    private final Set<Long> playerPlacedBlocks = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        playerPlacedBlocks.add(locationToLong(event.getBlock().getLocation()));
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        int chunkX = event.getChunk().getX();
        int chunkZ = event.getChunk().getZ();
        playerPlacedBlocks.removeIf(key -> {
            int blockX = (int) ((key >> 38) & 0x3FFFFFF);
            if ((blockX & 0x2000000) != 0) blockX |= ~0x3FFFFFF;
            int blockZ = (int) (key & 0x3FFFFFF);
            if ((blockZ & 0x2000000) != 0) blockZ |= ~0x3FFFFFF;
            return (blockX >> 4) == chunkX && (blockZ >> 4) == chunkZ;
        });
    }

    public boolean isPlayerPlaced(Location location) {
        return playerPlacedBlocks.remove(locationToLong(location));
    }

    private long locationToLong(Location loc) {
        return ((long) (loc.getBlockX() & 0x3FFFFFF) << 38)
                | ((long) (loc.getBlockY() & 0xFFF) << 26)
                | (long) (loc.getBlockZ() & 0x3FFFFFF);
    }

    public void clear() {
        playerPlacedBlocks.clear();
    }
}
