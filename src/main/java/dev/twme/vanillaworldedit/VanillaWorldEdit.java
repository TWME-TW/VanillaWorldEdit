package dev.twme.vanillaworldedit;

import dev.twme.vanillaworldedit.adapter.BukkitListener;
import dev.twme.vanillaworldedit.proxy.PluginMessageUtil;
import dev.twme.vanillaworldedit.task.FinishCommandNoticeTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public final class VanillaWorldEdit extends JavaPlugin {

    private static VanillaWorldEdit instance;

    @Override
    public void onEnable() {
        instance = this;
        this.getServer().getPluginManager().registerEvents(new BukkitListener(), this);
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "vanillaworldeditproxy:main", new PluginMessageUtil());
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new FinishCommandNoticeTask(), 10L, 100L);
    }

    @Override
    public void onDisable() {
    }

    public static VanillaWorldEdit getInstance() {
        return instance;
    }
}
