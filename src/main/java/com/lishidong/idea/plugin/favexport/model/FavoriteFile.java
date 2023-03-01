package com.lishidong.idea.plugin.favexport.model;

import java.io.Serializable;

public class FavoriteFile implements Serializable {

    private static final long serialVersionUID = 1L;

    private String category;
    private String module;
    private String filename;
    private String filepath;

    public FavoriteFile() {
    }

    public FavoriteFile(String category, String module, String filename, String filepath) {
        this.category = category;
        this.module = module;
        this.filename = filename;
        this.filepath = filepath;
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

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }
}
