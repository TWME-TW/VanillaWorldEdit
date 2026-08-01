package dev.twme.vanillaworldedit.command;

import com.sk89q.worldedit.math.BlockVector3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EditRequestTest {

    @Test
    void normalizesCornersAndCalculatesVolumeWithLongArithmetic() {
        EditRequest request = new EditRequest(
                BlockVector3.at(20, 80, 5),
                BlockVector3.at(-10, 70, -5),
                "stone",
                EditMode.SET,
                null
        );

        assertEquals(BlockVector3.at(-10, 70, -5), request.min());
        assertEquals(BlockVector3.at(20, 80, 5), request.max());
        assertEquals(31L * 11L * 11L, request.volume());
    }
}
