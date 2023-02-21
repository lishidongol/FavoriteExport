package com.lishidong.idea.plugin.favexport.UI;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.IconUtil;
import com.intellij.util.ui.UIUtil;
import com.lishidong.idea.plugin.favexport.model.ConfigManager;
import com.lishidong.idea.plugin.favexport.model.MyMutableTreeNode;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 收藏工具栏
 * Tab页：
 * 1.分类目录配置页
 * 2.模块配置页
 * 3.根据目录和模块的展示树形
 */
public class FavToolWindow implements ToolWindowFactory {

    private JBList favJBList = new JBList();
    private JBList moduleJBList = new JBList();
    private Tree favFileTree = new Tree();

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        System.out.println("进入FavWindow。。。createToolWindowContent");
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        ConfigManager instance = ConfigManager.getInstance(project);

        /**=====创建分类目录维护tab=====*/
        SimpleToolWindowPanel favItemToolWindowPanel = new SimpleToolWindowPanel(true, true);
        // 创建按钮组增删改
        ActionToolbar favToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLWINDOW_CONTENT, createFavGroup(project), true);
        favItemToolWindowPanel.setToolbar(favToolbar.getComponent());
        // 创建内容窗口
        JBPanel favJBPanel = new JBPanel(new BorderLayout());
        // 填充favJBList
        favJBList.setModel(instance.getFavListModel());
        favJBPanel.add(favJBList, BorderLayout.CENTER);
        favItemToolWindowPanel.setContent(favJBPanel);
        toolWindow.getContentManager().addContent(contentFactory.createContent(favItemToolWindowPanel, "目录", true));
        /**=====创建分类目录维护tab=====*/

        /**=====创建模块维护tab=====*/
        SimpleToolWindowPanel moduleItemToolWindowPanel = new SimpleToolWindowPanel(true, true);
        // 创建按钮组增删改
        ActionToolbar moduleToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLWINDOW_CONTENT, createModuleGroup(project), true);
        moduleItemToolWindowPanel.setToolbar(moduleToolbar.getComponent());
        // 创建内容窗口
        //JBPanel moduleJBPanel = new JBPanel(new BorderLayout());
        // 填充favJBList
        moduleJBList.setModel(instance.getModuleListModel());
        //moduleJBPanel.add(moduleJBList, BorderLayout.CENTER);
        moduleItemToolWindowPanel.setContent(moduleJBList);
        toolWindow.getContentManager().addContent(contentFactory.createContent(moduleItemToolWindowPanel, "模块", true));
        /**=====创建模块维护tab=====*/

        /**=====收藏模块维护tab=====*/
        SimpleToolWindowPanel favFileToolWindowPanel = new SimpleToolWindowPanel(true, true);
        // 创建按钮组
        ActionToolbar favFileToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLWINDOW_CONTENT, createFavFileGroup(project), true);
        favFileToolWindowPanel.setToolbar(favFileToolbar.getComponent());
        // 创建内容窗口
        favFileTree.setModel(ConfigManager.getInstance(project).getFavFileTreeModel());
        favFileToolWindowPanel.setContent(favFileTree);
        toolWindow.getContentManager().addContent(contentFactory.createContent(favFileToolWindowPanel, "导出", true));

        /**=====收藏模块维护tab=====*/
    }

    /**
     * 显示提示信息
     *
     * @param msg
     */
    private static void showInfo(String msg) {
        Messages.showMessageDialog(msg, "提示", Messages.getInformationIcon());
    }

    /**
     * 收藏TAB工具栏
     *
     * @param project
     * @return
     */
    private ActionGroup createFavGroup(Project project) {
        ConfigManager instance = ConfigManager.getInstance(project);
        return new DefaultActionGroup(
                new AnAction[]{new AnAction(IconUtil.getAddIcon()) {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        // 添加
                        String input = Messages.showInputDialog("请输入收藏夹名称", "添加收藏夹", IconUtil.getAddIcon());
                        if (StringUtil.isEmptyOrSpaces(input)) {
                            showInfo("名称不能为空！");
                        }
                        else {
                            if (instance.getFavListModel().contains(input)) {
                                showInfo("名称不能重复！");
                            }
                            else {
                                instance.getFavListModel().addElement(input);
                            }
                        }
                    }
                }, new AnAction(IconUtil.getEditIcon()) {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        // 修改
                        int selectedIndex = favJBList.getSelectedIndex();
                        if (selectedIndex != -1) {
                            String input = Messages.showInputDialog(e.getProject(), "请输入收藏夹名称", "修改收藏夹", UIUtil.getInformationIcon(), favJBList.getSelectedValue().toString(), null);
                            if (StringUtil.isEmptyOrSpaces(input)) {
                                showInfo("名称不能为空！");
                            }
                            else {
                                instance.getFavListModel().setElementAt(input, selectedIndex);
                            }
                        }
                    }
                }, new AnAction(IconUtil.getRemoveIcon()) {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        instance.getFavListModel().removeElementAt(favJBList.getSelectedIndex());
                    }
                }});
    }

    /**
     * 模块TAB工具栏
     *
     * @param project
     * @return
     */
    private ActionGroup createModuleGroup(Project project) {
        ConfigManager instance = ConfigManager.getInstance(project);
        return new DefaultActionGroup(
                new AnAction[]{new AnAction(IconUtil.getAddIcon()) {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        // 添加
                        String input = Messages.showInputDialog("请输入模块名称", "添加模块", IconUtil.getAddIcon());
                        if (StringUtil.isEmptyOrSpaces(input)) {
                            showInfo("名称不能为空！");
                        }
                        else {
                            if (instance.getModuleListModel().contains(input)) {
                                showInfo("名称不能重复！");
                            }
                            else {
                                instance.getModuleListModel().addElement(input);
                            }
                        }
                    }
                }, new AnAction(IconUtil.getEditIcon()) {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        // 修改
                        int selectedIndex = favJBList.getSelectedIndex();
                        if (selectedIndex != -1) {
                            String input = Messages.showInputDialog(e.getProject(), "请输入模块名称", "修改模块", UIUtil.getInformationIcon(), favJBList.getSelectedValue().toString(), null);
                            if (StringUtil.isEmptyOrSpaces(input)) {
                                showInfo("名称不能为空！");
                            }
                            else {
                                instance.getModuleListModel().setElementAt(input, selectedIndex);
                            }
                        }
                    }
                }, new AnAction(IconUtil.getRemoveIcon()) {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        instance.getModuleListModel().removeElementAt(favJBList.getSelectedIndex());
                    }
                }});
    }

    /**
     * 收藏文件TAB工具栏
     */
    private ActionGroup createFavFileGroup(Project project) {
        return new DefaultActionGroup(
                new AnAction(IconUtil.getRemoveIcon()) {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        // 获取选择的节点
                        int selectionCount = favFileTree.getSelectionCount();
                        //
                        System.out.println(selectionCount);

                    }
                },new AnAction(IconUtil.getAddIcon()) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                // 获取选择的节点
                MyMutableTreeNode[] selectedNodes = favFileTree.getSelectedNodes(MyMutableTreeNode.class, null);
                Map<String,String> map = new HashMap<>();
                map.put("name","11111");
                map.put("age","22222");
                ConfigManager.getInstance(e.getProject()).addTreeNode("12345",selectedNodes[0], map);
                System.out.println(selectedNodes.length);
            }
        });
    }
}
