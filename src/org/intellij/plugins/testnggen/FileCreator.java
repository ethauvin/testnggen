package org.intellij.plugins.testnggen;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.psi.PsiJavaFile;
import org.intellij.plugins.testnggen.diff.DiffFileAction;
import org.intellij.plugins.testnggen.util.GenUtil;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;


/**
 * Responsible for writing test case out to the file and bringing
 * new editor window up.
 * Must implement runnable since we are using Application.runWriteAction in
 * TestNGGeneratorAction to refresh the content of the VirtualFileSystem.
 *
 * @author Alex Nazimok (SCI)
 * @author <a href="mailto:erik@thauvin.net">Erik C. Thauvin</a>
 * @since <pre>Sep 1, 2003</pre>
 */
public class FileCreator implements Runnable {
    private final String _outputFile;
    private final StringWriter _writer;
    private final DataContext _ctx;
    private final PsiJavaFile _file;
    private final GeneratorContext _genCtx;
    private final boolean _overwrite;

    /**
     * Default constructor
     * @param outputFile output file name
     * @param writer holds the content of the file
     * @param genCtx generator context
     */
    public FileCreator(String outputFile, StringWriter writer, GeneratorContext genCtx, boolean overwrite) {
        _outputFile = outputFile + ".java";
        _writer = writer;
        _ctx = genCtx.getDataContext();
        _file = genCtx.getFile();
        _genCtx = genCtx;
        _overwrite = overwrite;
    }

     /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see     java.lang.Thread#run()
     */
    public final void run() {
        final File newFile = new File(_outputFile);
        int overwriteInd = JOptionPane.NO_OPTION;

        if (!newFile.getParentFile().exists()) {
            newFile.getParentFile().mkdirs();
        }

        if (newFile.exists() && !_overwrite) {
            overwriteInd =
                JOptionPane.showOptionDialog(null, Const.OVERWRITE_MSG, "View the difference?",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
        }

        if (JOptionPane.NO_OPTION == overwriteInd) {
            FileWriter w = null;
            try {
                w = new FileWriter(newFile);
                w.write(_writer.toString());
                w.flush();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if (w != null) {
                    try {
                        w.close();
                    }
                    catch (IOException ignore) {
                       ; // Do nothing
                    }
                }
            }

            //Refresh and open new file in the editor
            final Project project = GenUtil.getProject(_ctx);
            final VirtualFileSystem vfs = _file.getVirtualFile().getFileSystem();

            GenUtil.getLogger(getClass().getName()).info("OutputFile: " + _outputFile);
            final VirtualFile fileToOpen = vfs.refreshAndFindFileByPath(_outputFile);

            if(fileToOpen != null) {
                FileEditorManager.getInstance(project).openFile(fileToOpen, true);
                try {
                    // Demetra doesn't support this call, for some reason
                    vfs.forceRefreshFile(fileToOpen);
                }
                catch (NoSuchMethodError ignore) {
                    ; // Do nothing.
                }
            }
            else {
                throw new IllegalArgumentException("Unable to find: " + _outputFile);
            }
        }

        else if (JOptionPane.YES_OPTION == overwriteInd) {
            final VirtualFileSystem vfs = _file.getVirtualFile().getFileSystem();
            DiffFileAction.showDiff(_writer.toString(), vfs.findFileByPath(_outputFile), _genCtx);
        }
    }
}
