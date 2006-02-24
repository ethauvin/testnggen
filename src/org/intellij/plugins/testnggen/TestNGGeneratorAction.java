package org.intellij.plugins.testnggen;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.psi.PsiJavaFile;
import org.intellij.plugins.testnggen.util.GenUtil;


/**
 * TestNGGenerator action implementation
 * @author Alex Nazimok (SCI)
 * @since <pre>Aug 28, 2003</pre>
 */
public class TestNGGeneratorAction extends EditorAction {
    public TestNGGeneratorAction() {
        super(new TestNGGeneratorActionHandler());
    }

    /**
     * Enables Generate popup for Java files only.
     * @param editor
     * @param presentation
     * @param dataContext
     */
    public final void update(Editor editor, Presentation presentation, DataContext dataContext) {
        final PsiJavaFile javaFile = GenUtil.getSelectedJavaFile(dataContext);
        presentation.setEnabled(javaFile != null);
    }
}
