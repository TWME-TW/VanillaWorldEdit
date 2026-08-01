package dev.twme.vanillaworldedit.adapter;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import dev.twme.vanillaworldedit.command.EditMode;
import dev.twme.vanillaworldedit.command.EditRequest;
import dev.twme.vanillaworldedit.command.VanillaCommandParser;
import dev.twme.vanillaworldedit.config.PluginSettings;
import dev.twme.vanillaworldedit.service.DestroyQueue;
import dev.twme.vanillaworldedit.service.WorldEditExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Locale;

public final class BukkitListener implements Listener {

    private final PluginSettings settings;
    private final WorldEditExecutor worldEditExecutor;
    private final DestroyQueue destroyQueue;

    public BukkitListener(
            PluginSettings settings,
            WorldEditExecutor worldEditExecutor,
            DestroyQueue destroyQueue
    ) {
        this.settings = settings;
        this.worldEditExecutor = worldEditExecutor;
        this.destroyQueue = destroyQueue;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String commandName = firstToken(event.getMessage()).toLowerCase(Locale.ROOT);
        if (!isHandled(commandName)) {
            return;
        }

        Player player = event.getPlayer();
        String permission = commandName.endsWith("fill") ? "minecraft.command.fill" : "minecraft.command.setblock";
        if (!player.hasPermission(permission)) {
            return;
        }

        event.setCancelled(true);
        try {
            BlockVector3 origin = BlockVector3.at(
                    player.getLocation().getBlockX(),
                    player.getLocation().getBlockY(),
                    player.getLocation().getBlockZ()
            );
            EditRequest request = VanillaCommandParser.parse(event.getMessage(), origin);
            validateWorldHeight(player, request);
            validateFawePermissions(player, request);

            if (request.mode() == EditMode.DESTROY && settings.destroy().mode() == PluginSettings.DestroyMode.THROTTLED) {
                destroyQueue.submit(player, request);
            } else {
                worldEditExecutor.execute(player, request);
            }
        } catch (IllegalArgumentException exception) {
            player.sendMessage("VanillaWorldEdit: " + exception.getMessage());
        } catch (Exception exception) {
            player.sendMessage("VanillaWorldEdit: edit failed; see the server log.");
            player.getServer().getLogger().warning("VanillaWorldEdit edit failed for " + player.getName() + ": " + exception);
        }
    }

    private static String firstToken(String message) {
        int space = message.indexOf(' ');
        return space < 0 ? message : message.substring(0, space);
    }

    private static boolean isHandled(String command) {
        return command.equals("/fill") || command.equals("/minecraft:fill")
                || command.equals("/setblock") || command.equals("/minecraft:setblock");
    }

    private static void validateWorldHeight(Player player, EditRequest request) {
        var world = BukkitAdapter.adapt(player.getWorld());
        if (request.min().y() < world.getMinY()
                || request.max().y() > world.getMaxY()) {
            throw new IllegalArgumentException("selection is outside the world's build height");
        }
    }

    private static void validateFawePermissions(Player player, EditRequest request) {
        String[] permissions = switch (request.mode()) {
            case SET, DESTROY -> new String[]{"worldedit.region.set"};
            case KEEP, REPLACE -> new String[]{"worldedit.region.replace"};
            case OUTLINE -> new String[]{"worldedit.region.faces"};
            case HOLLOW -> new String[]{"worldedit.region.set", "worldedit.region.faces"};
        };
        for (String permission : permissions) {
            if (!player.hasPermission(permission)) {
                throw new IllegalArgumentException("missing permission: " + permission);
            }
        }
    }
}
