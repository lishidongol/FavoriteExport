package com.lishidong.idea.plugin.favexport.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.lishidong.idea.plugin.favexport.UI.ModuleTreeDialog;

/**
 * 项目视图-文件右键
 */
public class FavoredFileAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // 弹出收藏模块选择窗口
        ModuleTreeDialog dialog = new ModuleTreeDialog(e.getProject());
        dialog.setActionEvent(e);
        dialog.setSize(600, 400);
        dialog.show();
    }
}
