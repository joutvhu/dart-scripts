package com.joutvhu.intellij.dartscripts;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.MergeableLineMarkerInfo;
import com.intellij.codeInsight.intention.PriorityAction;
import com.intellij.execution.ExecutorRegistryImpl;
import com.intellij.execution.actions.RunContextAction;
import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.execution.lineMarker.RunLineMarkerProvider;
import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionWithDelegate;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.MarkupEditorFilter;
import com.intellij.openapi.editor.markup.MarkupEditorFilterFactory;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.joutvhu.intellij.dartscripts.action.PubspecRunScriptAction;
import com.joutvhu.intellij.dartscripts.util.DartBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLScalar;
import org.jetbrains.yaml.psi.impl.YAMLBlockMappingImpl;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

public class PubspecLineMarkerProvider extends RunLineMarkerProvider {
    public static final String SCRIPT_KEY = "scripts";
    public static final String PUBSPEC_FILE = "pubspec.yaml";
    public static final Key<Pair<PsiElement, DataContext>> LOCATION_WRAPPER = Key.create("LOCATION_WRAPPER");

    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        if (element instanceof YAMLKeyValue &&
                ((YAMLKeyValue) element).getValue() instanceof YAMLScalar &&
                element.getParent() instanceof YAMLBlockMappingImpl &&
                element.getParent().getParent() instanceof YAMLKeyValue) {
            YAMLKeyValue scriptsElement = (YAMLKeyValue) element.getParent().getParent();
            if (scriptsElement.getFirstChild() instanceof LeafPsiElement &&
                    SCRIPT_KEY.equals(scriptsElement.getFirstChild().getText()) &&
                    YAMLTokenTypes.SCALAR_KEY.equals(((LeafPsiElement) scriptsElement.getFirstChild()).getElementType())) {
                if (scriptsElement.getParent() != null && scriptsElement.getParent()
                        .getParent() instanceof YAMLDocument) {
                    YAMLDocument doc = (YAMLDocument) scriptsElement.getParent().getParent();
                    if (doc.getParent() instanceof YAMLFile) {
                        VirtualFile file = ((YAMLFile) doc.getParent()).getVirtualFile();
                        if (!file.isDirectory() && PUBSPEC_FILE.equals(file.getName())) {
                            return createMarkerInfo((YAMLKeyValue) element);
                        }
                    }
                }
            }
        }
        return null;
    }

    private LineMarkerInfo<?> createMarkerInfo(YAMLKeyValue element) {
        String scriptName = element.getKeyText();
        AnAction action = new PubspecRunScriptAction(DartBundle.message("ds.action.name.run", scriptName), "", AllIcons.Actions.Execute);
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new LineMarkerActionWrapper(element, action));
        return new RunLineMarkerInfo(element, AllIcons.Actions.Execute, actionGroup);
    }

    public static class RunLineMarkerInfo extends MergeableLineMarkerInfo<PsiElement> {
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
            } else {
                mySingleAction = null;
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
                    return myActionGroup;
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

    public static class LineMarkerActionWrapper extends ActionGroup implements PriorityAction, ActionWithDelegate<AnAction> {
        private static final Logger logger = Logger.getInstance(LineMarkerActionWrapper.class);
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
                if (((ExecutorAction)myOrigin).getOrigin() instanceof ExecutorRegistryImpl.ExecutorGroupActionGroup) {
                    final AnAction[] children = ((ExecutorRegistryImpl.ExecutorGroupActionGroup)((ExecutorAction)myOrigin).getOrigin()).getChildren(null);
                    logger.assertTrue(ContainerUtil.all(Arrays.asList(children), o -> o instanceof RunContextAction));
                    return ContainerUtil.mapNotNull(children, o -> {
                        PsiElement element = myElement.getElement();
                        return element != null ? new LineMarkerActionWrapper(element, o) : null;
                    }).toArray(AnAction.EMPTY_ARRAY);
                }
            }
            if (myOrigin instanceof ActionGroup) {
                return ((ActionGroup)myOrigin).getChildren(e == null ? null : wrapEvent(e));
            }
            return AnAction.EMPTY_ARRAY;
        }

        @Override
        public boolean canBePerformed(@NotNull DataContext context) {
            return !(myOrigin instanceof ActionGroup) || ((ActionGroup)myOrigin).canBePerformed(wrapContext(context));
        }

        @Override
        public boolean isDumbAware() {
            return myOrigin.isDumbAware();
        }

        @Override
        public boolean isPopup() {
            return !(myOrigin instanceof ActionGroup) || ((ActionGroup)myOrigin).isPopup();
        }

        @Override
        public boolean hideIfNoVisibleChildren() {
            return myOrigin instanceof ActionGroup && ((ActionGroup)myOrigin).hideIfNoVisibleChildren();
        }

        @Override
        public boolean disableIfNoVisibleChildren() {
            return !(myOrigin instanceof ActionGroup) || ((ActionGroup)myOrigin).disableIfNoVisibleChildren();
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            AnActionEvent wrapped = wrapEvent(e);
            myOrigin.update(wrapped);
            Icon icon = wrapped.getPresentation().getIcon();
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
                    .loadFromDataContext(dataContext, PubspecLineMarkerProvider.LOCATION_WRAPPER);
            PsiElement element = myElement.getElement();
            if (pair == null || pair.first != element) {
                pair = Pair.pair(element, dataContext);
                DataManager.getInstance().saveInDataContext(dataContext, PubspecLineMarkerProvider.LOCATION_WRAPPER, pair);
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
}
