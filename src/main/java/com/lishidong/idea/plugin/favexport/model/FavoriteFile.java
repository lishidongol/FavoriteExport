package com.lishidong.idea.plugin.favexport.model;

import com.intellij.openapi.vfs.VirtualFile;

public class FavoriteFile {

    private String category;
    private String module;
    private VirtualFile file;

    public FavoriteFile(String category, String module, VirtualFile file) {
        this.category = category;
        this.module = module;
        this.file = file;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public VirtualFile getFile() {
        return file;
    }

    public void setFile(VirtualFile file) {
        this.file = file;
    }
}
