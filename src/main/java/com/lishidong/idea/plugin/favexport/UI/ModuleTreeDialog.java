package com.lishidong.idea.plugin.favexport.UI;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.lishidong.idea.plugin.favexport.listener.FilesChangeListener;
import com.lishidong.idea.plugin.favexport.model.FavoriteFile;
import com.lishidong.idea.plugin.favexport.model.FileMutableTreeNode;
import com.lishidong.idea.plugin.favexport.model.GlobalState;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        setOKActionEnabled(false);
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
        moduleTree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Icon getIcon() {
                return AllIcons.Nodes.Folder;
            }
        });
        // 设置单选模式
        DefaultTreeSelectionModel defaultTreeSelectionModel = new DefaultTreeSelectionModel();
        defaultTreeSelectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        moduleTree.setSelectionModel(defaultTreeSelectionModel);
        moduleTree.setRootVisible(false);
        moduleTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                TreePath path = e.getPath();
                if (path.getPathCount() != 3) {
                    setOKActionEnabled(false);
                }
                else {
                    setOKActionEnabled(true);
                }
            }
        });
        panel.setViewportView(moduleTree);
        return panel;
    }

    @Override
    protected void doOKAction() {
        // 获取选择的文件
        VirtualFile[] chooseFiles = actionEvent.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
        if (chooseFiles != null && chooseFiles.length > 0) {
            // 先检查是否选择了目录
            for (VirtualFile chooseFile : chooseFiles) {
                if (chooseFile.isDirectory()) {
                    Messages.showMessageDialog("不支持收藏目录!", "提示", Messages.getInformationIcon());
                    super.doOKAction();
                    return;
                }
            }

            // 确定选择的模块节点
            FileMutableTreeNode fileMutableTreeNode = (FileMutableTreeNode) moduleTree.getLastSelectedPathComponent();

            GlobalState instance = GlobalState.getInstance(project);
            List<FavoriteFile> files = instance.files;
            List<FavoriteFile> addFav = new ArrayList<>();
            // 判断选择的模块下是否存在文件
            List<FavoriteFile> collect = files.stream()
                    .filter(f -> fileMutableTreeNode.category.equals(f.getCategory()) && fileMutableTreeNode.module.equals(f.getModule()))
                    .collect(Collectors.toList());
            if (!collect.isEmpty()) {
                for (VirtualFile chooseFile : chooseFiles) {
                    boolean isExists = false;
                    for (FavoriteFile file : collect) {
                        if (file.getFilepath().equals(chooseFile.getPath())) {
                            isExists = true;
                            break;
                        }
                    }
                    if (!isExists) {
                        // 添加
                        addFav.add(new FavoriteFile(fileMutableTreeNode.category, fileMutableTreeNode.module, chooseFile.getName(), chooseFile.getPath()));
                    }
                }
            }
            else {
                // 选择的全部添加
                for (VirtualFile chooseFile : chooseFiles) {
                    addFav.add(new FavoriteFile(fileMutableTreeNode.category, fileMutableTreeNode.module, chooseFile.getName(), chooseFile.getPath()));
                }
            }
            if (!addFav.isEmpty()) {
                instance.files.addAll(addFav);
                // 刷新文件树
                project.getMessageBus().syncPublisher(FilesChangeListener.TOPIC).filesChange();
            }
        }
        setOKActionEnabled(true);
        super.doOKAction();
    }
}
