package dev.twme.vanillaworldedit.service;

import com.sk89q.worldedit.math.BlockVector3;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CuboidCursorTest {

    @Test
    void visitsEveryCoordinateExactlyOnce() {
        CuboidCursor cursor = new CuboidCursor(
                BlockVector3.at(-1, 4, 8),
                BlockVector3.at(0, 5, 10)
        );
        List<String> visited = new ArrayList<>();

        while (cursor.hasNext()) {
            visited.add(cursor.x() + "," + cursor.y() + "," + cursor.z());
            cursor.advance();
        }

        assertEquals(12, visited.size());
        assertEquals(12, visited.stream().distinct().count());
        assertEquals("-1,4,8", visited.getFirst());
        assertEquals("0,5,10", visited.getLast());
        assertFalse(cursor.hasNext());
    }

    @Test
    void stateSizeDoesNotDependOnCuboidVolume() {
        long primitiveFields = List.of(CuboidCursor.class.getDeclaredFields()).stream()
                .map(Field::getType)
                .filter(Class::isPrimitive)
                .count();

        assertEquals(10, primitiveFields);
    }
}
