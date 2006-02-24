package org.intellij.plugins.testnggen;

import com.intellij.openapi.components.ApplicationComponent;


/**
 * ApplicationComponent implementation
 * @author Alex Nazimok (SCI)
 * @since <pre>Aug 29, 2003</pre>
 */
public class TestNGGenerator implements ApplicationComponent {
    /**
     * Method is called after plugin is already created and configured. Plugin can start to communicate with
     * other plugins only in this method.
     */
    public final void initComponent() {}

    /**
     * This method is called on plugin disposal.
     */
    public final void disposeComponent() {}

    /**
     * Returns the name of component
     * @return String representing component name. Use plugin_name.component_name notation.
     */
    public final String getComponentName() {
        return "TestNGGenerator.TestNGGenerator";
    }
}
