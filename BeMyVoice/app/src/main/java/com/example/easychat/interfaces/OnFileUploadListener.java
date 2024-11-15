package com.example.easychat.interfaces;

public interface OnFileUploadListener {
    void onUploadSuccess(String downloadUrl);

    void onUploadFailure();
}