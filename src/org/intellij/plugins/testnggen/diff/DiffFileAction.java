package org.intellij.plugins.testnggen.diff;

import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.actionSystem.DataConstants;
import org.intellij.plugins.testnggen.GeneratorContext;

import java.io.IOException;


public final class DiffFileAction {
    /**
	 * Disables the default constructor.
	 *
	 * @throws UnsupportedOperationException if the constructor is called.
	 */
	private DiffFileAction() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Illegal constructor call.");
	}

    public static void showDiff(String paneTwoContents, VirtualFile paneTwoFile, GeneratorContext genCtx) {
        final Project project =
            (Project) genCtx.getDataContext().getData(DataConstants.PROJECT);

        if (project != null) {
            if ((paneTwoFile != null) && (paneTwoContents != null)) {
                DiffFileToken paneOneToken = null;

                try {
                    paneOneToken = DiffFileToken.createForVirtualFile(paneTwoFile);
                }
                catch (IOException e) {
                    System.err.println(new StringBuffer("DiffFile plugin: could not read selected file: ").append(
                            paneTwoFile.getPath()).toString());
                    e.printStackTrace();

                    Messages.showDialog(project, "Could not read contents of selected file.", // message
                        "Diff File Error", // title
                        new String[] { "Dismiss" }, 0, Messages.getErrorIcon());

                    return;
                }

                if (paneOneToken != null) {
                    final DiffFileToken paneTwoToken = new DiffFileToken();

                    paneTwoToken.setName("Editor Contents");
                    paneTwoToken.setTitle("Temporary Buffer");
                    paneTwoToken.setContents(paneTwoContents);
                    paneTwoToken.setType(FileTypeManager.getInstance().getFileTypeByExtension(paneTwoFile.getExtension()));

                    final DiffViewerFrame diffViewer =
                        new DiffViewerFrame(project /*, paneTwoFile */ , paneOneToken, paneTwoToken, genCtx);

                    diffViewer.setVisible(true);
                }
            }
        }
    }
}
