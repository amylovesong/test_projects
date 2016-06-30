package com.sun.appformaven;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.sun.util.Utils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.showMessage(getApplicationContext(), "Utils from local repo");
    }
}
