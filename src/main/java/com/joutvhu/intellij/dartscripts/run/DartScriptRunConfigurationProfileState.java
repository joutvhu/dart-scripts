package com.joutvhu.intellij.dartscripts.run;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.Platform;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.PtyCommandLine;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.process.KillableProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.util.ExecUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.terminal.TerminalExecutionConsole;
import com.intellij.util.EnvironmentUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.io.BaseDataReader;
import com.intellij.util.io.BaseOutputReader;
import com.joutvhu.intellij.dartscripts.util.ShellStringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DartScriptRunConfigurationProfileState implements RunProfileState {
    private final Project myProject;
    private final DartScriptRunConfiguration myRunConfiguration;

    DartScriptRunConfigurationProfileState(@NotNull Project project, @NotNull DartScriptRunConfiguration runConfiguration) {
        myProject = project;
        myRunConfiguration = runConfiguration;
    }

    @Override
    public @Nullable ExecutionResult execute(Executor executor, @NotNull ProgramRunner<?> runner) throws ExecutionException {
        if (myRunConfiguration.isExecuteInTerminal()) {
            DartScriptRunner dsRunner = ApplicationManager.getApplication().getService(DartScriptRunner.class);
            if (dsRunner != null && dsRunner.isAvailable(myProject)) {
                dsRunner.run(myProject, buildCommand(), myRunConfiguration.getScriptWorkingDirectory(), myRunConfiguration.getName(),
                        isActivateToolWindow());
                return null;
            }
        }
        return buildExecutionResult();
    }

    private boolean isActivateToolWindow() {
        RunnerAndConfigurationSettings settings = RunManager.getInstance(myProject).findSettings(myRunConfiguration);
        return settings == null || settings.isActivateToolWindowBeforeRun();
    }

    private ExecutionResult buildExecutionResult() throws ExecutionException {
        GeneralCommandLine commandLine = createCommandLineForScript();
        ProcessHandler processHandler = createProcessHandler(commandLine);
        ProcessTerminatedListener.attach(processHandler);
        ConsoleView console = new TerminalExecutionConsole(myProject, processHandler);
        console.attachToProcess(processHandler);
        return new DefaultExecutionResult(console, processHandler);
    }

    @NotNull
    private static ProcessHandler createProcessHandler(GeneralCommandLine commandLine) throws ExecutionException {
        return new KillableProcessHandler(commandLine) {
            @NotNull
            @Override
            protected BaseOutputReader.Options readerOptions() {
                return new BaseOutputReader.Options() {
                    @Override
                    public BaseDataReader.SleepingPolicy policy() {
                        return BaseDataReader.SleepingPolicy.BLOCKING;
                    }

                    @Override
                    public boolean splitToLines() {
                        return false;
                    }
                };
            }
        };
    }

    @NotNull
    private GeneralCommandLine createCommandLineForScript() {
        PtyCommandLine commandLine = getTerminalCommand();
        if (!SystemInfo.isWindows) {
            commandLine.getEnvironment().put("TERM", "xterm-256color");
        }

        commandLine.withConsoleMode(false);
        commandLine.withInitialColumns(120);
        commandLine.withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE);
        commandLine.setWorkDirectory(this.myRunConfiguration.getScriptWorkingDirectory());
        commandLine.withParameters(this.myRunConfiguration.getScriptText());

        return commandLine;
    }

    private PtyCommandLine getTerminalCommand() {
        PtyCommandLine commandLine = new PtyCommandLine();
        commandLine.setCharset(StandardCharsets.UTF_8);
        String defaultShell = DartScriptConfigurationType.getDefaultShell();
        if (SystemInfo.isWindows) {
            commandLine.withExePath(ObjectUtils.notNull(defaultShell, ExecUtil.getWindowsShellName()));
            commandLine.withParameters("/c");
        } else if (SystemInfo.isMac) {
            commandLine.withExePath(ObjectUtils.notNull(defaultShell, ExecUtil.getOpenCommandPath()));
            commandLine.withParameters("-a");
        } else {
            commandLine.withExePath(ObjectUtils.notNull(defaultShell, "/bin/sh"));
            commandLine.withParameters(EnvironmentUtil.SHELL_COMMAND_ARGUMENT);
        }
        return commandLine;
    }

    @NotNull
    private String buildCommand() throws ExecutionException {
        List<String> commandLine = new ArrayList<>();
        addIfPresent(commandLine, myRunConfiguration.getEnvData().getEnvs(), true);
        addIfPresent(commandLine, myRunConfiguration.getScriptText());
        return String.join(" ", commandLine);
    }

    private static void addIfPresent(@NotNull List<String> commandLine, @Nullable String options) {
        ContainerUtil.addIfNotNull(commandLine, StringUtil.nullize(options));
    }

    private static void addIfPresent(
            @NotNull List<String> commandLine,
            @NotNull Map<String, String> envs,
            boolean endWithSemicolon
    ) {
        int index = 0;
        for (Map.Entry<String, String> entry : envs.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String quotedString;
            if (Platform.current() != Platform.WINDOWS) {
                quotedString = ShellStringUtil.quote(value);
            } else {
                String escapedValue = StringUtil.escapeQuotes(value);
                quotedString = StringUtil.containsWhitespaces(value) ? StringUtil.QUOTER.fun(escapedValue) : escapedValue;
            }
            if (endWithSemicolon) {
                String semicolon = "";
                if (index == envs.size() - 1) semicolon = ";";
                commandLine.add("export " + key + "=" + quotedString + semicolon);
            } else {
                commandLine.add(key + "=" + quotedString);
            }
            index++;
        }
    }
}