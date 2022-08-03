package com.joutvhu.intellij.dartscripts;

import com.intellij.codeInsight.daemon.MergeableLineMarkerInfo;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.MarkupEditorFilter;
import com.intellij.openapi.editor.markup.MarkupEditorFilterFactory;
import com.intellij.psi.PsiElement;
import com.intellij.util.Function;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

public class RunLineMarkerInfo extends MergeableLineMarkerInfo<PsiElement> {
    private final DefaultActionGroup myActionGroup;
    private final AnAction mySingleAction;

    RunLineMarkerInfo(PsiElement element, Icon icon, DefaultActionGroup actionGroup) {
        this(element, icon, psiElement -> "", actionGroup);
    }

    RunLineMarkerInfo(PsiElement element, Icon icon, Function<? super PsiElement, @Nls String> tooltipProvider, DefaultActionGroup actionGroup) {
        super(element, element.getTextRange(), icon, tooltipProvider, null, GutterIconRenderer.Alignment.CENTER, () -> tooltipProvider.fun(element));
        myActionGroup = actionGroup;
        if (myActionGroup.getChildrenCount() == 1) {
            mySingleAction = myActionGroup.getChildActionsOrStubs()[0];
            myActionGroup.setPopup(false);
        } else {
            mySingleAction = null;
            myActionGroup.setPopup(true);
        }
    }

    @Override
    public GutterIconRenderer createGutterRenderer() {
        return new LineMarkerGutterIconRenderer<>(this) {
            @Override
            public AnAction getClickAction() {
                return mySingleAction;
            }

            @Override
            public boolean isNavigateAction() {
                return true;
            }

            @Override
            public ActionGroup getPopupMenuActions() {
                return myActionGroup.isPopup() ? myActionGroup : null;
            }
        };
    }

    @NotNull
    @Override
    public MarkupEditorFilter getEditorFilter() {
        return MarkupEditorFilterFactory.createIsNotDiffFilter();
    }

    @Override
    public boolean canMergeWith(@NotNull MergeableLineMarkerInfo<?> info) {
        return info instanceof RunLineMarkerInfo && info.getIcon() == getIcon();
    }

    @Override
    public Icon getCommonIcon(@NotNull List<? extends MergeableLineMarkerInfo<?>> infos) {
        return getIcon();
    }
}
