package com.lishidong.idea.plugin.favexport.model;

import java.io.Serializable;

public class ExportClass implements Serializable {

    private static final long serialVersionUID = 1L;

    private String className;
    private String classPackage;
    private String classPath;

    public ExportClass(String className, String classPackage, String classPath) {
        this.className = className;
        this.classPackage = classPackage;
        this.classPath = classPath;
    }

    public ExportClass() {
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassPackage() {
        return classPackage;
    }

    public void setClassPackage(String classPackage) {
        this.classPackage = classPackage;
    }

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }
}
