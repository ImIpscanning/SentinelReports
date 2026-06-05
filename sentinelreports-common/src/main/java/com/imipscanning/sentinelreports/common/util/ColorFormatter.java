package com.imipscanning.sentinelreports.common.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ColorFormatter {
    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final Map<Character, String> LEGACY = new LinkedHashMap<>();

    static {
        LEGACY.put('0', "black");
        LEGACY.put('1', "dark_blue");
        LEGACY.put('2', "dark_green");
        LEGACY.put('3', "dark_aqua");
        LEGACY.put('4', "dark_red");
        LEGACY.put('5', "dark_purple");
        LEGACY.put('6', "gold");
        LEGACY.put('7', "gray");
        LEGACY.put('8', "dark_gray");
        LEGACY.put('9', "blue");
        LEGACY.put('a', "green");
        LEGACY.put('b', "aqua");
        LEGACY.put('c', "red");
        LEGACY.put('d', "light_purple");
        LEGACY.put('e', "yellow");
        LEGACY.put('f', "white");
    }

    private ColorFormatter() {
    }

    public static Component component(String input) {
        return MINI.deserialize(legacyToMini(input == null ? "" : input));
    }

    public static String legacyToMini(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if ((c == '&' || c == '§') && i + 1 < input.length()) {
                char code = Character.toLowerCase(input.charAt(++i));
                String color = LEGACY.get(code);
                if (color != null) {
                    out.append('<').append(color).append('>');
                    continue;
                }
                switch (code) {
                    case 'l' -> out.append("<bold>");
                    case 'o' -> out.append("<italic>");
                    case 'n' -> out.append("<underlined>");
                    case 'm' -> out.append("<strikethrough>");
                    case 'r' -> out.append("<reset>");
                    default -> {
                        out.append(c).append(code);
                    }
                }
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}
