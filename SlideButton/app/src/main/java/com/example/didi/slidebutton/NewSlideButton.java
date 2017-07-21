package com.example.didi.slidebutton;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.didichuxing.sofa.animation.Animator;
import com.didichuxing.sofa.animation.SofaAnimatorCompat;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

/**
 * @author sxl  (sunxiaoling@didichuxing.com)
 * @date 2017/7/19 18:28
 */
public class NewSlideButton extends FrameLayout {
    private final float ACTION_CONFIRM_DISTANCE_FRACTION = 0.3f;

    private static final String TAG = "NewSlideButton";
    private ShimmerTextView foregroundView;
    private View backgroundView;
    private View loadingView;

    private Shimmer mForegroundShimmer;

    /**
     * 滑动时MotionEvent的各项数值
     */
    private float mRawXStart;
    private float mRawXMove;
    private float mRawXEnd;
    private float mMoveDeltaX;
    /**
     * 初始状态的x坐标
     */
    private float mViewInitialX;
    private int mViewWidth;

    private float mSlideX;

    private boolean mLoading = false;
    private Animator loadingAnimator;

    private CustomStyle mStyle = CustomStyle.DEFAULT_STYLE;

    public NewSlideButton(Context context) {
        this(context, null);
    }

    public NewSlideButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NewSlideButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        final View rootView = inflate(getContext(), R.layout.new_slide_button, this);
        foregroundView = (ShimmerTextView) findViewById(R.id.foreground_view);
        backgroundView = rootView;/*findViewById(R.id.background_view);*/
        loadingView = findViewById(R.id.loading_view);

        foregroundView.setClickable(false);
        loadingView.setVisibility(GONE);
        setText("右滑开始自由接单");
        setStyle(mStyle);

        initShimmer();
        initLoadingAnimation();

        post(new Runnable() {
            @Override
            public void run() {
                mViewInitialX = 0;
                mViewWidth = getWidth();
                logMsg("post run mViewInitialX: " + mViewInitialX + " mViewWidth: " + mViewWidth);
            }
        });
    }

    private void initShimmer() {
        mForegroundShimmer = new Shimmer();
        mForegroundShimmer.setDuration(3000);
    }

    private void initLoadingAnimation() {
        loadingAnimator = SofaAnimatorCompat.playTogether(
                SofaAnimatorCompat.play(loadingView).fadeIn().duration(500).build(),
                SofaAnimatorCompat.play(loadingView).rotateCW().repeatInfinite().decelerate().duration(800).build());
    }

    private void startShimmer() {
        logMsg("startShimmer");
        mForegroundShimmer.start(foregroundView);
    }

    private void stopShimmer() {
        logMsg("stopShimmer");
        mForegroundShimmer.cancel();
    }

    public void startLoading() {
        logMsg("startLoading mLoading: " + mLoading);
        if (mLoading) {
            return;
        }
        mLoading = true;
        loadingView.setVisibility(VISIBLE);
        loadingAnimator.start();
    }

    public void stopLoading() {
        logMsg("stopLoading mLoading: " + mLoading);
        if (!mLoading) {
            return;
        }
        loadingAnimator.stop();
        loadingView.setVisibility(GONE);
        mLoading = false;
    }

    public void setShimmerColor(int colorResId) {
        foregroundView.setReflectionColor(getContext().getResources().getColor(colorResId));
    }

    public void setText(CharSequence text) {
        foregroundView.setText(text);
    }

    public void setText(int resId) {
        foregroundView.setText(resId);
    }

    public void setStyle(CustomStyle style) {
        this.mStyle = style;
        onStyleChanged();
    }

    private void onStyleChanged() {
        setViewResource(mStyle.getForegroundResId(), mStyle.getBackgroundResId());
        setShimmerColor(mStyle.getShimmerColorResId());
    }

    private void logMsg(String s) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, s);
        }
    }

    @Override
    public final boolean onTouchEvent(MotionEvent event) {
        logMsg("onTouchEvent event: " + event + " mLoading: " + mLoading);
        if (mLoading) {
            return true;
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mRawXStart = event.getRawX();
                logMsg("ACTION_DOWN mRawXStart: " + mRawXStart);
                onActionDown();
                break;
            case MotionEvent.ACTION_UP:
                mRawXEnd = event.getRawX();
                mMoveDeltaX = mRawXEnd - mRawXStart;
                logMsg("ACTION_UP mRawXEnd: " + mRawXEnd + " mMoveDeltaX:" + mMoveDeltaX + " mViewWidth: " + mViewWidth);
                if (mMoveDeltaX <= mViewWidth * ACTION_CONFIRM_DISTANCE_FRACTION){
                    // 手势滑动的距离未达到设定的阈值，取消本次操作
                    logMsg("action not confirmed");
                    cancelActionWithAnimation();
                } else {
                    logMsg("action confirmed");
                    // 手势滑动的距离达到设定的阈值，确认本次操作
                    confirmActionWithAnimation();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                mRawXMove = event.getRawX();
                mMoveDeltaX = mRawXMove - mRawXStart;
                logMsg("ACTION_MOVE mRawXMove: " + mRawXMove + " mRawXStart: " + mRawXStart + " mMoveDeltaX: " + mMoveDeltaX);
                // 判定是否为滑动操作
                if (Math.abs(mMoveDeltaX) > mViewWidth * 0.02f) {
                    // 用滑动的delta值来更新x坐标，即将更新的值不可小于初始值（即只可滑动退回到初始位置，不可向左滑出）
                    final float targetX = Math.max(mMoveDeltaX, mViewInitialX);
                    onActionMove();
                    setSlideX(targetX);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                onActionCancel();
                break;
        }
        return true;
    }

    private void confirmActionWithAnimation() {
        final float curX = mSlideX;
        final float targetX = mViewInitialX + mViewWidth;
        final int duration = (int) (Math.abs(targetX - curX) / mViewWidth * 1000);
        SofaAnimatorCompat
                .play(NewSlideButton.this).property("SlideX", curX, targetX).duration(duration).decelerate()
                .withListener(new com.didichuxing.sofa.animation.AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animator, View view) {
                        super.onAnimationEnd(animator, view);
                        actionConfirmed();
                    }
                })
                .build().start();
    }

    private void cancelActionWithAnimation() {
        final float curX = mSlideX;
        final float targetX = mViewInitialX;
        if (curX == targetX) {// 没有滑动，直接处理为取消操作
            onActionCancel();
        } else {
            final int duration = (int) (Math.abs(targetX - curX) / mViewWidth * 1000);
            SofaAnimatorCompat
                    .play(NewSlideButton.this).property("SlideX", curX, targetX).duration(duration)
                    .withListener(new com.didichuxing.sofa.animation.AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animator, View view) {
                            super.onAnimationEnd(animator, view);
                            onActionCancel();
                        }
                    })
                    .build().start();
        }
    }

    /*private void updateForegroundX(float targetX) {
        setSlideX(targetX);
    }*/

    private void setSlideX(float x) {
        mSlideX = x;
        foregroundView.setX(x);
    }

    private void setViewResource(int foregroundResId, int backgroundResId) {
        foregroundView.setBackgroundResource(foregroundResId);
        backgroundView.setBackgroundResource(backgroundResId);
    }

    private void ensureForegroundAtInitialX() {
        logMsg("ensureForegroundViewPosition mSlideX: " + mSlideX + ", mViewInitialX: " + mViewInitialX);
        if (mSlideX != mViewInitialX) {
            setSlideX(mViewInitialX);
        }
    }

    private void onActionDown() {
        logMsg("actionDown");
        stopShimmer();
    }

    private void onActionMove() {
        logMsg("actionMove");
    }

    private void onActionCancel() {
        logMsg("actionCancel");
        startShimmer();
    }

    private void actionConfirmed() {
        logMsg("actionConfirmed");
        startLoading();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startShimmer();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopShimmer();
    }

    public static class CustomStyle {
        static final CustomStyle DEFAULT_STYLE = new CustomStyle(
                R.drawable.sofa_slide_button_foreground_orange,
                R.drawable.sofa_slide_button_background_orange,
                R.color.sofa_color_slide_button_shimmer_orange);

        private int foregroundResId;
        private int backgroundResId;
        private int shimmerColorResId;

        CustomStyle(int normalResId, int slideResId, int shimmerColorResId) {
            this.foregroundResId = normalResId;
            this.backgroundResId = slideResId;
            this.shimmerColorResId = shimmerColorResId;
        }

        int getForegroundResId() {
            return foregroundResId;
        }

        int getBackgroundResId() {
            return backgroundResId;
        }

        public int getShimmerColorResId() {
            return shimmerColorResId;
        }
    }
}
