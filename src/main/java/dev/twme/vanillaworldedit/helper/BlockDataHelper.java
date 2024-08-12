package dev.twme.vanillaworldedit.helper;

import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class BlockDataHelper {

    private static final Pattern BLOCK_DATA_PARSERER = Pattern.compile("~(-?\\d*)");

    public static BlockVector parseCoordinateString(String input, BlockVector vector) {
        var playerX = vector.getBlockX();
        var playerY = vector.getBlockY();
        var playerZ = vector.getBlockZ();

        var replacedX = BlockDataHelper.replaceTildeWithCoordinate(input, playerX);
        var replacedY = BlockDataHelper.replaceTildeWithCoordinate(replacedX, playerY);
        var replacedZ = BlockDataHelper.replaceTildeWithCoordinate(replacedY, playerZ);

        var parts = replacedZ.split(" ");
        var x = BlockDataHelper.evaluateExpression(parts[0]);
        var y = BlockDataHelper.evaluateExpression(parts[1]);
        var z = BlockDataHelper.evaluateExpression(parts[2]);

        return new BlockVector(x, y, z);
    }

    public static String convertBlockData(@NotNull String input) {
        return "\"" + input.replace("#minecraft:", "##").replace("[", " ^[") + "\"";
    }

    private static String replaceTildeWithCoordinate(String input, double playerCoordinate) {
        var matcher = BLOCK_DATA_PARSERER.matcher(input);
        var result = new StringBuilder();

        if (matcher.find()) {
            var relative = matcher.group(1);
            var replacement = relative.isEmpty() ? String.valueOf(playerCoordinate) : playerCoordinate + (relative.startsWith("-") ? relative : "+" + relative);

            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private static double evaluateExpression(String expression) {
        expression = expression.replace(',', '.');
        return BlockDataHelper.parseArithmetic(expression);
    }

    private static double parseArithmetic(String expr) {
        var tokens = expr.split("(?=[-+*/])|(?<=[-+*/])");
        var result = 0D;
        var currentNumber = 0D;
        var operation = '+';

        for (String token : tokens) {
            token = token.trim();
            if (token.isEmpty()) continue;

            if (token.matches("[0-9.]+")) {
                currentNumber = Double.parseDouble(token);

            } else if (token.matches("[+*/-]")) {
                result = BlockDataHelper.evaluate(operation, result, currentNumber);
                operation = token.charAt(0);
                currentNumber = 0;
            }
        }

        result = BlockDataHelper.evaluate(operation, result, currentNumber);
        return result;
    }

    private static double evaluate(char operation, double value, double currentValue) {
        switch (operation) {
            case '+' -> value += currentValue;
            case '-' -> value -= currentValue;
            case '*' -> value *= currentValue;
            case '/' -> value /= currentValue;
        }
        return value;
    }
}
