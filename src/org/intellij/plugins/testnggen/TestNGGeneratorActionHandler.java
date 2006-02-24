package org.intellij.plugins.testnggen;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.psi.*;
import com.intellij.pom.java.LanguageLevel;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.intellij.plugins.testnggen.util.GenUtil;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 * This is where the magic happens.
 *
 * @author Alex Nazimok (SCI)
 * @author <a href="mailto:erik@thauvin.net">Erik C. Thauvin</a>
 * @since <pre>Sep 3, 2003</pre>
 * @noinspection InnerClassMayBeStatic
 */
public class TestNGGeneratorActionHandler extends EditorWriteActionHandler {
    private static final Logger logger = GenUtil.getLogger(TestNGGeneratorActionHandler.class.getName());
    private List entryList;
    private GeneratorContext genCtx;

    /**
     * Executed upon action in the Editor
     *
     * @param editor      IDEA Editor
     * @param dataContext DataCOntext
     */
    public final void executeWriteAction(Editor editor, DataContext dataContext) {
        final PsiJavaFile file = GenUtil.getSelectedJavaFile(dataContext);

        if (file == null) {
            return;
        }

        final PsiClass[] psiClasses = file.getClasses();

        if (psiClasses == null) {
            return;
        }

        for (int i = 0; i < psiClasses.length; i++) {
            if ((psiClasses[i] != null) && (psiClasses[i].getQualifiedName() != null)) {
                genCtx = new GeneratorContext(dataContext, file, psiClasses[i]);
                entryList = new ArrayList(0);

                try {
                    if (psiClasses[i] == null) {
                        return;
                    }

                    if (!psiClasses[i].isInterface()) {
                        final List methodList = new ArrayList(0);
                        final List fieldList = new ArrayList(0);

                        buildMethodList(psiClasses[i].getMethods(), methodList);
                        buildFieldList(psiClasses[i].getFields(), fieldList);

                        final PsiClass[] innerClass = psiClasses[i].getAllInnerClasses();

                        for (int idx = 0; idx < innerClass.length; idx++) {
                            buildMethodList(innerClass[idx].getMethods(), methodList);
                            buildFieldList(psiClasses[i].getFields(), fieldList);
                        }

                        entryList.add(new TemplateEntry(genCtx.getClassName(false),
                                                        genCtx.getPackageName(),
                                                        methodList,
                                                        fieldList));
                        process();
                    }
                }
                catch (Exception e) {
                    GenUtil.getLogger(getClass().getName()).error(e);
                }
            }
        }
    }

    /**
     * Builds a list of class scope fields from an array of PsiFields
     * @param fields an array of fields
     * @param fieldList list to be populated
     */
    private static void buildFieldList(PsiField[] fields, List fieldList) {
        for(int i = 0; i < fields.length; i++){
            fieldList.add(fields[i].getName());
        }
    }

    /**
     * Builds method List from an array of PsiMethods
     *
     * @param methods    array of methods
     * @param methodList list to be populated
     */
    private static void buildMethodList(PsiMethod[] methods, List methodList) {
        for (int j = 0; j < methods.length; j++) {
            if (!methods[j].isConstructor()) {
                final PsiModifierList modifiers = methods[j].getModifierList();

                if (!modifiers.hasModifierProperty("private")) {
                    final String methodName = methods[j].getName();

                    if (!methodList.contains(methodName)) {
                        methodList.add(methodName);
                    }
                    else {
                        for (int k = 1; k < methods.length; k++) {
                            if (!methodList.contains(methodName + k)) {
                                methodList.add(methodName + k);

                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Sets all the needed vars in VelocityContext and
     * merges the template
     */
    private void process() {
        try {
            final Properties velocityProperties = new Properties();
            velocityProperties.setProperty(VelocityEngine.RESOURCE_LOADER, Const.RESOURCE_LOADER_TYPE);
            velocityProperties.setProperty(Const.RESOURCE_LOADER_CLASS_KEY, Const.RESOURCE_LOADER_CLASS_VALUE);
            velocityProperties.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH,
                    GenUtil.getResourcePath(Const.RELATIVE_DIR_NAME));

            final VelocityContext context = new VelocityContext();
            context.put(Const.ENTRY_LIST_VAR_NAME, entryList);
            context.put(Const.TODAY_VAR_NAME, GenUtil.formatDate("MM/dd/yyyy"));
            context.put(Const.TODAY_LONG_VAR_NAME, GenUtil.formatDate("MMMM d, yyyy"));
            context.put(Const.AUTHOR_VAR_NAME, System.getProperty("user.name", ""));

            final LanguageLevel level = PsiManager.getInstance(GenUtil.getProject(genCtx.getDataContext())).getEffectiveLanguageLevel();
            final boolean hasAnnotations = !(level == LanguageLevel.JDK_1_4 || level == LanguageLevel.JDK_1_3);
            context.put(Const.HAS_ANNOTATIONS_VAR_NAME, Boolean.valueOf(hasAnnotations));


            final VelocityEngine ve = new VelocityEngine();
            ve.init(velocityProperties);

            final Template template = ve.getTemplate(Const.TEMPLATE_NAME);
            final StringWriter writer = new StringWriter();
            template.merge(context, writer);
            genCtx.setOutputFileName((String) context.get(Const.CLASS_NAME_VAR));
            ApplicationManager.getApplication().runWriteAction(new FileCreator(GenUtil.getOutputFile(genCtx,
                    genCtx.getOutputFileName()), writer, genCtx, false));
        }
        catch (Exception e) {
            logger.error(e);
        }
    }

    /**
     * DataHolder class. Needs to be public since velocity is using it in the
     * template.
     * @noinspection CanBeStatic,PublicInnerClass,NonStaticInnerClassInSecureContext
     */
    public class TemplateEntry {
        private List _methodList = new ArrayList(0);
        private List _fieldList = new ArrayList(0);

        private final String _className;
        private final String _packageName;


        public TemplateEntry(String className, String packageName, List methodList, List fieldList) {
            _className = className;
            _packageName = packageName;
            _methodList = methodList;
            _fieldList = fieldList;
        }

        public final String getClassName() {
            return _className;
        }

        public final String getPackageName() {
            return _packageName;
        }

        public final List getMethodList() {
            return _methodList;
        }

        public final List getFieldList() {
            return _fieldList;
        }
    }
}
