package com.joutvhu.intellij.dartscripts.util;

import com.intellij.ui.IconManager;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.Serializable;

@UtilityClass
public class DartIcons implements Serializable {
    private static final long serialVersionUID = -7782889778971848655L;

    @NotNull
    public final Icon ScriptIcon = load("icons/dartScript.svg", -7782889778971848656L, 0);

    @NotNull
    private Icon load(@NotNull String path, long cacheKey, int flags) {
        return IconManager.getInstance().loadRasterizedIcon(path, DartIcons.class.getClassLoader(), cacheKey, flags);
    }
}
