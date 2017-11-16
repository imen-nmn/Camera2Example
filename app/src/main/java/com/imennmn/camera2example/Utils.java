package com.imennmn.camera2example;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by imen_nmn on 16/11/17.
 */

public class Utils {

    public static Boolean existsSdcard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static File getCacheFile(String dirName, Context context) {
        File result;
        if (existsSdcard()) {
            File cacheDir = context.getExternalCacheDir();
            if (cacheDir == null) {
                result = new File(Environment.getExternalStorageDirectory(),
                        "Android/data/" + context.getPackageName() + "/cache/" + dirName);
            } else {
                result = new File(cacheDir, dirName);
            }
        } else {
            result = new File(context.getCacheDir(), dirName);
        }

        if (result.exists() || result.mkdirs()) {
            return result;
        } else {
            return null;
        }
    }



}
