package com.joutvhu.intellij.dartscripts.util;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.function.Supplier;

public class DartBundle extends DynamicBundle {
    @NonNls
    public static final String BUNDLE = "messages.DartScriptsBundle";
    private static final DartBundle INSTANCE = new DartBundle();

    private DartBundle() {
        super(BUNDLE);
    }

    @NotNull
    @Nls
    public static String message(
        @NotNull @PropertyKey(resourceBundle = BUNDLE) String key,
        @NotNull Object... params
    ) {
        return INSTANCE.getMessage(key, params);
    }

    @NotNull
    public static Supplier<String> messagePointer(
        @NotNull @PropertyKey(resourceBundle = BUNDLE) String key,
        @NotNull Object... params
    ) {
        return INSTANCE.getLazyMessage(key, params);
    }
}
