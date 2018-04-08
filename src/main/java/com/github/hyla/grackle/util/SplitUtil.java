package com.github.hyla.grackle.util;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class SplitUtil {

    private final static Pattern CAPITAL_LETTER_PAT = Pattern.compile("(?<!^)(?=[A-Z])");

    private SplitUtil() {
    }

    public static <T> Optional<Tuple2<String, T>> findOnRight(String line, Function<String, Optional<T>> finder) {
        List<Integer> positions = RegexUtils.allMatchedPositions(CAPITAL_LETTER_PAT, line);
        positions.add(line.length()); // to check empty string

        for (int position : positions) {
            String candidate = StringUtils.uncapitalize(line.substring(position));
            Optional<T> thing = finder.apply(candidate);
            if (thing.isPresent()) {
                return Optional.of(Tuple2.create(line.substring(0, position), thing.get()));
            }
        }

        return Optional.empty();
    }

    public static List<String> splitWithAliases(String s, Map<String, List<String>> aliases) {
        return Arrays.stream(s.split("_"))
                .map(StringUtils::uncapitalize)
                .flatMap(part -> aliases.getOrDefault(part, singletonList(part)).stream())
                .collect(toList());
    }
}
