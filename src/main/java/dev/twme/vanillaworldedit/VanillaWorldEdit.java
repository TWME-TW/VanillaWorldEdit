package dev.twme.vanillaworldedit;

import dev.twme.vanillaworldedit.adapter.BukkitListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class VanillaWorldEdit extends JavaPlugin {

    private static VanillaWorldEdit instance;

    @Override
    public void onEnable() {
        instance = this;
        this.getServer().getPluginManager().registerEvents(new BukkitListener(), this);
    }

    @Override
    public void onDisable() {
    }

    public static VanillaWorldEdit getInstance() {
        return instance;
    }
}
