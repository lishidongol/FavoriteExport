package com.lishidong.idea.plugin.favexport.model;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.Converter;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.OptionTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.StringJoiner;

/**
 * 全局配置
 */
@State(name = "com.lishidong.idea.plugin.favexport.model.ConfigManager", storages = @Storage(StoragePathMacros.WORKSPACE_FILE))
public class ConfigManager implements PersistentStateComponent<ConfigManager> {

    public static ConfigManager getInstance(Project project) {
        return project.getService(ConfigManager.class);
    }

    @OptionTag(nameAttribute = "", valueAttribute = "favListModel", converter = DefaultListModelConverter.class)
    private DefaultListModel favListModel;

    @OptionTag(nameAttribute = "", valueAttribute = "moduleListModel", converter = DefaultListModelConverter.class)
    private DefaultListModel moduleListModel;

    @OptionTag(nameAttribute = "", valueAttribute = "favFileTreeModel", converter = DefaultTreeModelConverter.class)
    private DefaultTreeModel favFileTreeModel;

    public void addTreeNode(String nodeName,MyMutableTreeNode parentNode, Object obj) {
        MyMutableTreeNode mutableTreeNode = new MyMutableTreeNode(nodeName,obj);
        //mutableTreeNode.
        favFileTreeModel.insertNodeInto(mutableTreeNode, parentNode,0);
    }

    public DefaultTreeModel getFavFileTreeModel() {
        if (favFileTreeModel == null) {
            favFileTreeModel = new DefaultTreeModel(new MyMutableTreeNode("root",null));
        }
        return favFileTreeModel;
    }

    public DefaultListModel getFavListModel() {
        if (favListModel == null) {
            favListModel = new DefaultListModel();
        }
        return favListModel;
    }

    public DefaultListModel getModuleListModel() {
        if (moduleListModel == null) {
            moduleListModel = new DefaultListModel();
        }
        return moduleListModel;
    }

    static final class DefaultTreeModelConverter extends Converter<DefaultTreeModel> {

        private String split = "__SPLIT__";

        @Override
        public @Nullable DefaultTreeModel fromString(@NotNull String value) {
            try {
                // 使用Base64解码将字符串转换为字节数组
                byte[] bytes = Base64.getDecoder().decode(value);
                // 创建一个字节数组输入流
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                // 创建一个对象输入流
                ObjectInputStream ois = new ObjectInputStream(bais);
                // 从对象输入流中读取数据模型
                DefaultTreeModel treeModel = (DefaultTreeModel) ois.readObject();
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
        public @Nullable String toString(@NotNull DefaultTreeModel value) {
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

    /**
     * DefaultListModel 转换方法
     */
    static final class DefaultListModelConverter extends Converter<DefaultListModel> {

        private String split = "__SPLIT__";

        // 加载的时候还原
        @Override
        public @Nullable DefaultListModel fromString(@NotNull String value) {
            DefaultListModel defaultListModel = new DefaultListModel();
            if (!"".equals(value)) {
                String[] strings = value.split(split);
                for (int i = 0; i < strings.length; i++) {
                    defaultListModel.addElement(strings[i]);
                }
            }
            return defaultListModel;
        }

        // 保存的时候调用
        @Override
        public @Nullable String toString(@NotNull DefaultListModel value) {
            if (value != null && value.getSize() >= 0) {
                StringJoiner joiner = new StringJoiner(split);
                int size = value.getSize();
                for (int i = 0; i < size; i++) {
                    joiner.add(value.getElementAt(i).toString());
                }
                return joiner.toString();
            }
            return "";
        }
    }


    @Override
    public @Nullable ConfigManager getState() {
        System.out.println("ConfigManager...getState");
        return this;
    }

    @Override
    public void loadState(@NotNull ConfigManager state) {
        System.out.println("ConfigManager...loadState");
        XmlSerializerUtil.copyBean(state, this);
    }

}
