package com.joutvhu.intellij.dartscripts.action;

import com.intellij.execution.Executor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.joutvhu.intellij.dartscripts.LineMarkerActionWrapper;
import com.joutvhu.intellij.dartscripts.PubspecLineMarkerProvider;
import com.joutvhu.intellij.dartscripts.run.PubspecTerminalHelper;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import javax.swing.*;
import java.nio.file.Paths;
import java.util.Map;

public class PubspecRunScriptAction extends AnAction {
    public static final String ID = "runPubspecScriptAction";
    public static final String DIRECTORY_KEY = "directory";
    public static final String TERMINAL_KEY = "terminal";

    public PubspecRunScriptAction(
            @Nullable @Nls(capitalization = Nls.Capitalization.Title) String text,
            @Nullable @Nls(capitalization = Nls.Capitalization.Sentence) String description,
            @Nullable Icon icon
    ) {
        super(text, description, icon);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getData(CommonDataKeys.PROJECT);
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        YAMLKeyValue element = getElement(e);
        if (project != null && file instanceof YAMLFile && element != null) {
            Map<String, String> params = PubspecLineMarkerProvider.getActionParams(element);
            if (params != null) {
                String scriptText = params.get(PubspecLineMarkerProvider.SCRIPT_TEXT_KEY);
                if (scriptText != null) {
                    String workingDirectory = file.getVirtualFile().getParent().getPath();
                    if (params.containsKey(DIRECTORY_KEY)) {
                        workingDirectory = Paths.get(workingDirectory)
                                .resolve(params.get(DIRECTORY_KEY))
                                .normalize()
                                .toAbsolutePath()
                                .toString();
                    }
                    Boolean executeInTerminal = null;
                    if (params.containsKey(TERMINAL_KEY)) {
                        String terminalOption = params.get(TERMINAL_KEY);
                        if ("true".equalsIgnoreCase(terminalOption) || "yes".equalsIgnoreCase(terminalOption))
                            executeInTerminal = true;
                        if ("false".equalsIgnoreCase(terminalOption) || "no".equalsIgnoreCase(terminalOption))
                            executeInTerminal = false;
                    }

                    PubspecTerminalHelper.runScript(
                            this.getExecutor(),
                            e.getData(CommonDataKeys.PROJECT),
                            element.getKeyText(),
                            scriptText,
                            workingDirectory,
                            executeInTerminal);
                }
            }
        }
    }

    protected Executor getExecutor() {
        return DefaultRunExecutor.getRunExecutorInstance();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        boolean enable = isEnabled(e);
        e.getPresentation().setEnabledAndVisible(enable);
    }

    private static boolean isEnabled(@NotNull AnActionEvent e) {
        if (e.getProject() != null) {
            PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
            if (file instanceof YAMLFile)
                return getElement(e) != null;
        }
        return false;
    }

    private static YAMLKeyValue getElement(@NotNull AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        if (dataContext instanceof UserDataHolder) {
            Pair<PsiElement, ?> pair = ((UserDataHolder) dataContext)
                    .getUserData(LineMarkerActionWrapper.LOCATION_WRAPPER);
            if (pair != null && pair.first instanceof YAMLKeyValue)
                return (YAMLKeyValue) pair.first;
        }
        return null;
    }

    @Override
    @Nullable
    @Nls(capitalization = Nls.Capitalization.Title)
    public String getTemplateText() {
        return super.getTemplateText();
    }
}
