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
    public final Icon ScriptIcon = load("icons/dartScript.svg");

    @NotNull
    private Icon load(@NotNull String path) {
        return IconManager.getInstance().getIcon(path, DartIcons.class);
    }
}
