package com.github.videobox.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckerUrl {
    public static String getVideoIdFromYoutubeUrl(String url) {
        Matcher matcher = Pattern.compile("http(?:s)?:\\/\\/(?:m.)?(?:www\\.)?youtu(?:\\.be\\/|be\\.com\\/(?:watch\\?(?:feature=youtu.be\\&)?v=|v\\/|embed\\/|user\\/(?:[\\w#]+\\/)+))([^&#?\\n]+)", 2).matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }
}
