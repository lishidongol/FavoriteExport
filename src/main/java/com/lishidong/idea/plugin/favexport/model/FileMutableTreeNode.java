package com.lishidong.idea.plugin.favexport.model;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.tree.DefaultMutableTreeNode;

public class FileMutableTreeNode extends DefaultMutableTreeNode {

    public String category;
    public String module;
    public VirtualFile file;

    public FileMutableTreeNode(String category) {
        this.category = category;
    }

    public FileMutableTreeNode(String category, String module) {
        this.category = category;
        this.module = module;
    }

    public FileMutableTreeNode(String category, String module, VirtualFile file) {
        this.category = category;
        this.module = module;
        this.file = file;
    }

    public String getFileName() {
        return this.file.getName();
    }

    public String getFilepath() {
        return this.getFilepath();
    }

    @Override
    public String toString() {
        if (file != null) {
            return file.getName();
        }
        else if (!StringUtil.isEmptyOrSpaces(module)) {
            return module;
        }
        else {
            return category;
        }
    }
}
