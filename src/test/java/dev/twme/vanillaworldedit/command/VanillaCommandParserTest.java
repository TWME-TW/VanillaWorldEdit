package dev.twme.vanillaworldedit.command;

import com.sk89q.worldedit.math.BlockVector3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VanillaCommandParserTest {

    private static final BlockVector3 ORIGIN = BlockVector3.at(10, 64, -5);

    @Test
    void parsesRelativeFillWithoutChangingBlockText() {
        EditRequest request = VanillaCommandParser.parse(
                "/fill ~ ~-2 ~1 ~2 ~ ~3 minecraft:oak_log[axis=x] hollow",
                ORIGIN
        );

        assertEquals(BlockVector3.at(10, 62, -4), request.first());
        assertEquals(BlockVector3.at(12, 64, -2), request.second());
        assertEquals("minecraft:oak_log[axis=x]", request.block());
        assertEquals(EditMode.HOLLOW, request.mode());
        assertNull(request.replaceFilter());
        assertEquals(27, request.volume());
    }

    @Test
    void evaluatesAdvertisedCoordinateExpressionsWithPrecedence() {
        EditRequest request = VanillaCommandParser.parse(
                "/setblock ~(2+3) ~2*3-1 ~-4/2 stone",
                ORIGIN
        );

        assertEquals(BlockVector3.at(15, 69, -7), request.first());
    }

    @Test
    void floorsNegativeDecimalCoordinatesLikeBlockCoordinates() {
        EditRequest request = VanillaCommandParser.parse(
                "/setblock -0.1 63.9 ~-0.1 stone",
                BlockVector3.at(0, 64, 0)
        );

        assertEquals(BlockVector3.at(-1, 63, -1), request.first());
    }

    @Test
    void parsesNamespacedCommandAndReplaceFilter() {
        EditRequest request = VanillaCommandParser.parse(
                "/minecraft:fill 0 1 2 3 4 5 gold_block replace #minecraft:logs",
                ORIGIN
        );

        assertEquals(EditMode.REPLACE, request.mode());
        assertEquals("#minecraft:logs", request.replaceFilter());
    }

    @Test
    void setblockReplaceMeansUnconditionalSet() {
        EditRequest request = VanillaCommandParser.parse(
                "/setblock 1 2 3 stone replace",
                ORIGIN
        );

        assertEquals(EditMode.REPLACE, request.mode());
        assertNull(request.replaceFilter());
        assertEquals(1, request.volume());
    }

    @Test
    void rejectsMalformedCommandsAndModes() {
        assertThrows(IllegalArgumentException.class,
                () -> VanillaCommandParser.parse("/fill 1 2", ORIGIN));
        assertThrows(IllegalArgumentException.class,
                () -> VanillaCommandParser.parse("/setblock 1 nope 3 stone", ORIGIN));
        assertThrows(IllegalArgumentException.class,
                () -> VanillaCommandParser.parse("/setblock 1 2 3 stone hollow", ORIGIN));
    }
}
