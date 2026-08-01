package dev.twme.vanillaworldedit.service;

import com.sk89q.worldedit.function.pattern.AbstractPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.util.Objects;

final class ShellPattern extends AbstractPattern {

    private final BlockVector3 min;
    private final BlockVector3 max;
    private final Pattern shell;
    private final BaseBlock air;

    ShellPattern(BlockVector3 min, BlockVector3 max, Pattern shell) {
        this.min = min;
        this.max = max;
        this.shell = shell;
        this.air = Objects.requireNonNull(BlockTypes.AIR).getDefaultState().toBaseBlock();
    }

    @Override
    public BaseBlock applyBlock(BlockVector3 position) {
        if (position.x() == min.x() || position.x() == max.x()
                || position.y() == min.y() || position.y() == max.y()
                || position.z() == min.z() || position.z() == max.z()) {
            return shell.applyBlock(position);
        }
        return air;
    }

    @Override
    public Pattern fork() {
        return new ShellPattern(min, max, shell.fork());
    }
}
