package dev.twme.vanillaworldedit.service;

import com.sk89q.worldedit.math.BlockVector3;
import dev.twme.vanillaworldedit.command.EditMode;
import dev.twme.vanillaworldedit.command.EditRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorldEditExecutorTest {

    @Test
    void enforcesConfiguredVolumeBeforeWorldAccess() {
        WorldEditExecutor executor = new WorldEditExecutor(1_000);
        EditRequest accepted = request(9, 9, 9);
        EditRequest rejected = request(10, 10, 10);

        assertDoesNotThrow(() -> executor.validateVolume(accepted));
        assertThrows(IllegalArgumentException.class, () -> executor.validateVolume(rejected));
    }

    private static EditRequest request(int maxX, int maxY, int maxZ) {
        return new EditRequest(
                BlockVector3.ZERO,
                BlockVector3.at(maxX, maxY, maxZ),
                "stone",
                EditMode.SET,
                null
        );
    }
}
