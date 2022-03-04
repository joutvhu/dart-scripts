package com.joutvhu.jetbrains.dartscripts.action;

import com.intellij.execution.Executor;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.openapi.util.NlsActions;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class PubspecDebugScriptAction extends PubspecRunScriptAction {
    public static final String ID = "debugPubspecScriptAction";

    public PubspecDebugScriptAction(
            @Nullable @NlsActions.ActionText String text,
            @Nullable @NlsActions.ActionDescription String description,
            @Nullable Icon icon
    ) {
        super(text, description, icon);
    }

    @Override
    protected Executor getExecutor() {
        return DefaultDebugExecutor.getDebugExecutorInstance();
    }
}
