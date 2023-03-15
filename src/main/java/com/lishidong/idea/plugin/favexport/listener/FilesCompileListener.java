package com.lishidong.idea.plugin.favexport.listener;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.Topic;

import java.util.Collection;
import java.util.EventListener;

/**
 * 触发文件编译事件
 */
public interface FilesCompileListener extends EventListener {

    @Topic.ProjectLevel
    Topic<FilesCompileListener> TOPIC = new Topic<>("files compile events", FilesCompileListener.class, Topic.BroadcastDirection.NONE);


    void filesCompile(String outputPath, String outputName, Collection<VirtualFile> selectedFiles);
}
