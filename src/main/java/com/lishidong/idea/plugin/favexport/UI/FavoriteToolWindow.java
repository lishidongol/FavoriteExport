package com.lishidong.idea.plugin.favexport.UI;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.IconUtil;
import com.lishidong.idea.plugin.favexport.listener.CompileDoneListener;
import com.lishidong.idea.plugin.favexport.listener.FilesChangeListener;
import com.lishidong.idea.plugin.favexport.listener.FilesCompileListener;
import com.lishidong.idea.plugin.favexport.model.FileMutableTreeNode;
import com.lishidong.idea.plugin.favexport.model.GlobalState;
import com.lishidong.idea.plugin.favexport.util.FavUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * 左侧
 */
public class FavoriteToolWindow implements ToolWindowFactory, FilesChangeListener {

    private final ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();

    private final JBList categoryList = new JBList();
    private final JBList moduleList = new JBList();
    private final Tree fileTree = new Tree();
    private final DefaultListModel categoryListModel = new DefaultListModel();
    private final DefaultListModel moduleListModel = new DefaultListModel();
    private final DefaultTreeModel fileTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode("root"));

    private Project project;

    private static void showInfo(String msg) {
        Messages.showMessageDialog(msg, "提示", Messages.getInformationIcon());
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        this.project = project;
        SimpleToolWindowPanel fileTreePanel = createFileTreePanel(project);
        toolWindow.getContentManager().addContent(contentFactory.createContent(fileTreePanel, "文件", true));

        SimpleToolWindowPanel categoryPanel = createCategoryPanel(project);
        toolWindow.getContentManager().addContent(contentFactory.createContent(categoryPanel, "目录", true));

        SimpleToolWindowPanel modulePanel = createModulePanel(project);
        toolWindow.getContentManager().addContent(contentFactory.createContent(modulePanel, "模块", true));

        // 创建事件监听
        this.project.getMessageBus().connect().subscribe(FilesChangeListener.TOPIC, this);
        this.project.getMessageBus().connect().subscribe(FilesCompileListener.TOPIC, new CompileDoneListener(this.project));
    }

    /**
     * 刷新文件树
     */
    public void refreshFileTree() {
        // 更新文件树
        ((DefaultMutableTreeNode) fileTreeModel.getRoot()).removeAllChildren();
        fileTreeModel.setRoot(GlobalState.getInstance(project).getFileTreeNode());
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
                        refreshFileTree();
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
                        refreshFileTree();
                    }
                }
            }
        });
        // 删除按钮
        defaultActionGroup.add(new AnAction(IconUtil.getRemoveIcon()) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                int selectedIndex = categoryList.getSelectedIndex();
                if (selectedIndex != -1) {
                    Object selectedValue = categoryList.getSelectedValue();
                    GlobalState instance = GlobalState.getInstance(project);
                    // 删除关联目录的文件
                    instance.files.removeIf(next -> String.valueOf(selectedValue).equals(next.getCategory()));

                    categoryListModel.removeElementAt(selectedIndex);
                    instance.categorys.remove(selectedIndex);
                    // 更新文件树
                    refreshFileTree();
                }
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
        categoryList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Icon getIcon() {
                return AllIcons.Nodes.Folder;
            }
        });
        // 定义目录列表
        categoryList.setModel(categoryListModel);

        simpleToolWindowPanel.setContent(new JBScrollPane(categoryList));
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
                        refreshFileTree();
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
                        refreshFileTree();
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
                refreshFileTree();
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
        moduleList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Icon getIcon() {
                return AllIcons.Nodes.Folder;
            }
        });
        // 定义目录列表
        moduleList.setModel(moduleListModel);

        simpleToolWindowPanel.setContent(new JBScrollPane(moduleList));
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
                        GlobalState.getInstance(project).files.removeIf(favoriteFile -> lastSelectedPathComponent.category.equals(favoriteFile.getCategory()) && lastSelectedPathComponent.module.equals(favoriteFile.getModule()) && lastSelectedPathComponent.getFilepath().equals(favoriteFile.getFilepath()));
                        // 更新树节点
                        fileTreeModel.removeNodeFromParent(lastSelectedPathComponent);
                    }
                    else if (!StringUtil.isEmptyOrSpaces(lastSelectedPathComponent.module)) {
                        // 删除模块下的文件
                        GlobalState.getInstance(project).files.removeIf(favoriteFile -> lastSelectedPathComponent.category.equals(favoriteFile.getCategory()) && lastSelectedPathComponent.module.equals(favoriteFile.getModule()));
                        // 更新树节点
                        lastSelectedPathComponent.removeAllChildren();
                        fileTreeModel.reload(lastSelectedPathComponent);
                    }
                    else if (!StringUtil.isEmptyOrSpaces(lastSelectedPathComponent.category)) {
                        // 删除目录模块下的文件
                        GlobalState.getInstance(project).files.removeIf(favoriteFile -> lastSelectedPathComponent.category.equals(favoriteFile.getCategory()));
                        // 模块级别
                        Enumeration<TreeNode> children = lastSelectedPathComponent.children();
                        List<FileMutableTreeNode> removeList = new ArrayList<>();
                        while (children.hasMoreElements()) {
                            // 文件级别
                            Iterator<? extends TreeNode> iterator = children.nextElement().children().asIterator();
                            while (iterator.hasNext()) {
                                FileMutableTreeNode fileMutableTreeNode = (FileMutableTreeNode) iterator.next();
                                removeList.add(fileMutableTreeNode);
                            }
                        }
                        if (!removeList.isEmpty()) {
                            for (FileMutableTreeNode fileMutableTreeNode : removeList) {
                                fileTreeModel.removeNodeFromParent(fileMutableTreeNode);
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
                // 刷新文件树
                refreshFileTree();
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

        fileTree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                FileMutableTreeNode fileMutableTreeNode = (FileMutableTreeNode) value;
                if (fileMutableTreeNode.file != null) {
                    return new JBLabel(fileMutableTreeNode.file.getName(), fileMutableTreeNode.file.getFileType().getIcon(), 2);
                }
                else {
                    return new JBLabel(fileMutableTreeNode.toString(), AllIcons.Nodes.Folder, 2);
                }
            }
        });

        // 创建右键菜单
        JBPopupMenu popupMenu = new JBPopupMenu();
        JMenuItem openMenuItem = new JMenuItem("打开文件", AllIcons.Actions.MenuOpen);
        JMenuItem exportMenuItem = new JMenuItem("导出", AllIcons.ToolbarDecorator.Export);
        openMenuItem.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 获取选中的节点
                FileMutableTreeNode[] selectedNodes = fileTree.getSelectedNodes(FileMutableTreeNode.class, node -> node.file != null);
                for (FileMutableTreeNode selectedNode : selectedNodes) {
                    new OpenFileDescriptor(project, selectedNode.file).navigate(true);
                }
            }
        });
        exportMenuItem.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 获取选中的文件
                FileMutableTreeNode[] selectedNodes = fileTree.getSelectedNodes(FileMutableTreeNode.class, node -> node.file != null);
                // 转成file
                List<VirtualFile> list = new ArrayList<>();
                for (FileMutableTreeNode selectedNode : selectedNodes) {
                    list.add(selectedNode.file);
                }
                // 弹出选择框
                FavUtil.openFileSelect(project, list);
            }
        });
        popupMenu.add(openMenuItem);
        popupMenu.add(exportMenuItem);

        // 鼠标事件监听
        fileTree.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                // 判断位置
                TreePath pathForLocation = fileTree.getPathForLocation(x, y);
                if (pathForLocation != null) {
                    // 在右键点击的位置弹出菜单
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        popupMenu.show(fileTree, x, y);
                    }
                    // 左键双击,打开文件
                    if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                        // 打开选中文件
                        FileMutableTreeNode[] selectedNodes = fileTree.getSelectedNodes(FileMutableTreeNode.class, node -> node.file != null);
                        if (selectedNodes.length > 0) {
                            new OpenFileDescriptor(project, selectedNodes[0].file).navigate(true);
                        }
                    }
                }
            }
        });

        simpleToolWindowPanel.setContent(new JBScrollPane(fileTree));
        return simpleToolWindowPanel;
    }


    @Override
    public void filesChange() {
        refreshFileTree();
    }
}
