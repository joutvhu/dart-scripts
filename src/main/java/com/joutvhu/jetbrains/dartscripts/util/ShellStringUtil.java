package com.joutvhu.jetbrains.dartscripts.util;

import com.intellij.openapi.util.text.StringUtil;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@UtilityClass
public class ShellStringUtil {
    private final char[] ORIGIN_CHARS = new char[]{' ', '!', '"', '#', '$', '&', '\'', '(', ')', '*', ',', ';', '<', '>', '=', '?', '[', '\\', ']', '^', '`', '{', '|', '}'};
    private final List<String> ENCODED;
    private final List<String> ORIGINS;

    private List<String> toStr(char[] arr, Character prefix) {
        return IntStream.range(0, arr.length).mapToObj((i) -> {
            String v = String.valueOf(arr[i]);
            return prefix != null ? prefix + v : v;
        }).collect(Collectors.toList());
    }

    @NotNull
    public String quote(String name) {
        return StringUtil.replace(name, ORIGINS, ENCODED);
    }

    @NotNull
    public String unquote(String afterSlash) {
        return StringUtil.replace(afterSlash, ENCODED, ORIGINS);
    }

    static {
        ENCODED = toStr(ORIGIN_CHARS, '\\');
        ORIGINS = toStr(ORIGIN_CHARS, (Character)null);
    }
}
