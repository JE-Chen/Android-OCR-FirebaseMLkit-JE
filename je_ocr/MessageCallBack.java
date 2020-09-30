package com.je_chen.je_ocr;

import java.io.File;

public interface MessageCallBack {

    void onDownloadStarted(long totalLength);

    void onDownloadProgress(long downloadedLength);

    void onDownloadComplete(File file);

    void onDownloadError(Exception e);

}
