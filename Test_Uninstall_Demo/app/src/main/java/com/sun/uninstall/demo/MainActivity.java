package com.sun.uninstall.demo;

import com.example.test_uninstall_demo.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Intent intent = new Intent(this, SDCardListenerService.class);
		startService(intent);

		NativeClass nativeClass = new NativeClass();
		nativeClass.init();
	}

	static {
		Log.i(TAG, "load jni lib");
		System.loadLibrary("uninstall_demo");
	}
}
