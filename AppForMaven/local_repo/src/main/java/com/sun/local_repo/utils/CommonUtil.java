package com.sun.local_repo.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by sunxiaoling on 16/6/30.
 */
public class CommonUtil {
    public static void showMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
