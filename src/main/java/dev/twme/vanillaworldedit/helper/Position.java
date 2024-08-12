package dev.twme.vanillaworldedit.helper;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.extension.platform.permission.ActorSelectorLimits;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;

public enum Position {

    PRIMARY {
        @Override
        public void selectPosition(BukkitPlayer bukkitPlayer, LocalSession session, World world, BlockVector3 blockVector3) {
            var actor = ActorSelectorLimits.forActor(bukkitPlayer);

            var regionSelector = session.getRegionSelector(world);
            if (!regionSelector.selectPrimary(blockVector3, actor)) return;

            regionSelector.explainPrimarySelection(bukkitPlayer, session, blockVector3);
        }
    },
    SECONDARY {
        @Override
        public void selectPosition(BukkitPlayer bukkitPlayer, LocalSession session, World world, BlockVector3 blockVector3) {
            var actor = ActorSelectorLimits.forActor(bukkitPlayer);

            var regionSelector = session.getRegionSelector(world);
            if (!regionSelector.selectSecondary(blockVector3, actor)) return;

            regionSelector.explainSecondarySelection(bukkitPlayer, session, blockVector3);
        }
    };

    public abstract void selectPosition(BukkitPlayer bukkitPlayer, LocalSession session, World world, BlockVector3 blockVector3);
}
