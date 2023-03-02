package com.lishidong.idea.plugin.favexport.listener;

import com.intellij.util.messages.Topic;

import java.util.EventListener;

/**
 * 自定义文件树改变事件-需手动触发
 */
public interface FilesChangeListener extends EventListener {

    @Topic.ProjectLevel
    Topic<FilesChangeListener> TOPIC = new Topic<>("files change events", FilesChangeListener.class, Topic.BroadcastDirection.NONE);

    void filesChange();

}
