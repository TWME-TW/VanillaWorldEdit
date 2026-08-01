package dev.twme.vanillaworldedit.service;

import com.sk89q.worldedit.math.BlockVector3;

public final class CuboidCursor {

    private final int minX;
    private final int minY;
    private final int minZ;
    private final int maxX;
    private final int maxY;
    private final int maxZ;
    private int x;
    private int y;
    private int z;
    private boolean exhausted;

    public CuboidCursor(BlockVector3 min, BlockVector3 max) {
        this.minX = min.x();
        this.minY = min.y();
        this.minZ = min.z();
        this.maxX = max.x();
        this.maxY = max.y();
        this.maxZ = max.z();
        this.x = minX;
        this.y = minY;
        this.z = minZ;
        this.exhausted = minX > maxX || minY > maxY || minZ > maxZ;
    }

    public boolean hasNext() {
        return !exhausted;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    public void advance() {
        if (exhausted) {
            return;
        }
        if (y < maxY) {
            y++;
        } else if (z < maxZ) {
            y = minY;
            z++;
        } else if (x < maxX) {
            z = minZ;
            y = minY;
            x++;
        } else {
            exhausted = true;
        }
    }
}
