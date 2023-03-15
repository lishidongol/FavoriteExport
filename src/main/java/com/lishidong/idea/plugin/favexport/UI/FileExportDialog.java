package com.lishidong.idea.plugin.favexport.UI;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.VcsShowConfirmationOption;
import com.intellij.openapi.vcs.changes.ui.SelectFilesDialog;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.lishidong.idea.plugin.favexport.listener.FilesCompileListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.List;

/**
 * 导出文件配置弹窗
 */
public class FileExportDialog extends SelectFilesDialog {

    private List<VirtualFile> virtualFiles;
    private Project project;

    private TextFieldWithBrowseButton fileChooser = new TextFieldWithBrowseButton();

    private JBTextField outputNameTextField = new JBTextField();

    public FileExportDialog(Project project, @NotNull List<? extends VirtualFile> files, @Nullable @NlsContexts.Label String prompt, @Nullable VcsShowConfirmationOption confirmationOption, boolean selectableFiles, boolean deletableFiles) {
        super(project, files, prompt, confirmationOption, selectableFiles, deletableFiles);
        this.project = project;
        init();
    }

    /**
     * 创建导出位置选择
     *
     * @return
     */
    @Override
    protected @Nullable JComponent createTitlePane() {
        // 两行一列
        JBPanel panel = new JBPanel(new GridLayout(2, 1));

        // 第一行
        JBPanel row1 = new JBPanel(new BorderLayout());
        row1.add(new JBLabel("导出位置："), BorderLayout.WEST);
        row1.add(fileChooser, BorderLayout.CENTER);
        // 第二行
        JBPanel row2 = new JBPanel(new BorderLayout());
        row2.add(new JBLabel("导出名称："), BorderLayout.WEST);
        row2.add(outputNameTextField, BorderLayout.CENTER);

        panel.add(row1);
        panel.add(row2);

        //panel.add(new JBLabel("导出位置："), BorderLayout.WEST);
        //panel.add(new JBLabel("导出名称："), BorderLayout.WEST);
        //panel.add(fileChooser, BorderLayout.CENTER);
        //panel.add(outputNameTextField, BorderLayout.CENTER);

        fileChooser.setEditable(false);
        fileChooser.addBrowseFolderListener(new TextBrowseFolderListener(FileChooserDescriptorFactory.createSingleFolderDescriptor(), project));
        return panel;
    }

    /**
     * 触发事件
     */
    @Override
    protected void doOKAction() {
        // 导出位置
        String outputPath = fileChooser.getText();
        if (StringUtil.isEmptyOrSpaces(outputPath)) {
            Messages.showMessageDialog("导出路径不能为空!", "提示", Messages.getInformationIcon());
            return;
        }
        if (StringUtil.isEmptyOrSpaces(outputNameTextField.getText())) {
            Messages.showMessageDialog("导出名称不能为空!", "提示", Messages.getInformationIcon());
            return;
        }

        Collection<VirtualFile> selectedFiles = this.getSelectedFiles();
        if (selectedFiles == null || selectedFiles.isEmpty()) {
            Messages.showMessageDialog("未选择导出文件!", "提示", Messages.getInformationIcon());
            return;
        }

        // 执行编译
        project.getMessageBus().syncPublisher(FilesCompileListener.TOPIC).filesCompile(outputPath, outputNameTextField.getText(), selectedFiles);

        super.doOKAction();
    }

    public void setVirtualFiles(List<VirtualFile> virtualFiles) {
        this.virtualFiles = virtualFiles;
    }
}
