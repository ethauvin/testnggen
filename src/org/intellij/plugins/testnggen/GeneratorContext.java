package org.intellij.plugins.testnggen;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;


/**
 * Data holder/distributer object.
 *
 * @author Alex Nazimok (SCI)
 * @since <pre>Sep 3, 2003</pre>
 */
public class GeneratorContext {
    private final DataContext _ctx;
    private final PsiJavaFile _file;
    private final PsiClass _psiClass;
    private String _outputFileName;

    public GeneratorContext(DataContext ctx, PsiJavaFile file, PsiClass psiClass) {
        _ctx = ctx;
        _file = file;
        _psiClass = psiClass;
    }

    public final DataContext getDataContext() {
        return _ctx;
    }

    public final PsiJavaFile getFile() {
        return _file;
    }

    public final PsiClass getPsiClass() {
        return _psiClass;
    }

    public final String getPackageName() {
        return _file.getPackageName();
    }

    public final String getClassName(boolean qualified) {
        if (!qualified) {
            return _psiClass.getName();
        }
        else {
            return _psiClass.getQualifiedName();
        }
    }

    public final String getOutputFileName() {
        return _outputFileName;
    }

    public final void setOutputFileName(String outputFileName) {
        _outputFileName = outputFileName;
    }
}
