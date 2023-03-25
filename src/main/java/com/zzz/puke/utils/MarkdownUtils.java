package com.zzz.puke.utils;

public class MarkdownUtils {
    public static String mode = "{\n" +
            "    \"msgtype\": \"markdown\",\n" +
            "    \"markdown\": {\n" +
            "    \"content\": \"%s\"\n" +
            "    }\n" +
            "}";

    public static String getMarkdown(String content) {
        return String.format(mode, content);
    }

}
