package dev.twme.vanillaworldedit;

import dev.twme.vanillaworldedit.adapter.BukkitListener;
import dev.twme.vanillaworldedit.config.PluginSettings;
import dev.twme.vanillaworldedit.service.DestroyQueue;
import dev.twme.vanillaworldedit.service.WorldEditExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public final class VanillaWorldEdit extends JavaPlugin {

    private DestroyQueue destroyQueue;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        PluginSettings settings = PluginSettings.from(getConfig());
        WorldEditExecutor worldEditExecutor = new WorldEditExecutor(settings.maxVolume());
        destroyQueue = new DestroyQueue(this, settings.destroy(), worldEditExecutor);
        getServer().getPluginManager().registerEvents(
                new BukkitListener(settings, worldEditExecutor, destroyQueue), this
        );
    }

    @Override
    public void onDisable() {
        if (destroyQueue != null) {
            destroyQueue.shutdown();
        }
    }
}
