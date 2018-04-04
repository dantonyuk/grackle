package com.github.hyla.grackle.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SplitUtilTest {

    @Test
    public void testFindLongest() {
        Optional<Tuple2<String, String>> result =
                SplitUtil.findOnRight("aaa_bbbCccDddEee", fromValues("dddEee", "eee", "cccDddEee"));
        assertThat(result.isPresent(), is(true));
        if (result.isPresent()) { // to cheat code inspector
            assertThat(result.get().get_1(), is("aaa_bbb"));
            assertThat(result.get().get_2(), is("cccDddEee"));
        }
    }

    @Test
    public void testFindEmpty() {
        Optional<Tuple2<String, String>> result =
                SplitUtil.findOnRight("aaa_bbbCccDddEee", fromValues("xxx", ""));
        assertThat(result.isPresent(), is(true));
        if (result.isPresent()) { // to cheat code inspector
            assertThat(result.get().get_1(), is("aaa_bbbCccDddEee"));
            assertThat(result.get().get_2(), is(""));
        }
    }

    @Test
    public void testSplitWithNoAliases() {
        List<String> result = SplitUtil.splitWithAliases("aaa_bbbCcc_dddEee", new HashMap<>());
        assertThat(result, hasSize(3));
        assertThat(result.get(0), is("aaa"));
        assertThat(result.get(1), is("bbbCcc"));
        assertThat(result.get(2), is("dddEee"));
    }

    @Test
    public void testSplitWithAbsentAliases() {
        HashMap<String, List<String>> aliases = new HashMap<>();
        aliases.put("xxx", Arrays.asList("qqq", "www"));
        List<String> result = SplitUtil.splitWithAliases("aaa_bbbCcc_dddEee", aliases);
        assertThat(result, hasSize(3));
        assertThat(result.get(0), is("aaa"));
        assertThat(result.get(1), is("bbbCcc"));
        assertThat(result.get(2), is("dddEee"));
    }

    @Test
    public void testSplitWithExistingAliases() {
        HashMap<String, List<String>> aliases = new HashMap<>();
        aliases.put("aaa", Arrays.asList("qqq", "www"));
        List<String> result = SplitUtil.splitWithAliases("aaa_bbbCcc_dddEee", aliases);
        assertThat(result, hasSize(4));
        assertThat(result.get(0), is("qqq"));
        assertThat(result.get(1), is("www"));
        assertThat(result.get(2), is("bbbCcc"));
        assertThat(result.get(3), is("dddEee"));
    }

    private Function<String, Optional<String>> fromValues(String... values) {
        Map<String, String> map = Arrays.stream(values).collect(Collectors.toMap(x -> x, x -> x));
        return key -> Optional.ofNullable(map.get(key));
    }
}
