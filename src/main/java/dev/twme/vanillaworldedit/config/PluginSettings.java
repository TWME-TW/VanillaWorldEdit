package dev.twme.vanillaworldedit.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.Locale;

public record PluginSettings(long maxVolume, DestroySettings destroy) {

    public static PluginSettings from(FileConfiguration config) {
        long maxVolume = Math.max(1, config.getLong("max-volume", 16_777_216));
        DestroyMode mode;
        try {
            mode = DestroyMode.valueOf(config.getString("destroy.mode", "throttled").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("destroy.mode must be 'fawe' or 'throttled'", exception);
        }
        DestroySettings destroy = new DestroySettings(
                mode,
                Math.max(1, config.getInt("destroy.blocks-per-tick", 128)),
                Math.max(1, config.getInt("destroy.max-millis-per-tick", 4)),
                Math.max(1, config.getInt("destroy.max-queued-jobs", 8))
        );
        return new PluginSettings(maxVolume, destroy);
    }

    public enum DestroyMode {
        FAWE,
        THROTTLED
    }

    public record DestroySettings(
            DestroyMode mode,
            int blocksPerTick,
            int maxMillisPerTick,
            int maxQueuedJobs
    ) {
    }
}
