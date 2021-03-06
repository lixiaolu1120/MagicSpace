package com.gdq.multhreaddownload.download.db;

import com.gdq.multhreaddownload.download.bean.FileInfo;
import com.gdq.multhreaddownload.download.bean.ThreadInfo;

import java.util.List;


/**
 * Created by gdq on 16/6/26.
 */
public interface ThreadDAO {
    public void insertThread(ThreadInfo threadInfo);

    public void deleteThread(String url);

    public void updateThread(String url, int id, int finished);

    public List<ThreadInfo> getThreadsByUrl(String url);

    public boolean isThreadExists(String url, int id);

    public void insertUnFinishFileifNotExists(FileInfo fileInfo);

    public void insertFinishFileifNotExists(FileInfo fileInfo);

    public void deleteUnFinishFileByContentId(String contentId);

    public void deleteFinishFileByContentId(String contentId);

    public List<FileInfo> getAllFinishFile();

    public List<FileInfo> getAllUnFinishFile();

    public void updateFinishFileByContentId(FileInfo fileInfo);

    public void updateUnFinishFileByContentId(FileInfo fileInfo);


}
