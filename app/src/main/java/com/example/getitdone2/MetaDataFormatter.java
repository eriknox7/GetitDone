package com.example.getitdone2;
public class MetaDataFormatter {
    public static String titleFormatter(String title) {
        if(title.length() > 22) {
            return title.substring(0, 22) + "...";
        }
        return title;
    }
    public static String contentFormatter(String content) {
        String[] lines = content.split("\n", 2);
        content = lines[0];
        if(content.length() > 35) {
            return content.substring(0, 35) + "...";
        }
        return content;
    }
}