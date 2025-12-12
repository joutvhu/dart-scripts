package com.joutvhu.intellij.dartscripts.run;

import com.intellij.execution.Executor;
import com.intellij.execution.configuration.EnvironmentVariablesData;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.LocatableConfigurationBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtilRt;
import com.joutvhu.intellij.dartscripts.util.DartBundle;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;

public class DartScriptRunConfiguration extends LocatableConfigurationBase<Element> {
    @Serial
    private static final long serialVersionUID = 8860638547700631226L;

    @NonNls
    private static final String TAG_PREFIX = "INDEPENDENT_";
    @NonNls
    private static final String SCRIPT_TEXT_TAG = "SCRIPT_TEXT";
    @NonNls
    private static final String SCRIPT_WORKING_DIRECTORY_TAG = "SCRIPT_WORKING_DIRECTORY";
    @NonNls
    private static final String EXECUTE_IN_TERMINAL_TAG = "EXECUTE_IN_TERMINAL";

    private String myScriptText = "";
    private String myScriptWorkingDirectory = "";
    private boolean myExecuteInTerminal = true;
    private EnvironmentVariablesData myEnvData = EnvironmentVariablesData.DEFAULT;

    DartScriptRunConfiguration(@NotNull Project project, @NotNull ConfigurationFactory factory, @NotNull String name) {
        super(project, factory, name);
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new DartScriptRunConfigurationEditor(getProject());
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        if (!FileUtil.exists(myScriptWorkingDirectory)) {
            throw new RuntimeConfigurationError(DartBundle.message("ds.run.working.dir.not.found"));
        }
    }

    @Override
    public @NotNull RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) {
        return new DartScriptRunConfigurationProfileState(environment.getProject(), this);
    }

    @Override
    public void writeExternal(@NotNull Element element) {
        super.writeExternal(element);
        JDOMExternalizerUtil.writeField(element, SCRIPT_TEXT_TAG, myScriptText);
        writePathWithMetadata(element, myScriptWorkingDirectory);
        JDOMExternalizerUtil.writeField(element, EXECUTE_IN_TERMINAL_TAG, String.valueOf(myExecuteInTerminal));
        myEnvData.writeExternal(element);
    }

    @Override
    public void readExternal(@NotNull Element element) throws InvalidDataException {
        super.readExternal(element);
        myScriptText = readStringTagValue(element, SCRIPT_TEXT_TAG);
        myScriptWorkingDirectory = readPathWithMetadata(element);
        myExecuteInTerminal = Boolean.parseBoolean(JDOMExternalizerUtil.readField(element, EXECUTE_IN_TERMINAL_TAG, Boolean.TRUE.toString()));
        myEnvData = EnvironmentVariablesData.readExternal(element);
    }

    private static void writePathWithMetadata(@NotNull Element element, @NotNull String path) {
        String systemIndependentPath = FileUtil.toSystemIndependentName(path);
        JDOMExternalizerUtil.writeField(element, TAG_PREFIX + SCRIPT_WORKING_DIRECTORY_TAG, Boolean.toString(systemIndependentPath.equals(path)));
        JDOMExternalizerUtil.writeField(element, SCRIPT_WORKING_DIRECTORY_TAG, systemIndependentPath);
    }

    private static String readPathWithMetadata(@NotNull Element element) {
        return Boolean.parseBoolean(JDOMExternalizerUtil.readField(element, TAG_PREFIX + SCRIPT_WORKING_DIRECTORY_TAG))
            ? readStringTagValue(element, SCRIPT_WORKING_DIRECTORY_TAG)
            : FileUtil.toSystemDependentName(readStringTagValue(element, SCRIPT_WORKING_DIRECTORY_TAG));
    }

    @NotNull
    private static String readStringTagValue(@NotNull Element element, @NotNull String tagName) {
        return StringUtilRt.notNullize(JDOMExternalizerUtil.readField(element, tagName), "");
    }

    public String getScriptText() {
        return myScriptText;
    }

    public void setScriptText(String scriptText) {
        myScriptText = scriptText;
    }

    public String getScriptWorkingDirectory() {
        return myScriptWorkingDirectory;
    }

    public void setScriptWorkingDirectory(String scriptWorkingDirectory) {
        myScriptWorkingDirectory = scriptWorkingDirectory.trim();
    }

    public boolean isExecuteInTerminal() {
        return myExecuteInTerminal;
    }

    public void setExecuteInTerminal(boolean executeInTerminal) {
        myExecuteInTerminal = executeInTerminal;
    }

    public EnvironmentVariablesData getEnvData() {
        return myEnvData;
    }

    public void setEnvData(EnvironmentVariablesData envData) {
        myEnvData = envData;
    }
}
