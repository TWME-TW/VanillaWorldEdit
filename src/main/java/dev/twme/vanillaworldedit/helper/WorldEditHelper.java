package dev.twme.vanillaworldedit.helper;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.factory.MaskFactory;
import com.sk89q.worldedit.extension.factory.PatternFactory;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.MaskIntersection;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BaseBlock;
import org.bukkit.entity.Player;

import java.util.Set;

public class WorldEditHelper {
    public static Pattern getPattern(String blockTypeString, Player player) {
        Pattern blockPattern;
        try {
            ParserContext context = new ParserContext();
            context.setActor(BukkitAdapter.adapt(player));
            context.setWorld(BukkitAdapter.adapt(player.getWorld()));
            PatternFactory patternFactory = WorldEdit.getInstance().getPatternFactory();
            blockPattern = patternFactory.parseFromInput(blockTypeString, context);
        } catch (Exception e) {
            return null;
        }
        return blockPattern;
    }

    public static void setBlock(Player player, Pattern blockPattern, LocalSession localSession, Vector3 vector3) {
        try (var editSession = localSession.createEditSession(BukkitAdapter.adapt(player))) {
            BaseBlock baseBlock = blockPattern.applyBlock(vector3.toBlockPoint());

            editSession.smartSetBlock(vector3.toBlockPoint(), baseBlock);
            editSession.commit();

            localSession.remember(editSession);
        }
    }

    public static EditSession setBlocks(Player player, Pattern blockPattern, LocalSession localSession, Region region) {
        try (var editSession = localSession.createEditSession(BukkitAdapter.adapt(player))) {

            editSession.setBlocks(region, blockPattern);
            editSession.commit();

            localSession.remember(editSession);
            return editSession;
        }
    }

    public static EditSession replaceBlock(Player player, Pattern blockPattern, Mask mask, LocalSession localSession, Region region) {
        try (var editSession = localSession.createEditSession(BukkitAdapter.adapt(player))) {

            editSession.replaceBlocks(region, mask, blockPattern);
            editSession.commit();
            return editSession;
        }
    }

    public static Mask getMask(String maskString, Player player) {
        Mask mask;
        try {
            ParserContext context = new ParserContext();
            context.setActor(BukkitAdapter.adapt(player));
            context.setWorld(BukkitAdapter.adapt(player.getWorld()));
            MaskFactory maskFactory = WorldEdit.getInstance().getMaskFactory();
            mask = maskFactory.parseFromInput(maskString, context);
        } catch (Exception e) {
            return null;
        }
        return mask;
    }
}
