package dev.twme.vanillaworldedit.service;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import dev.twme.vanillaworldedit.command.EditMode;
import dev.twme.vanillaworldedit.command.EditRequest;

public final class WorldEditExecutor {

    private final long maxVolume;

    public WorldEditExecutor(long maxVolume) {
        this.maxVolume = maxVolume;
    }

    public void validateVolume(EditRequest request) {
        long volume;
        try {
            volume = request.volume();
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("selection volume overflow", exception);
        }
        if (volume > maxVolume) {
            throw new IllegalArgumentException("selection is " + volume + " blocks; limit is " + maxVolume);
        }
    }

    public int execute(org.bukkit.entity.Player bukkitPlayer, EditRequest request) throws Exception {
        validateVolume(request);

        Player actor = BukkitAdapter.adapt(bukkitPlayer);
        World world = BukkitAdapter.adapt(bukkitPlayer.getWorld());
        LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(actor);
        CuboidRegion region = new CuboidRegion(world, request.first(), request.second());

        try (EditSession editSession = localSession.createEditSession(actor)) {
            ParserContext context = parserContext(actor, world, localSession, editSession);
            Pattern target = WorldEdit.getInstance().getPatternFactory().parseFromInput(request.block(), context);
            int changed = apply(request, region, editSession, context, target);
            localSession.remember(editSession);
            return changed;
        }
    }

    private static int apply(
            EditRequest request,
            CuboidRegion region,
            EditSession editSession,
            ParserContext context,
            Pattern target
    ) throws Exception {
        return switch (request.mode()) {
            case SET, DESTROY -> editSession.setBlocks((Region) region, target);
            case OUTLINE -> editSession.makeCuboidFaces(region, target);
            case HOLLOW -> editSession.setBlocks(
                    (Region) region,
                    new ShellPattern(request.min(), request.max(), target)
            );
            case KEEP -> {
                Mask air = WorldEdit.getInstance().getMaskFactory().parseFromInput("#air", context);
                yield editSession.replaceBlocks(region, air, target);
            }
            case REPLACE -> {
                if (request.replaceFilter() == null) {
                    yield editSession.setBlocks((Region) region, target);
                }
                String filter = normalizeMask(request.replaceFilter());
                Mask mask = WorldEdit.getInstance().getMaskFactory().parseFromInput(filter, context);
                yield editSession.replaceBlocks(region, mask, target);
            }
        };
    }

    private static ParserContext parserContext(
            Player actor,
            World world,
            LocalSession localSession,
            EditSession editSession
    ) {
        ParserContext context = new ParserContext();
        context.setActor(actor);
        context.setWorld(world);
        context.setSession(localSession);
        context.setExtent(editSession);
        context.setRestricted(true);
        return context;
    }

    private static String normalizeMask(String input) {
        return input.replace("#minecraft:", "##");
    }
}
