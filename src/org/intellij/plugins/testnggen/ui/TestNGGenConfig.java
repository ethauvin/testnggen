/*
 * Copyright (c) United Parcel Service of America, Inc.
 * All Rights Reserved.
 *                                                                                    
 * The use, disclosure, reproduction, modification, transfer, or transmittal          
 * of this work for any purpose in any form or by any means without the               
 * written permission of United Parcel Service is strictly prohibited.                
 * Confidential, Unpublished Property of United Parcel Service.                       
 * Use and Distribution Limited Solely to Authorized Personnel.                       
 */
package org.intellij.plugins.testnggen.ui;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.intellij.plugins.testnggen.Const;
import org.intellij.plugins.testnggen.util.GenUtil;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * TestNGGenerator configuration UI.
 * Could be accessed via File->Settings->(IDE Settings) TestNG Generator
 *
 * @author Alex Nazimok (SCI)
 * @since <pre>Sep 7, 2004</pre>
 */
public class TestNGGenConfig implements ApplicationComponent, Configurable {
    private Editor templateEditor;
    private Editor propsEditor;

    private String templateContent = "";
    private String propsContent = "";

    private static final String TEMPLATE_FILE = GenUtil.getResourcePath(Const.RELATIVE_DIR_NAME) + File.separator + Const.TEMPLATE_NAME;
    private static final String PROPS_FILE = GenUtil.getResourcePath(Const.RELATIVE_DIR_NAME) + File.separator + Const.PROPERTIES_FILE_NAME;

    public final String getDisplayName() {
        return "TestNG Generator";
    }

    public final Icon getIcon() {
        return new ImageIcon(TestNGGenConfig.class.getResource("smalllogo.gif"));
    }

    public final String getHelpTopic() {
        return null;
    }

    public final String getComponentName() {
        return "TestNGGeneratorPlugin.TestNGGenConfig";
    }

    public final void initComponent() {
        templateContent = readFile(TEMPLATE_FILE);
        propsContent = readFile(PROPS_FILE);
    }

    public final JComponent createComponent() {
        final JTabbedPane tabbedPane = new JTabbedPane();

        // test-case template configuration tab
        final EditorFactory factory = EditorFactory.getInstance();
        final char[] chars = new char[templateContent.length()];
        templateContent.getChars(0, templateContent.length(), chars, 0);

        templateEditor = factory.createEditor(factory.createDocument(chars));
        tabbedPane.addTab("Velocity Template", templateEditor.getComponent());

        templateEditor.getComponent().setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), TEMPLATE_FILE),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // Output Properties configuration tab
        propsEditor = factory.createEditor(factory.createDocument(propsContent));
        tabbedPane.addTab("Output Pattern", propsEditor.getComponent());

        propsEditor.getComponent().setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), PROPS_FILE),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        return tabbedPane;
    }

    public final boolean isModified() {
        return !templateContent.equals(templateEditor.getDocument().getText()) ||
                !propsContent.equals(propsEditor.getDocument().getText());
    }

    public final void apply() throws ConfigurationException {
        templateContent = templateEditor.getDocument().getText();
        propsContent = propsEditor.getDocument().getText();
    }

    private String readFile(String filename) {
        RandomAccessFile file = null;
        try {
            file  = new RandomAccessFile(filename, "r");
            final byte[] bytes = new byte[(int) file.length()];
            file.readFully(bytes);
            return new String(bytes);
        }
        catch (Exception ex) {
            GenUtil.getLogger(getClass().getName()).error(ex);
        }
        finally {
            if (file != null) {
                try {
                    file.close();
                }
                catch (IOException ignore) {
                    ; // Do nothing
                }
            }
        }
        throw new RuntimeException("Unable to read config files.");
    }

    private void writeFile(String filename, String content) {
        FileWriter file = null;
        try {
            file = new FileWriter(new File(filename), false);
            file.write(content);
        }
        catch (IOException e) {
            GenUtil.getLogger(getClass().getName()).error(e);
        }
        finally {
            if (file != null) {
                try {
                    file.close();
                }
                catch (IOException ignore) {
                    ; // Do nothing
                }
            }
        }

    }

    public final void disposeUIResources() {
        writeFile(TEMPLATE_FILE, templateContent);
        writeFile(PROPS_FILE, propsContent);
    }

    public final void disposeComponent() {
    }

    public final void reset() {
    }

}
