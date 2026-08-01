package dev.twme.vanillaworldedit.command;

import com.sk89q.worldedit.math.BlockVector3;

public record EditRequest(
        BlockVector3 first,
        BlockVector3 second,
        String block,
        EditMode mode,
        String replaceFilter
) {
    public BlockVector3 min() {
        return BlockVector3.at(
                Math.min(first.x(), second.x()),
                Math.min(first.y(), second.y()),
                Math.min(first.z(), second.z())
        );
    }

    public BlockVector3 max() {
        return BlockVector3.at(
                Math.max(first.x(), second.x()),
                Math.max(first.y(), second.y()),
                Math.max(first.z(), second.z())
        );
    }

    public long volume() {
        BlockVector3 min = min();
        BlockVector3 max = max();
        return Math.multiplyExact(
                Math.multiplyExact((long) max.x() - min.x() + 1, (long) max.y() - min.y() + 1),
                (long) max.z() - min.z() + 1
        );
    }
}
