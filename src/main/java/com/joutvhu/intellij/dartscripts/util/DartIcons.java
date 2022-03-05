package com.joutvhu.intellij.dartscripts.util;

import com.intellij.ui.IconManager;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.Serializable;

@UtilityClass
public class DartIcons implements Serializable {
    @NotNull
    public final Icon ScriptIcon = load("icons/dartScript.svg", 9113565375246300682L, 0);

    @NotNull
    private Icon load(@NotNull String path, long cacheKey, int flags) {
        return IconManager.getInstance().loadRasterizedIcon(path, DartIcons.class.getClassLoader(), cacheKey, flags);
    }
}
