package com.paletiers.tiertagger.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PlayerNameUtil {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("(?i)[a-z0-9_]{3,16}");

    private PlayerNameUtil() {}

    public static String normalizeForLookup(String rawName) {
        if (rawName == null) {
            return null;
        }

        String trimmed = rawName.trim();
        if (trimmed.isBlank()) {
            return null;
        }

        String withoutFormatting = stripLegacyFormatting(trimmed).trim();
        if (withoutFormatting.isBlank()) {
            return null;
        }

        if (USERNAME_PATTERN.matcher(withoutFormatting).matches()) {
            return withoutFormatting;
        }

        Matcher matcher = USERNAME_PATTERN.matcher(withoutFormatting);
        String firstMatch = null;
        String lastMatch = null;
        while (matcher.find()) {
            String token = matcher.group();
            if (firstMatch == null) {
                firstMatch = token;
            }
            lastMatch = token;
        }

        if (lastMatch == null) {
            return withoutFormatting;
        }

        // Prefixes are the dominant case for plugin-formatted nametags.
        // If the string starts with common rank wrappers, prefer the last username-like token.
        if (startsWithPrefixWrapper(withoutFormatting)) {
            return lastMatch;
        }

        // Otherwise prefer the first token to avoid suffixes like "AFK" being picked.
        return firstMatch != null ? firstMatch : lastMatch;
    }

    private static boolean startsWithPrefixWrapper(String value) {
        return value.startsWith("[")
            || value.startsWith("(")
            || value.startsWith("{")
            || value.startsWith("<");
    }

    private static String stripLegacyFormatting(String input) {
        StringBuilder sb = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\u00A7' && i + 1 < input.length()) {
                i++;
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
