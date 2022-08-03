package com.joutvhu.intellij.dartscripts;

import com.intellij.codeInsight.intention.PriorityAction;
import com.intellij.execution.actions.RunContextAction;
import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;

public class LineMarkerActionWrapper extends ActionGroup implements PriorityAction, ActionWithDelegate<AnAction> {
    private static final Logger logger = Logger.getInstance(LineMarkerActionWrapper.class);

    public static final Key<Pair<PsiElement, DataContext>> LOCATION_WRAPPER = Key.create("LOCATION_WRAPPER");

    protected final SmartPsiElementPointer<PsiElement> myElement;
    protected final AnAction myOrigin;

    public LineMarkerActionWrapper(PsiElement element, @NotNull AnAction origin) {
        myElement = SmartPointerManager.createPointer(element);
        myOrigin = origin;
        copyFrom(origin);
    }

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        if (myOrigin instanceof ExecutorAction) {
            AnAction originAction = ((ExecutorAction) myOrigin).getOrigin();
            if (originAction instanceof ActionGroup) {
                final AnAction[] children = ((ActionGroup) originAction).getChildren(null);
                logger.assertTrue(ContainerUtil.all(Arrays.asList(children), o -> o instanceof RunContextAction));
                return ContainerUtil.mapNotNull(children, o -> {
                    PsiElement element = myElement.getElement();
                    return element != null ? new LineMarkerActionWrapper(element, o) : null;
                }).toArray(AnAction.EMPTY_ARRAY);
            }
        }
        if (myOrigin instanceof ActionGroup) {
            return ((ActionGroup) myOrigin).getChildren(e == null ? null : wrapEvent(e));
        }
        return AnAction.EMPTY_ARRAY;
    }

    @Override
    public boolean isDumbAware() {
        return myOrigin.isDumbAware();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        AnActionEvent wrapped = wrapEvent(e);
        myOrigin.update(wrapped);
        Presentation presentation = wrapped.getPresentation();
        if (myOrigin instanceof ActionGroup) {
            Presentation originPresentation = myOrigin.getTemplatePresentation();
            presentation.setPerformGroup(originPresentation.isPerformGroup());
            presentation.setPopupGroup(originPresentation.isPopupGroup());
            presentation.setHideGroupIfEmpty(originPresentation.isHideGroupIfEmpty());
        } else {
            presentation.setPerformGroup(true);
            presentation.setPopupGroup(true);
            presentation.setHideGroupIfEmpty(true);
        }
        Icon icon = presentation.getIcon();
        if (icon != null) {
            getTemplatePresentation().setIcon(icon);
        }
    }

    @NotNull
    protected AnActionEvent wrapEvent(@NotNull AnActionEvent e) {
        DataContext dataContext = wrapContext(e.getDataContext());
        return new AnActionEvent(e.getInputEvent(), dataContext, e.getPlace(), e.getPresentation(), e.getActionManager(), e.getModifiers());
    }

    @NotNull
    protected DataContext wrapContext(DataContext dataContext) {
        Pair<PsiElement, DataContext> pair = DataManager.getInstance()
                .loadFromDataContext(dataContext, LOCATION_WRAPPER);
        PsiElement element = myElement.getElement();
        if (pair == null || pair.first != element) {
            pair = Pair.pair(element, dataContext);
            DataManager.getInstance()
                    .saveInDataContext(dataContext, LOCATION_WRAPPER, pair);
        }
        return dataContext;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        myOrigin.actionPerformed(wrapEvent(e));
    }

    @NotNull
    @Override
    public Priority getPriority() {
        return Priority.TOP;
    }

    @NotNull
    @Override
    public AnAction getDelegate() {
        return myOrigin;
    }
}
