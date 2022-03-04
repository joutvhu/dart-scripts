package com.joutvhu.jetbrains.dartscripts.run;

import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.SimpleConfigurationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.util.EnvironmentUtil;
import com.joutvhu.jetbrains.dartscripts.util.DartBundle;
import com.joutvhu.jetbrains.dartscripts.util.DartIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartScriptConfigurationType extends SimpleConfigurationType {
    public static String ID = "DartScriptConfigurationType";
    public static String CONFIGURATION_NAME = "Dart Script";

    DartScriptConfigurationType() {
        super(
                ID, CONFIGURATION_NAME,
                DartBundle.message("ds.run.configuration.description.0.configuration", CONFIGURATION_NAME),
                NotNullLazyValue.lazy(() -> DartIcons.ScriptIcon)
        );
    }

    @Override
    public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        DartScriptRunConfiguration configuration = new DartScriptRunConfiguration(project, this, CONFIGURATION_NAME);
        String projectPath = project.getBasePath();
        if (projectPath != null)
            configuration.setScriptWorkingDirectory(projectPath);
        return configuration;
    }

    public static DartScriptConfigurationType getInstance() {
        return ConfigurationTypeUtil.findConfigurationType(DartScriptConfigurationType.class);
    }

    @Override
    public boolean isEditableInDumbMode() {
        return true;
    }

    public static @Nullable String getDefaultShell() {
        return EnvironmentUtil.getValue("SHELL");
    }
}
