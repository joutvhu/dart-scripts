package com.joutvhu.intellij.dartscripts.run;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.NotNull;

public interface DartScriptRunner {
    void run(
            @NotNull Project project,
            @NotNull String command,
            @NotNull String workingDirectory,
            @NotNull @NlsContexts.TabTitle String title,
            boolean activateToolWindow
    );

    boolean isAvailable(@NotNull Project project);
}
