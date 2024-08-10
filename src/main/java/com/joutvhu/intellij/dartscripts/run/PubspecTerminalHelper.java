package com.joutvhu.intellij.dartscripts.run;

import com.intellij.execution.ExecutionManager;
import com.intellij.execution.Executor;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.openapi.project.Project;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PubspecTerminalHelper {
    public RunConfiguration runScript(
        Executor executor,
        Project project,
        String name,
        String script,
        String workingDirectory,
        Boolean executeInTerminal
    ) {
        RunManager runManager = RunManager.getInstance(project);
        DartScriptConfigurationType configurationType = ConfigurationTypeUtil
            .findConfigurationType(DartScriptConfigurationType.class);

        RunnerAndConfigurationSettings configurationSettings = runManager
            .findConfigurationByTypeAndName(configurationType, name);
        if (configurationSettings == null) {
            configurationSettings = runManager.createConfiguration(name, configurationType);
            runManager.addConfiguration(configurationSettings);
        }
        runManager.setSelectedConfiguration(configurationSettings);

        DartScriptRunConfiguration runConfiguration = (DartScriptRunConfiguration) configurationSettings.getConfiguration();
        runConfiguration.setScriptText(script);
        runConfiguration.setExecuteInTerminal(Boolean.TRUE.equals(executeInTerminal));
        runConfiguration.setScriptWorkingDirectory(workingDirectory);

        ExecutionEnvironmentBuilder builder = ExecutionEnvironmentBuilder
            .createOrNull(executor, runConfiguration);
        if (builder != null)
            ExecutionManager.getInstance(project).restartRunProfile(builder.build());

        return runConfiguration;
    }
}
