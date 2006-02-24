package org.intellij.plugins.testnggen.diff;

import com.intellij.openapi.diff.DiffManager;
import com.intellij.openapi.diff.DiffPanel;
import com.intellij.openapi.diff.SimpleContent;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;

import java.awt.*;


/**
 * Class to add some convenience abilities to the DiffPanel.
 */
public final class DiffPanelHelper {
    private Project _project = null;
    private DiffFileToken _paneTwoToken = null;
    private DiffFileToken _paneOneToken = null;
    private DiffPanel _panel = null;
    private Window _window = null;

    /**
     * Create a new DiffPanelHelper for the specified window, project, backing file, and contents.
     * The virtual file is used when a line number is selected.
     *
     * @param window       The window that will contain the diffPanel.
     * @param project      The project this window is associated with.
     * @param paneOneToken A token containing information about the file to be placed in the left
     *                     hand pane.
     * @param paneTwoToken A token containing information about the file to be placed in the right
     */
    public DiffPanelHelper(Window window, Project project, DiffFileToken paneOneToken, DiffFileToken paneTwoToken) {
        _window = window;
        _project = project;
        _paneTwoToken = paneTwoToken;
        _paneOneToken = paneOneToken;
    }

    public String getTitle() {
        return new StringBuffer(_paneOneToken.getName()).
                append(" vs. ").append(_paneTwoToken.getName()) .toString();
    }

    public DiffPanel getDiffPanel() {
        initDiffPanel();

        return _panel;
    }

    public void closeDiffPanel() {
        if (_panel != null) {
            _panel.dispose();
            _panel = null;
        }
    }

    private void initDiffPanel() {
        if (_panel == null) {
            _panel = DiffManager.getInstance().createDiffPanel(_window, _project);

            FileType fileType = _paneTwoToken.getType();

            if (fileType == null) {
                fileType = _paneOneToken.getType();
            }

            _panel.setTitle1(_paneOneToken.getTitle());
            _panel.setTitle2(_paneTwoToken.getTitle());
            _panel.setContents(new SimpleContent(_paneOneToken.getContents(), fileType),
                    new SimpleContent(_paneTwoToken.getContents(), fileType));
        }
    }

}
