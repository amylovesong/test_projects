package com.sun.appformaven;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        CommonUtil.showMessage(getApplicationContext(), "Utils from local repo");
        com.sun.bintray_repo.utils.CommonUtil.showMessage(getApplicationContext(),
                "Utils from bintray repo");
    }
}
