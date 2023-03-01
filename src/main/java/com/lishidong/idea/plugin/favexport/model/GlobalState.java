package com.lishidong.idea.plugin.favexport.model;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.util.xmlb.Converter;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.OptionTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * 全局状态管理
 */
@State(name = "com.lishidong.idea.plugin.favexport.model.GlobalState", storages = @Storage(StoragePathMacros.WORKSPACE_FILE))
public class GlobalState implements PersistentStateComponent<GlobalState>, Serializable {
    private static final long serialVersionUID = 1L;

    public List<String> categorys = new ArrayList<>();
    public List<String> modules = new ArrayList<>();

    @OptionTag(nameAttribute = "", valueAttribute = "files", converter = FavoriteFileConverter.class)
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
        files.clear();
        // 添加一个文件
        files.add(new FavoriteFile("11", "web", LocalFileSystem.getInstance().findFileByPath("C:/Users/OOO/IdeaProjects/untitled1/src/Test.java")));

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
                                    // 添加文件
                                    FileMutableTreeNode fileNode = new FileMutableTreeNode(categorys.get(i), modules.get(i1), files.get(i2).getFile());
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

    @Override
    public @Nullable GlobalState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull GlobalState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    static final class FavoriteFileConverter extends Converter<List<FavoriteFile>> {

        @Override
        public @Nullable List<FavoriteFile> fromString(@NotNull String value) {
            try {
                // 使用Base64解码将字符串转换为字节数组
                byte[] bytes = Base64.getDecoder().decode(value);
                // 创建一个字节数组输入流
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                // 创建一个对象输入流
                ObjectInputStream ois = new ObjectInputStream(bais);
                // 从对象输入流中读取数据模型
                List<FavoriteFile> treeModel = (List<FavoriteFile>) ois.readObject();
                // 关闭对象输入流
                ois.close();
                return treeModel;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public @Nullable String toString(@NotNull List<FavoriteFile> value) {
            try {
                // 创建一个字节数组输出流
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                // 创建一个对象输出流
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                // 将数据模型写入对象输出流
                oos.writeObject(value);
                // 关闭对象输出流
                oos.close();
                // 使用Base64编码将字节数组转换为字符串
                return Base64.getEncoder().encodeToString(baos.toByteArray());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
