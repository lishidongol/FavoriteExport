package com.lishidong.idea.plugin.favexport.UI;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vcs.VcsShowConfirmationOption;
import com.intellij.openapi.vcs.changes.ui.SelectFilesDialog;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FileListDialog extends SelectFilesDialog {
    public FileListDialog(Project project, @NotNull List<? extends VirtualFile> files, @Nullable @NlsContexts.Label String prompt, @Nullable VcsShowConfirmationOption confirmationOption, boolean selectableFiles, boolean deletableFiles) {
        super(project, files, prompt, confirmationOption, selectableFiles, deletableFiles);
        init();
    }


}
