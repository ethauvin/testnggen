package org.intellij.plugins.testnggen.diff;

import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.DimensionService;
import org.intellij.plugins.testnggen.FileCreator;
import org.intellij.plugins.testnggen.GeneratorContext;
import org.intellij.plugins.testnggen.util.GenUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.StringWriter;


public final class DiffViewerFrame extends JFrame implements DataProvider {
    private static final String DIMENSION_KEY = "DiffViewer.JFrame";
    private Project _project = null;
    private DiffPanelHelper _diffPanelHelper = null;
    private final GeneratorContext _ctx;
    private final String _content;

    public DiffViewerFrame(Project project /*, VirtualFile backingFile */, DiffFileToken paneOneToken,
        DiffFileToken paneTwoToken, GeneratorContext ctx) {
        setProject(project);
        _diffPanelHelper = new DiffPanelHelper(this, project, paneOneToken, paneTwoToken);

        _ctx = ctx;
        _content = paneTwoToken.getContents();
        setTitle(buildTitle());
        init();
    }

    public Project getProject() {
        return _project;
    }

    public void setProject(Project project) {
        _project = project;
    }

    public Object getData(String dataId) {
        if (DataConstants.PROJECT.equals(dataId)) {
            return _project;
        }
        else {
            return null;
        }
    }

    public void dispose() {
        _diffPanelHelper.closeDiffPanel();
        super.dispose();
    }

    private void init() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setIconImage(new ImageIcon(getClass().getResource("/diff/Diff.png")).getImage());

        getRootPane().registerKeyboardAction(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        addWindowListener(new WindowAdapter() {
                public void windowClosed(WindowEvent e) {
                    final DimensionService dimensionService = DimensionService.getInstance();
                    dimensionService.setSize(DIMENSION_KEY, getSize());
                    dimensionService.setLocation(DIMENSION_KEY, getLocation());
                }
            });

        final Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(8, 0));
        contentPane.add(createCenterPanel(), BorderLayout.CENTER);
        contentPane.add(createButtonPanel(), BorderLayout.SOUTH);

        pack();

        final DimensionService dimensionService = DimensionService.getInstance();
        final Dimension size = dimensionService.getSize(DIMENSION_KEY);

        if (size != null) {
            setSize(size);
        }

        final Point location = dimensionService.getLocation(DIMENSION_KEY);

        if (location != null) {
            setLocation(location);
        }
        else {
            setLocationRelativeTo(null);
        }
    }

    JComponent createButtonPanel() {
        final JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        final Action[] actions = createActions();

        for (int index = 0; index < actions.length; index++) {
            panel.add(new JButton(actions[index]));
        }

        return panel;
    }

    JComponent createCenterPanel() {
        return _diffPanelHelper.getDiffPanel().getComponent();
    }

    Action[] createActions() {
        final Action closeAction =
            new AbstractAction("Close") {
                public void actionPerformed(ActionEvent actionevent) {
                    DiffViewerFrame.this.setVisible(false);
                    DiffViewerFrame.this.dispose();
                }
            };

        final Action overwriteAction =
            new AbstractAction("Overwrite") {
                public void actionPerformed(ActionEvent actionevent) {
                    final StringWriter writer = new StringWriter();
                    writer.write(_content);

                    try {
                        ApplicationManager.getApplication().runWriteAction(new FileCreator(GenUtil.getOutputFile(_ctx,
                                    _ctx.getOutputFileName()), writer, _ctx, true));
                        DiffViewerFrame.this.setVisible(false);
                        DiffViewerFrame.this.dispose();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };

        return new Action[] { overwriteAction, closeAction };
    }

    private String buildTitle() {
        return new StringBuffer("Diff: ").append(_diffPanelHelper.getTitle()).toString();
    }
}
