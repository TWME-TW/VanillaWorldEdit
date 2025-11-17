package dev.twme.vanillaworldedit.proxy;

import com.fastasyncworldedit.core.math.MutableVector3;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import dev.twme.vanillaworldedit.VanillaWorldEdit;
import dev.twme.vanillaworldedit.helper.BlockDataHelper;
import dev.twme.vanillaworldedit.helper.Position;
import dev.twme.vanillaworldedit.helper.WorldEditHelper;
import dev.twme.vanillaworldedit.helper.WorldHelper;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PluginMessageUtil implements PluginMessageListener {

    public static final Map<Player, Long> firstCommandTime = new HashMap<>();
    public static final Map<Player, Long> lastCommandTime = new HashMap<>();
    public static final Map<Player, Long> commandCount = new HashMap<>();

    public static final String IDENTIFIER = "vanillaworldeditproxy:main";
    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        if (!channel.equals(IDENTIFIER)) {
            return;
        }
        String command = new String(message);
        runCommand("/" + command, player);
    }

    private static void runCommand(String message, Player player) {

        var arg = message.split(" ");
        var command = arg[0];
        var server = VanillaWorldEdit.getInstance().getServer();

        LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player));

        if (command.equals("/fill")) {

            if (arg.length < 8) {
                return;
            }

            lastCommandTime.put(player, System.currentTimeMillis());
            commandCount.put(player, commandCount.getOrDefault(player, 0L) + 1);
            if (!firstCommandTime.containsKey(player)) {
                firstCommandTime.put(player, System.currentTimeMillis());
            }

            var blockVector = player.getLocation().toVector().toBlockVector();
            var v1 = BlockDataHelper.parseCoordinateString(arg[1] + " " + arg[2] + " " + arg[3], blockVector);
            var v2 = BlockDataHelper.parseCoordinateString(arg[4] + " " + arg[5] + " " + arg[6], blockVector);

            int minX = Math.min(v1.getBlockX(), v2.getBlockX());
            int minY = Math.min(v1.getBlockY(), v2.getBlockY());
            int minZ = Math.min(v1.getBlockZ(), v2.getBlockZ());
            int maxX = Math.max(v1.getBlockX(), v2.getBlockX());
            int maxY = Math.max(v1.getBlockY(), v2.getBlockY());
            int maxZ = Math.max(v1.getBlockZ(), v2.getBlockZ());

            // WorldHelper.selectPositions(player, v1, Position.PRIMARY);
            // WorldHelper.selectPositions(player, v2, Position.SECONDARY);

            Region region = new CuboidRegion(BukkitAdapter.adapt(player.getWorld()), BlockVector3.at(v1.getX(), v1.getY(), v1.getZ()), BlockVector3.at(v2.getX(), v2.getY(), v2.getZ()));

            var blockData = BlockDataHelper.convertBlockData(arg[7]);

            Pattern pattern = WorldEditHelper.getPattern(blockData, player);

            if (pattern == null) {
                return;
            }

            switch (arg.length >= 9 ? arg[8] : "none") {
                case "destroy" -> {
                    // WorldHelper.getBlocksBetween(player.getWorld(), v1, v2).forEach(block -> block.breakNaturally(true, true));
                    localSession.remember(WorldEditHelper.setBlocks(player, pattern, localSession, region));
                }
                case "hollow" -> {
                    CuboidRegion hollowRegion = new CuboidRegion(BukkitAdapter.adapt(player.getWorld()), BlockVector3.at(minX + 1, minY + 1, minZ + 1), BlockVector3.at(maxX - 1, maxY - 1, maxZ - 1));
                    localSession.remember(WorldEditHelper.setBlocks(player, pattern, localSession, region));
                    localSession.remember(WorldEditHelper.setBlocks(player, pattern, localSession, hollowRegion));
                }
                case "keep" -> {
                    localSession.remember(WorldEditHelper.replaceBlock(player, pattern, WorldEditHelper.getMask("air", player), localSession, region));
                    server.dispatchCommand(player, "/replace air %s".formatted(blockData));
                }
                case "outline" -> server.dispatchCommand(player, "/outline %s".formatted(blockData));
                case "replace" -> {

                    Mask mask = arg.length >= 10 ? WorldEditHelper.getMask(arg[9], player) : WorldEditHelper.getMask("air", player);
                    localSession.remember(WorldEditHelper.replaceBlock(player, pattern, mask, localSession, region));
                    //var commandMessage = arg.length >= 10 ? "/replace %s %s".formatted(BlockDataHelper.convertBlockData(arg[9]), blockData) : "/replace %s".formatted(blockData);
                    //server.dispatchCommand(player, commandMessage);
                }
                default -> server.dispatchCommand(player, "/set %s".formatted(blockData));
            }
        } else if (command.equals("/setblock")) {
            lastCommandTime.put(player, System.currentTimeMillis());
            commandCount.put(player, commandCount.getOrDefault(player, 0L) + 1);
            if (!firstCommandTime.containsKey(player)) {
                firstCommandTime.put(player, System.currentTimeMillis());
            }

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
            BlockVector3 blockVector3 = BlockVector3.at(coordinate.getX(), coordinate.getY(), coordinate.getZ());
            switch (arg.length >= 6 ? arg[5] : "none") {
                case "destroy" -> {
                    // player.getWorld().getBlockAt(coordinate.getBlockX(), coordinate.getBlockY(), coordinate.getBlockZ()).breakNaturally(true, true);
                    WorldEditHelper.setBlock(player, pattern, localSession, vector3);
                }
                case "keep" -> WorldEditHelper.replaceBlock(player, pattern, WorldEditHelper.getMask("air", player), localSession, new CuboidRegion(BukkitAdapter.adapt(player.getWorld()) ,blockVector3, blockVector3));
                case "replace" -> WorldEditHelper.replaceBlock(player, pattern, WorldEditHelper.getMask("air", player), localSession, new CuboidRegion(BukkitAdapter.adapt(player.getWorld()) ,blockVector3, blockVector3));
                default -> WorldEditHelper.setBlock(player, pattern, localSession, vector3);
            }

        }
    }
}
