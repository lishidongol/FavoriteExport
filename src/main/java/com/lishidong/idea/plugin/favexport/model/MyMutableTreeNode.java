package com.lishidong.idea.plugin.favexport.model;

import javax.swing.tree.DefaultMutableTreeNode;

public class MyMutableTreeNode extends DefaultMutableTreeNode {

    private String nodeName;

    public MyMutableTreeNode(String nodeName,Object object) {
        super(object,true);
        this.nodeName = nodeName;
    }

    @Override
    public String toString() {
        return this.nodeName;
    }
}
