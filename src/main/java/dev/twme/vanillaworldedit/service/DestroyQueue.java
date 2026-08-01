package dev.twme.vanillaworldedit.service;

import dev.twme.vanillaworldedit.VanillaWorldEdit;
import dev.twme.vanillaworldedit.command.EditRequest;
import dev.twme.vanillaworldedit.config.PluginSettings.DestroySettings;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class DestroyQueue {

    private final DestroySettings settings;
    private final WorldEditExecutor worldEditExecutor;
    private final Deque<Job> jobs = new ArrayDeque<>();
    private final BukkitTask task;

    public DestroyQueue(
            VanillaWorldEdit plugin,
            DestroySettings settings,
            WorldEditExecutor worldEditExecutor
    ) {
        this.settings = settings;
        this.worldEditExecutor = worldEditExecutor;
        this.task = settings.mode() == dev.twme.vanillaworldedit.config.PluginSettings.DestroyMode.THROTTLED
                ? Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L)
                : null;
    }

    public void submit(Player player, EditRequest request) {
        worldEditExecutor.validateVolume(request);
        if (jobs.size() >= settings.maxQueuedJobs()) {
            throw new IllegalArgumentException("destroy queue is full; try again later");
        }
        jobs.addLast(new Job(
                player.getUniqueId(),
                player.getWorld().getUID(),
                request,
                Bukkit.createBlockData(request.block()),
                new CuboidCursor(request.min(), request.max())
        ));
        player.sendMessage("VanillaWorldEdit: destroy queued (" + request.volume() + " blocks)");
    }

    public void shutdown() {
        if (task != null) {
            task.cancel();
        }
        jobs.clear();
    }

    private void tick() {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(settings.maxMillisPerTick());
        int remaining = settings.blocksPerTick();

        while (remaining > 0 && System.nanoTime() < deadline && !jobs.isEmpty()) {
            Job job = jobs.peekFirst();
            World world = Bukkit.getWorld(job.worldId());
            if (world == null) {
                jobs.removeFirst();
                continue;
            }

            CuboidCursor cursor = job.cursor();
            var block = world.getBlockAt(cursor.x(), cursor.y(), cursor.z());
            block.breakNaturally(true, true);
            block.setBlockData(job.target(), false);
            cursor.advance();
            remaining--;

            if (!cursor.hasNext()) {
                jobs.removeFirst();
                finish(job);
            }
        }
    }

    private void finish(Job job) {
        Player player = Bukkit.getPlayer(job.playerId());
        if (player == null) {
            return;
        }
        player.sendMessage("VanillaWorldEdit: destroy finished (" + job.request().volume() + " processed)");
    }

    private record Job(
            UUID playerId,
            UUID worldId,
            EditRequest request,
            BlockData target,
            CuboidCursor cursor
    ) {
    }
}
