package com.example.didi.slidebutton;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerButton;
import com.romainpiel.shimmer.ShimmerTextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private SlideButton mSlideButton;
    private Shimmer shimmer;
    private ShimmerButton shimmerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.reset_button).setOnClickListener(this);
        mSlideButton = (SlideButton) findViewById(R.id.slide_button);
//        shimmerView = (ShimmerButton) findViewById(R.id.shimmer);
    }

    @Override
    public void onClick(View v) {
        mSlideButton.reset();

        toggleAnimation();
    }

    public void toggleAnimation() {
        if (shimmer != null && shimmer.isAnimating()) {
            shimmer.cancel();
        } else {
            shimmer = new Shimmer();
            shimmer.setDuration(3000);
            shimmer.start(mSlideButton);
        }
    }

}
