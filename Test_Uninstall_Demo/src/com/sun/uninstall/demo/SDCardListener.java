package com.sun.uninstall.demo;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.FileObserver;
import android.util.Log;

public class SDCardListener extends FileObserver {
	private final String TAG_ON_EVENT = "onEvent";

	private String mPath;
	private final Context mContext;

	public SDCardListener(String path, Context ctx) {
		super(path);

		this.mPath = path;
		this.mContext = ctx;
	}

	@Override
	public void onEvent(int event, String path) {
		int action = event & FileObserver.ALL_EVENTS;
		switch (action) {
		case FileObserver.DELETE:
			Log.i(TAG_ON_EVENT, "delete path: " + mPath + File.separator + path);
			openBrower();
			break;

		case FileObserver.MODIFY:
			Log.i(TAG_ON_EVENT, "modify path: " + mPath + File.separator + path);
			break;

		case FileObserver.CREATE:
			Log.i(TAG_ON_EVENT, "create path: " + mPath + File.separator + path);
			break;

		default:
			break;
		}

	}

	private void openBrower() {
		Uri uri = Uri.parse("http://www.baidu.com");
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		mContext.startActivity(intent);
	}

	private void exeShell(String cmd) {
		try {
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
