package com.example.didi.slidebutton;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.romainpiel.shimmer.Shimmer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SlideButton.OnSlideActionListener {
    private SlideButton mSlideButton;
    private Shimmer shimmer;
//    private ProgressBar mLoadingView;
    private ObjectAnimator animator;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.reset_button).setOnClickListener(this);
        mSlideButton = (SlideButton) findViewById(R.id.slide_button);
        mSlideButton.setOnSlideActionListener(this);

//        mLoadingView = (ProgressBar) findViewById(R.id.slide_button_progress);
//        mLoadingView.setAlpha(0);
        mHandler = new Handler();
    }

    @Override
    public void onClick(View v) {
        mSlideButton.setStyleOrange();
//        if (animator != null) {
//            animator.cancel();
//        }
//        mLoadingView.setAlpha(0);

//        toggleAnimation();
    }

    public void toggleAnimation() {
        if (isShimmerAnimating()) {
            stopShimmer();
        } else {
            startShimmer();
        }
    }

    private void stopShimmer() {
        if (isShimmerAnimating()) {
            shimmer.cancel();
        }
    }

    private boolean isShimmerAnimating() {
        return shimmer != null && shimmer.isAnimating();
    }

    private void startShimmer() {
        if (shimmer == null) {
            shimmer = new Shimmer();
            shimmer.setDuration(3000);
        }
        shimmer.start(mSlideButton);
    }

    @Override
    public void onTouchActionDown(SlideButton button) {

    }

    @Override
    public void onTouchActionMove(SlideButton button) {

    }

    @Override
    public void onActionCancel(SlideButton button) {

    }

    @Override
    public void onActionConfirmed(SlideButton button) {

    }
}
