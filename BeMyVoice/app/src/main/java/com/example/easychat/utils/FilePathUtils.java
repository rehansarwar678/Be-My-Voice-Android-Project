package com.example.easychat.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

public class FilePathUtils {

    public static String getRealPathFromUri(Context context, Uri uri) {
        String filePath = null;
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            filePath = getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            filePath = uri.getPath();
        }
        return filePath;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    // Additional methods for handling different versions of Android

    private static boolean isDocumentUri(Context context, Uri uri) {
        return DocumentsContract.isDocumentUri(context, uri);
    }

    private static String getDocumentId(Uri documentUri) {
        return DocumentsContract.getDocumentId(documentUri);
    }

    private static Uri buildDocumentUriUsingTree(Uri documentUri) {
        return DocumentsContract.buildDocumentUriUsingTree(documentUri,
                DocumentsContract.getTreeDocumentId(documentUri));
    }

    private static String getFilePathForNougatAndAbove(Context context, Uri uri) {
        String filePath = null;
        if (isDocumentUri(context, uri)) {
            String documentId = getDocumentId(uri);
            String uriAuthority = uri.getAuthority();

            if ("com.android.externalstorage.documents".equals(uriAuthority)) {
                String[] split = documentId.split(":");
                String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    filePath = Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if ("com.android.providers.downloads.documents".equals(uriAuthority)) {
                Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.parseLong(documentId));
                filePath = getDataColumn(context, contentUri, null, null);
            } else if ("com.android.providers.media.documents".equals(uriAuthority)) {
                String[] split = documentId.split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = "_id=?";
                String[] selectionArgs = new String[]{split[1]};
                filePath = getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        return filePath;
    }

    public static String getFilePathFromUri(Context context, Uri uri) {
        String filePath = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            filePath = getFilePathForNougatAndAbove(context, uri);
        } else {
            filePath = getRealPathFromUri(context, uri);
        }
        return filePath;
    }
}

