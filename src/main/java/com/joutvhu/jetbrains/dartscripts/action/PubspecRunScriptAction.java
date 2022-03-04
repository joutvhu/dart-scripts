package com.joutvhu.jetbrains.dartscripts.action;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.lineMarker.LineMarkerActionWrapper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsActions;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.joutvhu.jetbrains.dartscripts.run.PubspecTerminalHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import javax.swing.*;

public class PubspecRunScriptAction extends AnAction {
    public static final String ID = "runPubspecScriptAction";

    public PubspecRunScriptAction(
            @Nullable @NlsActions.ActionText String text,
            @Nullable @NlsActions.ActionDescription String description,
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
            PubspecTerminalHelper.runScript(
                    this.getExecutor(),
                    e.getData(CommonDataKeys.PROJECT),
                    element.getKeyText(),
                    element.getValueText(),
                    file.getVirtualFile().getParent().getPath());
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
        if (e.getDataContext() instanceof UserDataHolderBase) {
            UserDataHolderBase userDataHolderBase = (UserDataHolderBase) e.getDataContext();
            Pair<PsiElement, ?> pair = userDataHolderBase.getUserData(LineMarkerActionWrapper.LOCATION_WRAPPER);
            if (pair != null && pair.first instanceof YAMLKeyValue)
                return (YAMLKeyValue) pair.first;
        }
        return null;
    }

    @Override
    public @Nullable @NlsActions.ActionText String getTemplateText() {
        return super.getTemplateText();
    }
}
