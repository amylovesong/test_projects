package com.sun.uninstall.demo;

import java.io.File;
import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class SDCardListenerService extends Service {
	private static final String TAG = SDCardListenerService.class
			.getSimpleName();
	SDCardListener[] listeners;

	@Override
	public void onCreate() {
		super.onCreate();
		SDCardListener[] listeners = {
				new SDCardListener("/data/data/com.sun.uninstall.demo", this),
				new SDCardListener(Environment.getExternalStorageDirectory()
						+ File.separator + "1.txt", this) };
		this.listeners = listeners;

		Log.i(TAG, "=============onCreate==============");
		for (SDCardListener listener : listeners) {
			listener.startWatching();
		}

		File file = new File(Environment.getExternalStorageDirectory()
				+ File.separator + "1.txt");
		Log.i(TAG, "check file");
		if (file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		for (SDCardListener listener : listeners) {
			listener.stopWatching();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
