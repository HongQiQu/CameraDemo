package com.ihongqiqu.camera;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * 工具类
 *
 * Created by zhenguo on 2/27/15.
 */
public class Utils {

    public static File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

}
