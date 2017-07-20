package com.example.didi.slidebutton;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;

public class NewSlideButtonSampleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_slide_button_sample);
        final NewSlideButton slideButton = (NewSlideButton) findViewById(R.id.slide_button);
        final SwitchCompat switchCompat = (SwitchCompat) findViewById(R.id.switch_button);

        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    slideButton.startLoading();
                } else {
                    slideButton.stopLoading();
                }
            }
        });

        slideButton.setStyle(new NewSlideButton.CustomStyle(
                R.drawable.sofa_slide_button_foreground_blue,
                R.drawable.sofa_slide_button_background_blue,
                R.color.sofa_color_slide_button_shimmer_blue));
    }
}
