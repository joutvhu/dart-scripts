package com.joutvhu.intellij.dartscripts;

import com.intellij.codeInsight.intention.PriorityAction;
import com.intellij.execution.ExecutorRegistryImpl;
import com.intellij.execution.actions.RunContextAction;
import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.ActionWithDelegate;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.ReflectionUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
        Presentation presentation = getTemplatePresentation();
        Presentation originPresentation = myOrigin.getTemplatePresentation();
        if (!(myOrigin instanceof ActionGroup)) {
            presentation.setPerformGroup(true);
            presentation.setPopupGroup(true);
            presentation.setHideGroupIfEmpty(originPresentation.isHideGroupIfEmpty());
            presentation.setDisableGroupIfEmpty(originPresentation.isDisableGroupIfEmpty());
        } else {
            presentation.setPopupGroup(originPresentation.isPopupGroup());
            presentation.setHideGroupIfEmpty(false);
            presentation.setDisableGroupIfEmpty(false);
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return myOrigin.getActionUpdateThread();
    }

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        if (myOrigin instanceof ExecutorAction executorAction) {
            AnAction originAction = executorAction.getOrigin();
            if (originAction instanceof ExecutorRegistryImpl.ExecutorGroupActionGroup actionGroup) {
                final AnAction[] children = getChildren(actionGroup, null);
                logger.assertTrue(ContainerUtil.all(Arrays.asList(children), RunContextAction.class::isInstance));
                return ContainerUtil.mapNotNull(children, o -> {
                    PsiElement element = myElement.getElement();
                    return element != null ? new LineMarkerActionWrapper(element, o) : null;
                }).toArray(AnAction.EMPTY_ARRAY);
            }
        }
        if (myOrigin instanceof ActionGroup actionGroup) {
            return getChildren(actionGroup, e == null ? null : wrapEvent(e));
        }
        return AnAction.EMPTY_ARRAY;
    }

    public AnAction @NotNull [] getChildren(@NotNull ActionGroup actionGroup, @Nullable AnActionEvent e) {
        try {
            Method getChildren = ReflectionUtil.getMethod(actionGroup.getClass(), "getChildren", AnActionEvent.class);
            if (getChildren == null) return AnAction.EMPTY_ARRAY;
            return (AnAction[]) getChildren.invoke(actionGroup, e);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            return AnAction.EMPTY_ARRAY;
        }
    }

    @Override
    public boolean isDumbAware() {
        return myOrigin.isDumbAware();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        AnActionEvent wrapped = wrapEvent(e);
        myOrigin.update(wrapped);
        Icon icon = wrapped.getPresentation().getIcon();
        if (icon != null) {
            e.getPresentation().setIcon(icon);
        }
    }

    @NotNull
    private AnActionEvent wrapEvent(@NotNull AnActionEvent e) {
        return e.withDataContext(wrapContext(e.getDataContext()));
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
