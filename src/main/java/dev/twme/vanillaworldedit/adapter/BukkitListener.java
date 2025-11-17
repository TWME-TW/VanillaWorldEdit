package dev.twme.vanillaworldedit.adapter;

import com.fastasyncworldedit.core.math.MutableVector3;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.world.block.BaseBlock;
import dev.twme.vanillaworldedit.VanillaWorldEdit;
import dev.twme.vanillaworldedit.helper.BlockDataHelper;
import dev.twme.vanillaworldedit.helper.Position;
import dev.twme.vanillaworldedit.helper.WorldEditHelper;
import dev.twme.vanillaworldedit.helper.WorldHelper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class BukkitListener implements Listener {
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().toLowerCase();
        if (!message.startsWith("/")) return;

        var arg = message.split(" ");
        var command = arg[0];
        var server = VanillaWorldEdit.getInstance().getServer();
        var player = event.getPlayer();

        LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player));

        if (command.equals("/fill")) {

            if (arg.length < 8) {
                return;
            }
            event.setCancelled(true);

            var blockVector = player.getLocation().toVector().toBlockVector();
            var v1 = BlockDataHelper.parseCoordinateString(arg[1] + " " + arg[2] + " " + arg[3], blockVector);
            var v2 = BlockDataHelper.parseCoordinateString(arg[4] + " " + arg[5] + " " + arg[6], blockVector);

            WorldHelper.selectPositions(player, v1, Position.PRIMARY);
            WorldHelper.selectPositions(player, v2, Position.SECONDARY);

            var blockData = BlockDataHelper.convertBlockData(arg[7]);

            Pattern pattern = WorldEditHelper.getPattern(blockData, player);

            if (pattern == null) {
                return;
            }

            switch (arg.length >= 9 ? arg[8] : "none") {
                case "destroy" -> {
                    WorldHelper.getBlocksBetween(player.getWorld(), v1, v2).forEach(block -> block.breakNaturally(true, true));
                    server.dispatchCommand(player, "/set %s".formatted(blockData));
                }
                case "hollow" -> {
                    server.dispatchCommand(player, "/set air");
                    server.dispatchCommand(player, "/outline %s".formatted(blockData));
                }
                case "keep" -> server.dispatchCommand(player, "/replace air %s".formatted(blockData));
                case "outline" -> server.dispatchCommand(player, "/outline %s".formatted(blockData));
                case "replace" -> {
                    var commandMessage = arg.length >= 10 ? "/replace %s %s".formatted(BlockDataHelper.convertBlockData(arg[9]), blockData) : "/replace %s".formatted(blockData);
                    server.dispatchCommand(player, commandMessage);
                }
                default -> server.dispatchCommand(player, "/set %s".formatted(blockData));
            }
        } else if (command.equals("/setblock")) {
            event.setCancelled(true);

            var blockVector = player.getLocation().toVector().toBlockVector();
            var coordinate = BlockDataHelper.parseCoordinateString(arg[1] + " " + arg[2] + " " + arg[3], blockVector);
            var blockData = BlockDataHelper.convertBlockData(arg[4]);

            Pattern pattern = WorldEditHelper.getPattern(blockData, player);

            if (pattern == null) {
                return;
            }

            WorldHelper.selectPositions(player, coordinate, Position.PRIMARY);
            WorldHelper.selectPositions(player, coordinate, Position.SECONDARY);

            Vector3 vector3 = new MutableVector3(coordinate.getX(), coordinate.getY(), coordinate.getZ());



            switch (arg.length >= 6 ? arg[5] : "none") {
                case "destroy" -> {
                    // player.getWorld().getBlockAt(coordinate.getBlockX(), coordinate.getBlockY(), coordinate.getBlockZ()).breakNaturally(true, true);
                    try (EditSession editSession = localSession.createEditSession(BukkitAdapter.adapt(player))) {
                        BaseBlock baseBlock = pattern.applyBlock(vector3.toBlockPoint());
                        editSession.smartSetBlock(vector3.toBlockPoint(), baseBlock);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                case "keep" -> server.dispatchCommand(player, "/replace air %s".formatted(blockData));
                case "replace" -> server.dispatchCommand(player, "/replace %s".formatted(blockData));
                default -> server.dispatchCommand(player, "/set %s".formatted(blockData));
            }
        }
    }
}
