package ir.ac.pvz.model.support;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class JsonTableReader {
    private static final Pattern FIELD = Pattern.compile(
            "\\\"([^\\\"]+)\\\"\\s*:\\s*(?:\\\"((?:\\\\.|[^\\\"])*)\\\"|(-?\\d+(?:\\.\\d+)?))");

    private JsonTableReader() {
    }
    public static List<Map<String, String>> readObjectArray(
            String fileName, String arrayName) throws IOException {
        String json = read(fileName);
        int key = json.indexOf('"' + arrayName + '"');
        int arrayStart = -1;
        if (key >= 0) {
            arrayStart = json.indexOf('[', key);
        }
        if (arrayStart < 0) {
            throw new IOException("Missing JSON array: " + arrayName);
        }
        return parseFlatObjectArray(json, arrayStart, arrayName);
    }
    private static List<Map<String, String>> parseFlatObjectArray(
            String json, int arrayStart, String arrayName) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();
        boolean inString = false;
        boolean escaped = false;
        int depth = 0;
        int objectStart = -1;
        for (int index = arrayStart + 1; index < json.length(); index++) {
            char character = json.charAt(index);
            if (inString) {
                if (escaped) {
                    escaped = false;
                }
                else if (character == '\\') {
                    escaped = true;
                }
                else if (character == '"') {
                    inString = false;
                }
                continue;
            }
            if (character == '"') {
                inString = true;
            }
            else if (character == '{') {
                if (depth == 0) {
                    objectStart = index;
                }
                depth++;
            }
            else if (character == '}') {
                depth--;
                if (depth == 0 && objectStart >= 0) {
                    rows.add(parseObject(json.substring(objectStart, index + 1)));
                    objectStart = -1;
                }
            }
            else if (character == ']' && depth == 0) {
                return rows;
            }
        }
        throw new IOException("Unterminated JSON array: " + arrayName);
    }
    public static List<Map<String, String>> readRootObjectArray(String fileName)
            throws IOException {
        String json = read(fileName);
        int arrayStart = firstNonWhitespace(json);
        if (arrayStart < 0 || json.charAt(arrayStart) != '[') {
            throw new IOException("JSON root must be an array.");
        }
        return parseStructuredObjectArray(json, arrayStart);
    }
    public static List<Map<String, String>> readInlineObjectArray(String json)
            throws IOException {
        int arrayStart = firstNonWhitespace(json);
        if (arrayStart < 0 || json.charAt(arrayStart) != '[') {
            throw new IOException("JSON value must be an array.");
        }
        return parseStructuredObjectArray(json, arrayStart);
    }
    public static List<String> readInlineStringArray(String json)
            throws IOException {
        List<String> values = new ArrayList<>();
        int index = firstNonWhitespace(json);
        if (index < 0 || json.charAt(index) != '[') {
            throw new IOException("JSON value must be a string array.");
        }
        index++;
        while (index < json.length()) {
            index = skipWhitespace(json, index);
            if (index < json.length() && json.charAt(index) == ']') return values;
            if (index >= json.length() || json.charAt(index) != '"') {
                throw new IOException("Invalid JSON string array.");
            }
            int end = stringEnd(json, index);
            values.add(unescape(json.substring(index + 1, end)));
            index = skipWhitespace(json, end + 1);
            if (index < json.length() && json.charAt(index) == ',') index++;
            else if (index >= json.length() || json.charAt(index) != ']') {
                throw new IOException("Invalid JSON string array separator.");
            }
        }
        throw new IOException("Unterminated JSON string array.");
    }
    public static int readRootInteger(String fileName, String fieldName)
            throws IOException {
        Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(fieldName)
                + "\\\"\\s*:\\s*(-?\\d+)").matcher(read(fileName));
        if (!matcher.find()) {
            throw new IOException("Missing JSON integer field: " + fieldName);
        }
        try {
            return Integer.parseInt(matcher.group(1));
        }
        catch (NumberFormatException exception) {
            throw new IOException("Invalid JSON integer field: " + fieldName,
                    exception);
        }
    }
    private static String read(String fileName) throws IOException {
        try (InputStream input = DataFileLocator.open(fileName)) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
    private static List<Map<String, String>> parseStructuredObjectArray(
            String json, int arrayStart) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();
        int index = arrayStart + 1;
        while (index < json.length()) {
            index = skipWhitespace(json, index);
            if (index < json.length() && json.charAt(index) == ']') return rows;
            if (index >= json.length() || json.charAt(index) != '{') {
                throw new IOException("JSON array must contain objects.");
            }
            int end = matchingEnd(json, index, '{', '}');
            rows.add(parseStructuredObject(json.substring(index, end + 1)));
            index = skipWhitespace(json, end + 1);
            if (index < json.length() && json.charAt(index) == ',') index++;
            else if (index >= json.length() || json.charAt(index) != ']') {
                throw new IOException("Invalid JSON object array separator.");
            }
        }
        throw new IOException("Unterminated JSON object array.");
    }
    private static Map<String, String> parseStructuredObject(String object)
            throws IOException {
        Map<String, String> values = new LinkedHashMap<>();
        int index = 1;
        while (index < object.length()) {
            index = skipWhitespace(object, index);
            if (index >= object.length()) {
                throw new IOException("Unterminated JSON object.");
            }
            if (object.charAt(index) == '}') return values;
            if (object.charAt(index) != '"') throw new IOException("Invalid JSON key.");
            int keyEnd = stringEnd(object, index);
            String key = unescape(object.substring(index + 1, keyEnd));
            index = skipWhitespace(object, keyEnd + 1);
            if (index >= object.length() || object.charAt(index) != ':') {
                throw new IOException("Missing JSON key separator.");
            }
            index = skipWhitespace(object, index + 1);
            if (index >= object.length()) {
                throw new IOException("Missing JSON value.");
            }
            int valueEnd;
            String value;
            char first = object.charAt(index);
            if (first == '"') {
                valueEnd = stringEnd(object, index);
                value = unescape(object.substring(index + 1, valueEnd));
            }
            else if (first == '[') {
                valueEnd = matchingEnd(object, index, '[', ']');
                value = object.substring(index, valueEnd + 1);
            }
            else if (first == '{') {
                valueEnd = matchingEnd(object, index, '{', '}');
                value = object.substring(index, valueEnd + 1);
            }
            else {
                valueEnd = index;
                while (valueEnd + 1 < object.length()
                        && object.charAt(valueEnd + 1) != ','
                        && object.charAt(valueEnd + 1) != '}') valueEnd++;
                value = object.substring(index, valueEnd + 1).trim();
            }
            values.put(key, value);
            index = skipWhitespace(object, valueEnd + 1);
            if (index < object.length() && object.charAt(index) == ',') index++;
            else if (index >= object.length() || object.charAt(index) != '}') {
                throw new IOException("Invalid JSON object separator.");
            }
        }
        throw new IOException("Unterminated JSON object.");
    }
    private static int firstNonWhitespace(String text) {
        return skipWhitespace(text, 0);
    }
    private static int skipWhitespace(String text, int index) {
        while (index < text.length() && Character.isWhitespace(text.charAt(index))) index++;
        return index;
    }
    private static int stringEnd(String text, int start) throws IOException {
        boolean escaped = false;
        for (int index = start + 1; index < text.length(); index++) {
            char character = text.charAt(index);
            if (escaped) escaped = false;
            else if (character == '\\') escaped = true;
            else if (character == '"') return index;
        }
        throw new IOException("Unterminated JSON string.");
    }
    private static int matchingEnd(String text, int start, char open, char close)
            throws IOException {
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int index = start; index < text.length(); index++) {
            char character = text.charAt(index);
            if (inString) {
                if (escaped) escaped = false;
                else if (character == '\\') escaped = true;
                else if (character == '"') inString = false;
            } else if (character == '"') inString = true;
            else if (character == open) depth++;
            else if (character == close && --depth == 0) return index;
        }
        throw new IOException("Unterminated JSON container.");
    }
    private static Map<String, String> parseObject(String object)
            throws IOException {
        Map<String, String> values = new LinkedHashMap<>();
        Matcher matcher = FIELD.matcher(object);
        while (matcher.find()) {
            String value = matcher.group(2);
            if (value != null) {
                value = unescape(value);
            }
            else {
                value = matcher.group(3);
            }
            values.put(matcher.group(1), value);
        }
        if (values.isEmpty()) {
            throw new IOException("Empty or invalid JSON object.");
        }
        return values;
    }
    private static String unescape(String value) throws IOException {
        StringBuilder result = new StringBuilder(value.length());
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (character != '\\') {
                result.append(character);
                continue;
            }
            if (++index >= value.length()) {
                throw new IOException("Invalid JSON escape sequence.");
            }
            char escaped = value.charAt(index);
            if (escaped == 'n') result.append('\n');
            else if (escaped == 'r') result.append('\r');
            else if (escaped == 't') result.append('\t');
            else if (escaped == 'b') result.append('\b');
            else if (escaped == 'f') result.append('\f');
            else if (escaped == '"' || escaped == '\\' || escaped == '/') {
                result.append(escaped);
            }
            else if (escaped == 'u' && index + 4 < value.length()) {
                result.append(readUnicodeEscape(value, index));
                index += 4;
            }
            else {
                throw new IOException("Unsupported JSON escape sequence.");
            }
        }
        return result.toString();
    }
    private static char readUnicodeEscape(String value, int index)
            throws IOException {
        String digits = value.substring(index + 1, index + 5);
        try {
            return (char) Integer.parseInt(digits, 16);
        }
        catch (NumberFormatException exception) {
            throw new IOException("Invalid JSON unicode escape.", exception);
        }
    }
}
