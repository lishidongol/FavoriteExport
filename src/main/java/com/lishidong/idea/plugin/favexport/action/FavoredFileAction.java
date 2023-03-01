package com.lishidong.idea.plugin.favexport.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * 项目视图-文件右键
 */
public class FavoredFileAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        System.out.println("FavoredFileAction...actionPerformed");
    }
}
