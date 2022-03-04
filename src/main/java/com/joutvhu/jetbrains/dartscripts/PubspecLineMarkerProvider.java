package com.joutvhu.jetbrains.dartscripts;

import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.joutvhu.jetbrains.dartscripts.action.PubspecRunScriptAction;
import com.joutvhu.jetbrains.dartscripts.util.DartBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLScalar;
import org.jetbrains.yaml.psi.impl.YAMLBlockMappingImpl;

public class PubspecLineMarkerProvider extends RunLineMarkerContributor {
    public static String SCRIPT_KEY = "scripts";
    public static String PUBSPEC_FILE = "pubspec.yaml";

    @Override
    public Info getInfo(@NotNull PsiElement element) {
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

    private Info createMarkerInfo(YAMLKeyValue element) {
        String scriptName = element.getKeyText();
        return new Info(AllIcons.Actions.Execute, new AnAction[]{
                new PubspecRunScriptAction(DartBundle.message("ds.action.name.run", scriptName), "", AllIcons.Actions.Execute)
        }, psiElement -> "");
    }
}
