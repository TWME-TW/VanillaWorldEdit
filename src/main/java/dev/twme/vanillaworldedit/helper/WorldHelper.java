package dev.twme.vanillaworldedit.helper;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;

import java.util.ArrayList;
import java.util.List;

public class WorldHelper {

    public static void selectPositions(Player player, BlockVector blockVector, Position position) {
        var bukkitPlayer = BukkitAdapter.adapt(player);
        var session = bukkitPlayer.getSession();

        var world = bukkitPlayer.getWorld();
        var blockVector3 = BlockVector3.at(blockVector.getX(), blockVector.getY(), blockVector.getZ());

        position.selectPosition(bukkitPlayer, session, world, blockVector3);
    }

    public static List<Block> getBlocksBetween(World world, BlockVector v1, BlockVector v2) {
        var blocks = new ArrayList<Block>();

        var x1 = Math.min(v1.getBlockX(), v2.getBlockX());
        var y1 = Math.min(v1.getBlockY(), v2.getBlockY());
        var z1 = Math.min(v1.getBlockZ(), v2.getBlockZ());

        var x2 = Math.max(v1.getBlockX(), v2.getBlockX());
        var y2 = Math.max(v1.getBlockY(), v2.getBlockY());
        var z2 = Math.max(v1.getBlockZ(), v2.getBlockZ());

        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    blocks.add(world.getBlockAt(x, y, z));
                }
            }
        }
        return blocks;
    }
}
