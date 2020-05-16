package picocli;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class AbbreviationMatcher {
    static List<String> splitIntoChunks(String command) {
        List<String> result = new ArrayList<String>();
        int start = 0;
        for (int i = 0, codepoint; i < command.length(); i += Character.charCount(codepoint)) {
            codepoint = command.codePointAt(i);
            if (Character.isUpperCase(codepoint) || '-' == codepoint) {
                String chunk = makeCanonical(command.substring(start, i));
                if (chunk.length() > 0) {
                    result.add(chunk);
                }
                start = i;
            }
        }
        if (start < command.length()) {
            String chunk = makeCanonical(command.substring(start));
            if (chunk.length() > 0) {
                result.add(chunk);
            }
        }
        return result;
    }

    private static String makeCanonical(String str) {
        if ("-".equals(str)) {
            return "";
        }
        if (str.startsWith("-") && str.length() > 1) {
            int codepoint = str.codePointAt(1);
            char[] uppercase = Character.toChars(Character.toUpperCase(codepoint));
            return new String(uppercase) + str.substring(1 + Character.charCount(codepoint));
        }
        return str;
    }

    static String match(Set<String> set, String abbreviation) {
        if (set.contains(abbreviation)) { // return exact match
            return abbreviation;
        }
        List<String> abbreviatedKeyChunks = splitIntoChunks(abbreviation);
        List<String> candidates = new ArrayList<String>();
        for (String key : set) {
            List<String> keyChunks = splitIntoChunks(key);
            if (matchKeyChunks(abbreviatedKeyChunks, keyChunks)) {
                candidates.add(key);
            }
        }
        if (candidates.size() > 1) {
            String str = candidates.toString();
            throw new IllegalArgumentException(abbreviation + " is not unique: it matches '" +
                    str.substring(1, str.length() - 1).replace(", ", "', '") + "'");
        }
        return candidates.isEmpty() ? abbreviation : candidates.get(0); // return the original if no match found
    }

    private static boolean matchKeyChunks(List<String> abbreviatedKeyChunks, List<String> keyChunks) {
        if (abbreviatedKeyChunks.size() > keyChunks.size()) {
            return false;
        } else if (!keyChunks.get(0).startsWith(abbreviatedKeyChunks.get(0))) { // first chunk must match
            return false;
        }
        int matchCount = 1, lastMatchChunk = 1;
        for (int i = 1; i < abbreviatedKeyChunks.size(); i++, matchCount++) {
            boolean found = false;
            for (int j = lastMatchChunk; j < keyChunks.size(); j++) {
                if (found = keyChunks.get(j).startsWith(abbreviatedKeyChunks.get(i))) {
                    lastMatchChunk = j + 1;
                    break;
                }
            }
            if (!found) { // not a candidate
                break;
            }
        }
        return matchCount == abbreviatedKeyChunks.size();
    }
}
