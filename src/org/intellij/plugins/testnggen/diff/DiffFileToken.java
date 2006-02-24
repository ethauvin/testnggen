package org.intellij.plugins.testnggen.diff;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;


/**
 * A basic Java bean that holds information about a file that is relevant for a diff viewer.
 */
public final class DiffFileToken {
    private String _name;
    private String _title;
    private String _contents;
    private FileType _type;

    public DiffFileToken() {}

    public DiffFileToken(String name, String title, String fileContents, FileType fileType) {
        _name = name;
        _title = title;
        _contents = fileContents;
        _type = fileType;
    }

    /**
     * Creates a token based upon the specified virtual file. The file's path is used for the name,
     * the contents are extracted, and the file type is inferred from the file's extension.
     *
     * @param file  The file the token will be based upon.
     * @return  A new DiffFileToken.
     * @throws IOException  If the contents of the virtual file can not be read.
     */
    public static DiffFileToken createForVirtualFile(VirtualFile file)
        throws IOException {
        return new DiffFileToken(file.getName(), file.getPath(), String.valueOf(file.contentsToCharArray()),
            FileTypeManager.getInstance().getFileTypeByExtension(file.getExtension()));
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getTitle() {
        return _title;
    }

    public void setTitle(String title) {
        _title = title;
    }

    public String getContents() {
        return _contents;
    }

    public void setContents(String contents) {
        _contents = contents;
    }

    public FileType getType() {
        return _type;
    }

    public void setType(FileType type) {
        _type = type;
    }
}
