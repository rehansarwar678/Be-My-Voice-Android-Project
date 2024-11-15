package com.example.easychat.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageUtils {
    public static Uri saveImageAndGetUri(Context context, Bitmap imageBitmap) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String uniqueFileName = "image_" + timeStamp + ".jpg";
        File cacheDir = context.getCacheDir();
        File imageFile = new File(cacheDir, uniqueFileName);
        try {
            FileOutputStream fos = new FileOutputStream(imageFile);
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
            MediaScannerConnection.scanFile(context,
                    new String[]{imageFile.getAbsolutePath()},
                    new String[]{"image/jpeg"}, null);

            return Uri.fromFile(imageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}