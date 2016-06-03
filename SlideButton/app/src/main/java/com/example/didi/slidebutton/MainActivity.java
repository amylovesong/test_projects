package com.example.didi.slidebutton;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;

import com.romainpiel.shimmer.Shimmer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SlideButton.OnSlideActionListener {
    private SlideButton mSlideButton;
    private Shimmer shimmer;
    private ProgressBar mLoadingView;
    private ObjectAnimator animator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.reset_button).setOnClickListener(this);
        mSlideButton = (SlideButton) findViewById(R.id.slide_button);
        mSlideButton.setOnSlideActionListener(this);

        mLoadingView = (ProgressBar) findViewById(R.id.slide_button_progress);
        mLoadingView.setAlpha(0);
    }

    @Override
    public void onClick(View v) {
        mSlideButton.reset();
        if (animator != null) {
            animator.cancel();
        }
        mLoadingView.setAlpha(0);

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

    @Override
    public void onActionConfirmed() {
        animator = ObjectAnimator.ofFloat(mLoadingView, "Alpha", 0, 255);
        animator.setDuration(2000);
        animator.start();
    }
}
