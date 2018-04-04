package com.github.hyla.grackle.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("WeakerAccess")
public class RegexUtils {

    private RegexUtils() {
    }

    public static List<Integer> allMatchedPositions(Pattern pattern, String s) {
        List<Integer> result = new ArrayList<>();

        Matcher matcher = pattern.matcher(s);
        while (matcher.find()) {
            result.add(matcher.start());
        }

        return result;
    }
}
