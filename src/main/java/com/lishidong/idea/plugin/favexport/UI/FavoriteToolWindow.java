package com.lishidong.idea.plugin.favexport.UI;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBList;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.IconUtil;
import com.lishidong.idea.plugin.favexport.model.FileMutableTreeNode;
import com.lishidong.idea.plugin.favexport.model.GlobalState;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.util.Enumeration;
import java.util.List;

/**
 * 左侧
 */
public class FavoriteToolWindow implements ToolWindowFactory {

    private final ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();

    private final JBList categoryList = new JBList();
    private final JBList moduleList = new JBList();
    private final Tree fileTree = new Tree();
    private final DefaultListModel categoryListModel = new DefaultListModel();
    private final DefaultListModel moduleListModel = new DefaultListModel();
    private final DefaultTreeModel fileTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode("root"));

    private static void showInfo(String msg) {
        Messages.showMessageDialog(msg, "提示", Messages.getInformationIcon());
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        SimpleToolWindowPanel categoryPanel = createCategoryPanel(project);
        toolWindow.getContentManager().addContent(contentFactory.createContent(categoryPanel, "目录", true));
        SimpleToolWindowPanel modulePanel = createModulePanel(project);
        toolWindow.getContentManager().addContent(contentFactory.createContent(modulePanel, "模块", true));
        SimpleToolWindowPanel fileTreePanel = createFileTreePanel(project);
        toolWindow.getContentManager().addContent(contentFactory.createContent(fileTreePanel, "文件", true));
    }

    /**
     * 目录面板
     *
     * @param project
     * @return
     */
    public SimpleToolWindowPanel createCategoryPanel(Project project) {
        SimpleToolWindowPanel simpleToolWindowPanel = new SimpleToolWindowPanel(true, true);
        // 定义工具栏
        DefaultActionGroup defaultActionGroup = new DefaultActionGroup();
        // 新增按钮
        defaultActionGroup.add(new AnAction(IconUtil.getAddIcon()) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                String input = Messages.showInputDialog("请输入目录名称", "添加目录", Messages.getInformationIcon());
                if (StringUtil.isEmptyOrSpaces(input)) {
                    showInfo("名称不能为空！");
                }
                else {
                    // 检查重复
                    GlobalState instance = GlobalState.getInstance(project);
                    if (!instance.categorys.contains(input)) {
                        categoryListModel.addElement(input);
                        instance.categorys.add(input);
                        // 更新文件树
                        ((DefaultMutableTreeNode) fileTreeModel.getRoot()).removeAllChildren();
                        fileTreeModel.setRoot(instance.getFileTreeNode());
                    }
                }
            }
        });
        // 编辑按钮
        defaultActionGroup.add(new AnAction(IconUtil.getEditIcon()) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                // 获取选中的项
                int selectedIndex = categoryList.getSelectedIndex();
                Object selectedValue = categoryList.getSelectedValue();

                String input = Messages.showInputDialog("请输入新目录名称", "修改目录", Messages.getInformationIcon(), String.valueOf(selectedValue), null);
                if (StringUtil.isEmptyOrSpaces(input)) {
                    showInfo("名称不能为空！");
                }
                else {
                    GlobalState instance = GlobalState.getInstance(project);
                    if (!instance.categorys.contains(input)) {
                        categoryListModel.setElementAt(input, selectedIndex);
                        instance.categorys.set(selectedIndex, input);
                        // 更新文件列表
                        for (int i = 0; i < instance.files.size(); i++) {
                            if (instance.files.get(i).getCategory().equals(selectedValue)) {
                                instance.files.get(i).setCategory(input);
                            }
                        }
                        // 更新文件树
                        ((DefaultMutableTreeNode) fileTreeModel.getRoot()).removeAllChildren();
                        fileTreeModel.setRoot(instance.getFileTreeNode());
                    }
                }
            }
        });
        // 删除按钮
        defaultActionGroup.add(new AnAction(IconUtil.getRemoveIcon()) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                int selectedIndex = categoryList.getSelectedIndex();
                Object selectedValue = categoryList.getSelectedValue();
                GlobalState instance = GlobalState.getInstance(project);
                // 删除关联目录的文件
                instance.files.removeIf(next -> String.valueOf(selectedValue).equals(next.getCategory()));

                categoryListModel.removeElementAt(selectedIndex);
                instance.categorys.remove(selectedIndex);
                // 更新文件树
                ((DefaultMutableTreeNode) fileTreeModel.getRoot()).removeAllChildren();
                fileTreeModel.setRoot(instance.getFileTreeNode());
            }
        });
        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLWINDOW_CONTENT, defaultActionGroup, true);
        simpleToolWindowPanel.setToolbar(actionToolbar.getComponent());

        // 设置数据
        List<String> categorys = GlobalState.getInstance(project).categorys;
        if (categorys != null && !categorys.isEmpty()) {
            for (int i = 0; i < categorys.size(); i++) {
                categoryListModel.addElement(categorys.get(i));
            }
        }

        // 定义目录列表
        categoryList.setModel(categoryListModel);

        simpleToolWindowPanel.setContent(categoryList);
        return simpleToolWindowPanel;
    }

    /**
     * 模块面板
     *
     * @param project
     * @return
     */
    public SimpleToolWindowPanel createModulePanel(Project project) {
        SimpleToolWindowPanel simpleToolWindowPanel = new SimpleToolWindowPanel(true, true);
        // 定义工具栏
        DefaultActionGroup defaultActionGroup = new DefaultActionGroup();
        // 新增按钮
        defaultActionGroup.add(new AnAction(IconUtil.getAddIcon()) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                String input = Messages.showInputDialog("请输入模块名称", "添加模块", Messages.getInformationIcon());
                if (StringUtil.isEmptyOrSpaces(input)) {
                    showInfo("名称不能为空！");
                }
                else {
                    GlobalState instance = GlobalState.getInstance(project);
                    // 检查重复
                    if (!instance.modules.contains(input)) {
                        moduleListModel.addElement(input);
                        instance.modules.add(input);
                        // 更新文件树
                        ((DefaultMutableTreeNode) fileTreeModel.getRoot()).removeAllChildren();
                        fileTreeModel.setRoot(instance.getFileTreeNode());
                    }
                }
            }
        });
        // 编辑按钮
        defaultActionGroup.add(new AnAction(IconUtil.getEditIcon()) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                // 获取选中的项
                int selectedIndex = moduleList.getSelectedIndex();
                Object selectedValue = moduleList.getSelectedValue();

                String input = Messages.showInputDialog("请输入新模块名称", "修改模块", Messages.getInformationIcon(), String.valueOf(selectedValue), null);
                if (StringUtil.isEmptyOrSpaces(input)) {
                    showInfo("名称不能为空！");
                }
                else {
                    GlobalState instance = GlobalState.getInstance(project);
                    if (!instance.modules.contains(input)) {
                        moduleListModel.setElementAt(input, selectedIndex);
                        instance.modules.set(selectedIndex, input);

                        // 更新文件列表
                        for (int i = 0; i < instance.files.size(); i++) {
                            if (instance.files.get(i).getModule().equals(selectedValue)) {
                                instance.files.get(i).setModule(input);
                            }
                        }
                        // 更新文件树
                        ((DefaultMutableTreeNode) fileTreeModel.getRoot()).removeAllChildren();
                        fileTreeModel.setRoot(instance.getFileTreeNode());
                    }
                }
            }
        });
        // 删除按钮
        defaultActionGroup.add(new AnAction(IconUtil.getRemoveIcon()) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                int selectedIndex = moduleList.getSelectedIndex();
                Object selectedValue = moduleList.getSelectedValue();
                GlobalState instance = GlobalState.getInstance(project);
                // 删除关联目录的文件
                instance.files.removeIf(next -> String.valueOf(selectedValue).equals(next.getModule()));

                moduleListModel.removeElementAt(selectedIndex);
                instance.modules.remove(selectedIndex);
                // 更新文件树
                ((DefaultMutableTreeNode) fileTreeModel.getRoot()).removeAllChildren();
                fileTreeModel.setRoot(instance.getFileTreeNode());
            }
        });
        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLWINDOW_CONTENT, defaultActionGroup, true);
        simpleToolWindowPanel.setToolbar(actionToolbar.getComponent());

        // 设置数据
        List<String> modules = GlobalState.getInstance(project).modules;
        if (modules != null && !modules.isEmpty()) {
            for (int i = 0; i < modules.size(); i++) {
                moduleListModel.addElement(modules.get(i));
            }
        }

        // 定义目录列表
        moduleList.setModel(moduleListModel);

        simpleToolWindowPanel.setContent(moduleList);
        return simpleToolWindowPanel;
    }

    /**
     * 文件面板
     *
     * @param project
     * @return
     */
    public SimpleToolWindowPanel createFileTreePanel(Project project) {
        SimpleToolWindowPanel simpleToolWindowPanel = new SimpleToolWindowPanel(true, true);
        // 定义工具栏
        DefaultActionGroup defaultActionGroup = new DefaultActionGroup();
        // 删除按钮
        defaultActionGroup.add(new AnAction(IconUtil.getRemoveIcon()) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                // 判断选择的节点是否是文件节点
                FileMutableTreeNode lastSelectedPathComponent = (FileMutableTreeNode) fileTree.getLastSelectedPathComponent();
                if (lastSelectedPathComponent != null) {
                    if (lastSelectedPathComponent.file != null) {
                        // 文件,删除对应文件名称文件，并刷新目录树
                        GlobalState.getInstance(project).files.removeIf(favoriteFile -> lastSelectedPathComponent.category.equals(favoriteFile.getCategory())
                                && lastSelectedPathComponent.module.equals(favoriteFile.getModule())
                                && lastSelectedPathComponent.getFilepath().equals(favoriteFile.getFilepath()));
                        // 更新树节点
                        fileTreeModel.removeNodeFromParent(lastSelectedPathComponent);
                    }
                    else if (!StringUtil.isEmptyOrSpaces(lastSelectedPathComponent.module)) {
                        // 删除模块下的文件
                        GlobalState.getInstance(project).files.removeIf(favoriteFile -> lastSelectedPathComponent.category.equals(favoriteFile.getCategory())
                                && lastSelectedPathComponent.module.equals(favoriteFile.getModule()));
                        // 更新树节点
                        Enumeration<TreeNode> children = lastSelectedPathComponent.children();
                        while (children.hasMoreElements()) {
                            fileTreeModel.removeNodeFromParent((FileMutableTreeNode) children.nextElement());
                        }
                    }
                    else if (!StringUtil.isEmptyOrSpaces(lastSelectedPathComponent.category)) {
                        // 删除目录模块下的文件
                        GlobalState.getInstance(project).files.removeIf(favoriteFile -> lastSelectedPathComponent.category.equals(favoriteFile.getCategory()));
                        // 模块级别
                        Enumeration<TreeNode> children = lastSelectedPathComponent.children();
                        while (children.hasMoreElements()) {
                            // 文件级别
                            Enumeration<? extends TreeNode> childrenChildren = children.nextElement().children();
                            while (childrenChildren.hasMoreElements()) {
                                fileTreeModel.removeNodeFromParent((FileMutableTreeNode) childrenChildren.nextElement());
                            }
                        }
                    }
                }
            }
        });
        // 刷新按钮
        defaultActionGroup.add(new AnAction(AllIcons.Actions.Refresh) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                // 删除所有节点
                ((FileMutableTreeNode) fileTreeModel.getRoot()).removeAllChildren();
                // 重新加载
                fileTreeModel.setRoot(GlobalState.getInstance(project).getFileTreeNode());
            }
        });

        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLWINDOW_CONTENT, defaultActionGroup, true);
        simpleToolWindowPanel.setToolbar(actionToolbar.getComponent());

        // 设置数据
        DefaultMutableTreeNode fileTreeNode = GlobalState.getInstance(project).getFileTreeNode();

        fileTreeModel.setRoot(fileTreeNode);
        // 定义目录列表
        fileTree.setModel(fileTreeModel);
        fileTree.setRootVisible(false);
        // 添加事件监听
        fileTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                System.out.println("选择");
            }
        });

        fileTreeModel.addTreeModelListener(new TreeModelListener() {
            @Override
            public void treeNodesChanged(TreeModelEvent e) {
                System.out.println("treeNodesChanged");
            }

            @Override
            public void treeNodesInserted(TreeModelEvent e) {
                System.out.println("treeNodesInserted");
            }

            @Override
            public void treeNodesRemoved(TreeModelEvent e) {
                System.out.println("treeNodesRemoved");
            }

            @Override
            public void treeStructureChanged(TreeModelEvent e) {
                System.out.println("treeStructureChanged");
            }
        });

        simpleToolWindowPanel.setContent(fileTree);
        return simpleToolWindowPanel;
    }

}
