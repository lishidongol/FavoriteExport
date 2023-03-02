package com.lishidong.idea.plugin.favexport.UI;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.lishidong.idea.plugin.favexport.listener.FilesChangeListener;
import com.lishidong.idea.plugin.favexport.model.FavoriteFile;
import com.lishidong.idea.plugin.favexport.model.FileMutableTreeNode;
import com.lishidong.idea.plugin.favexport.model.GlobalState;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeSelectionModel;

/**
 * 模块选择
 */
public class ModuleTreeDialog extends DialogWrapper {

    private final Tree moduleTree = new Tree();
    private final DefaultTreeModel moduleTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode("root"));

    private Project project;

    /**
     * 将事件传递进入模态框
     */
    private AnActionEvent actionEvent;

    public ModuleTreeDialog(@Nullable Project project) {
        super(project);
        this.project = project;
        init();
    }

    public void setActionEvent(AnActionEvent actionEvent) {
        this.actionEvent = actionEvent;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JBScrollPane panel = new JBScrollPane();
        DefaultMutableTreeNode moduleTreeNode = GlobalState.getInstance(project).getModuleTreeNode();
        moduleTreeModel.setRoot(moduleTreeNode);
        moduleTree.setModel(moduleTreeModel);
        // 设置单选模式
        DefaultTreeSelectionModel defaultTreeSelectionModel = new DefaultTreeSelectionModel();
        defaultTreeSelectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        moduleTree.setSelectionModel(defaultTreeSelectionModel);
        moduleTree.setRootVisible(false);
        panel.setViewportView(moduleTree);
        return panel;
    }

    @Override
    protected void doOKAction() {
        // 确定选择的模块节点
        FileMutableTreeNode fileMutableTreeNode = (FileMutableTreeNode) moduleTree.getLastSelectedPathComponent();
        // 获取选择的文件
        VirtualFile[] chooseFiles = actionEvent.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
        if (chooseFiles != null && chooseFiles.length > 0) {
            GlobalState instance = GlobalState.getInstance(project);
            for (VirtualFile chooseFile : chooseFiles) {
                // 添加到文件树
                instance.files.add(new FavoriteFile(fileMutableTreeNode.category, fileMutableTreeNode.module, chooseFile.getName(), chooseFile.getPath()));
            }
            // 刷新文件树
            project.getMessageBus().syncPublisher(FilesChangeListener.TOPIC).filesChange();
        }
        super.doOKAction();
    }
}
