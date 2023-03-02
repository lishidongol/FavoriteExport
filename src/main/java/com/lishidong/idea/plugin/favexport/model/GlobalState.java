package com.lishidong.idea.plugin.favexport.model;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 全局状态管理
 */
@State(name = "com.lishidong.idea.plugin.favexport.model.GlobalState", storages = @Storage(StoragePathMacros.WORKSPACE_FILE))
public class GlobalState implements PersistentStateComponent<GlobalState>, Serializable {
    private static final long serialVersionUID = 1L;

    public List<String> categorys = new ArrayList<>();
    public List<String> modules = new ArrayList<>();

    public List<FavoriteFile> files = new ArrayList<>();

    public static GlobalState getInstance(Project project) {
        return project.getService(GlobalState.class);
    }

    /**
     * 获取收藏的文件树
     *
     * @return
     */
    public DefaultMutableTreeNode getFileTreeNode() {
        FileMutableTreeNode root = new FileMutableTreeNode("root");
        if (categorys != null && !categorys.isEmpty()) {
            for (int i = 0; i < categorys.size(); i++) {
                // 添加目录
                FileMutableTreeNode categoryNode = new FileMutableTreeNode(categorys.get(i));
                categoryNode.setAllowsChildren(true);
                if (modules != null && !modules.isEmpty()) {
                    for (int i1 = 0; i1 < modules.size(); i1++) {
                        // 添加模块
                        FileMutableTreeNode moduleNode = new FileMutableTreeNode(categorys.get(i), modules.get(i1));
                        moduleNode.setAllowsChildren(true);
                        if (files != null && !files.isEmpty()) {
                            for (int i2 = 0; i2 < files.size(); i2++) {
                                if (categorys.get(i).equals(files.get(i2).getCategory()) && modules.get(i1).equals(files.get(i2).getModule())) {
                                    VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(files.get(i2).getFilepath());
                                    // 添加文件
                                    FileMutableTreeNode fileNode = new FileMutableTreeNode(categorys.get(i), modules.get(i1), virtualFile);
                                    fileNode.setAllowsChildren(false);
                                    moduleNode.add(fileNode);
                                }
                            }
                        }
                        // 模块添加到目录节点
                        categoryNode.add(moduleNode);
                    }
                }
                // 目录添加到跟根节点
                root.add(categoryNode);
            }
        }
        return root;
    }

    /**
     * 模块树
     *
     * @return
     */
    public DefaultMutableTreeNode getModuleTreeNode() {
        FileMutableTreeNode root = new FileMutableTreeNode("root");
        if (categorys != null && !categorys.isEmpty()) {
            for (int i = 0; i < categorys.size(); i++) {
                // 添加目录
                FileMutableTreeNode categoryNode = new FileMutableTreeNode(categorys.get(i));
                categoryNode.setAllowsChildren(true);
                if (modules != null && !modules.isEmpty()) {
                    for (int i1 = 0; i1 < modules.size(); i1++) {
                        // 添加模块
                        FileMutableTreeNode moduleNode = new FileMutableTreeNode(categorys.get(i), modules.get(i1));
                        moduleNode.setAllowsChildren(false);
                        // 模块添加到目录节点
                        categoryNode.add(moduleNode);
                    }
                }
                // 目录添加到跟根节点
                root.add(categoryNode);
            }
        }
        return root;
    }

    @Override
    public @Nullable GlobalState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull GlobalState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
