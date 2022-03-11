package com.joutvhu.intellij.dartscripts.run;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public interface DartScriptRunner {
    void run(
            @NotNull Project project,
            @NotNull String command,
            @NotNull String workingDirectory,
            @NotNull String title,
            boolean activateToolWindow
    );

    boolean isAvailable(@NotNull Project project);
}
