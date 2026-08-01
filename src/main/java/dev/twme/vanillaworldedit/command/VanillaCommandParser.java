package dev.twme.vanillaworldedit.command;

import com.sk89q.worldedit.math.BlockVector3;

import java.util.Locale;

public final class VanillaCommandParser {

    private VanillaCommandParser() {
    }

    public static EditRequest parse(String message, BlockVector3 origin) {
        String[] args = message.trim().split("\\s+");
        String command = args[0].toLowerCase(Locale.ROOT);
        if (command.endsWith("fill")) {
            requireLength(args, 8, "/fill <from> <to> <block> [mode] [filter]");
            BlockVector3 first = coordinates(args, 1, origin);
            BlockVector3 second = coordinates(args, 4, origin);
            EditMode mode = parseMode(args.length > 8 ? args[8] : "set", true);
            String filter = mode == EditMode.REPLACE && args.length > 9 ? args[9] : null;
            return new EditRequest(first, second, args[7], mode, filter);
        }
        if (command.endsWith("setblock")) {
            requireLength(args, 5, "/setblock <pos> <block> [mode]");
            BlockVector3 position = coordinates(args, 1, origin);
            EditMode mode = parseMode(args.length > 5 ? args[5] : "set", false);
            return new EditRequest(position, position, args[4], mode, null);
        }
        throw new IllegalArgumentException("unsupported command");
    }

    private static BlockVector3 coordinates(String[] args, int offset, BlockVector3 origin) {
        return BlockVector3.at(
                coordinate(args[offset], origin.x()),
                coordinate(args[offset + 1], origin.y()),
                coordinate(args[offset + 2], origin.z())
        );
    }

    private static int coordinate(String token, int origin) {
        try {
            double value = token.startsWith("~")
                    ? origin + (token.length() == 1 ? 0 : ArithmeticParser.evaluate(token.substring(1)))
                    : ArithmeticParser.evaluate(token);
            if (!Double.isFinite(value) || value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                throw new NumberFormatException("out of range");
            }
            return (int) Math.floor(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("invalid coordinate: " + token, exception);
        }
    }

    private static EditMode parseMode(String value, boolean fill) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "set", "none", "replace_all" -> EditMode.SET;
            case "destroy" -> EditMode.DESTROY;
            case "keep" -> EditMode.KEEP;
            case "replace" -> EditMode.REPLACE;
            case "hollow" -> {
                if (!fill) throw new IllegalArgumentException("hollow is only valid for /fill");
                yield EditMode.HOLLOW;
            }
            case "outline" -> {
                if (!fill) throw new IllegalArgumentException("outline is only valid for /fill");
                yield EditMode.OUTLINE;
            }
            default -> throw new IllegalArgumentException("unsupported mode: " + value);
        };
    }

    private static void requireLength(String[] args, int length, String usage) {
        if (args.length < length) {
            throw new IllegalArgumentException("usage: " + usage);
        }
    }

    private static final class ArithmeticParser {

        private final String input;
        private int position;

        private ArithmeticParser(String input) {
            this.input = input.replace(',', '.');
        }

        static double evaluate(String input) {
            ArithmeticParser parser = new ArithmeticParser(input);
            double result = parser.expression();
            if (parser.position != parser.input.length()) {
                throw new NumberFormatException("unexpected character");
            }
            return result;
        }

        private double expression() {
            double value = term();
            while (position < input.length()) {
                char operator = input.charAt(position);
                if (operator != '+' && operator != '-') break;
                position++;
                double right = term();
                value = operator == '+' ? value + right : value - right;
            }
            return value;
        }

        private double term() {
            double value = factor();
            while (position < input.length()) {
                char operator = input.charAt(position);
                if (operator != '*' && operator != '/') break;
                position++;
                double right = factor();
                value = operator == '*' ? value * right : value / right;
            }
            return value;
        }

        private double factor() {
            if (position >= input.length()) throw new NumberFormatException("missing number");
            char current = input.charAt(position);
            if (current == '+' || current == '-') {
                position++;
                double value = factor();
                return current == '-' ? -value : value;
            }
            if (current == '(') {
                position++;
                double value = expression();
                if (position >= input.length() || input.charAt(position) != ')') {
                    throw new NumberFormatException("missing closing parenthesis");
                }
                position++;
                return value;
            }
            int start = position;
            while (position < input.length()) {
                char value = input.charAt(position);
                if ((value < '0' || value > '9') && value != '.') break;
                position++;
            }
            if (start == position) throw new NumberFormatException("missing number");
            return Double.parseDouble(input.substring(start, position));
        }
    }
}
