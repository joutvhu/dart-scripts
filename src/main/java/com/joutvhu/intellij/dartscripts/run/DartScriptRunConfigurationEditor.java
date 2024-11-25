package com.joutvhu.intellij.dartscripts.run;

import com.intellij.execution.configuration.EnvironmentVariablesComponent;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.components.JBCheckBox;
import com.joutvhu.intellij.dartscripts.util.DartBundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DartScriptRunConfigurationEditor extends SettingsEditor<DartScriptRunConfiguration> {
    private JPanel myPanel;
    private RawCommandLineEditor myScript;
    private TextFieldWithBrowseButton myScriptWorkingDirectory;
    private JBCheckBox myExecuteScriptInTerminal;
    private EnvironmentVariablesComponent myScriptEnvComponent;

    DartScriptRunConfigurationEditor(Project project) {
        FileChooserDescriptor fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        myScriptWorkingDirectory.addBrowseFolderListener(project, fileChooserDescriptor
            .withTitle(DartBundle.message("ds.label.choose.script.working.directory")).withDescription(""));
    }

    @Override
    protected void resetEditorFrom(@NotNull DartScriptRunConfiguration configuration) {
        // Configure UI for script text execution
        myScript.setText(configuration.getScriptText());
        myScriptWorkingDirectory.setText(configuration.getScriptWorkingDirectory());
        myExecuteScriptInTerminal.setSelected(configuration.isExecuteInTerminal());
        myScriptEnvComponent.setEnvData(configuration.getEnvData());
    }

    @Override
    protected void applyEditorTo(@NotNull DartScriptRunConfiguration configuration) {
        configuration.setScriptText(myScript.getText());
        configuration.setScriptWorkingDirectory(myScriptWorkingDirectory.getText());
        configuration.setExecuteInTerminal(myExecuteScriptInTerminal.isSelected());
        configuration.setEnvData(myScriptEnvComponent.getEnvData());
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return myPanel;
    }
}