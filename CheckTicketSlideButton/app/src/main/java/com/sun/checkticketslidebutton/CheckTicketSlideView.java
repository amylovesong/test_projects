package com.sun.checkticketslidebutton;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Locale;

/**
 * @author sxl  (sunxiaoling@didichuxing.com)
 * @date 2017/3/16 18:14
 */
public class CheckTicketSlideView extends RelativeLayout {

    public interface OnCheckStateChangedListener {
        void onCheckStateChanged(final CheckTicketSlideView view, final boolean checked);
    }

    private static final float SLIDE_DISTANCE_RATIO = 0.33f;

    private View mRootView;
    private View mForegroundView;
    private View mBackgroundView;
    private ImageView mForegroundIcon;
    private ImageView mBackgroundIcon;
    private TextView mPhoneNumberTextView;
    private TextView mBackgroundText;
    private View mBackgroundInfoView;

    private int mStartX;
    private int mCurX;
    private int mDeltaX;
    private boolean mInitialized = false;
    private int mForegroundViewInitialX;
    private int mForegroundViewWidth;
    /**
     * 滑动操作的结果：是否确认当前操作
     */
    private boolean mConfirmed = false;
    /**
     * 当前的验票状态
     */
    private boolean mChecked = false;
    /**
     * 前景view的动画正在执行；带delay时间的animator在start之后{@link Animator#isRunning()}在等待时间段内并不会return true，因此需要自定义flag
     */
    private boolean mForegroundViewAnimatorRunning;

    private Animator mForegroundViewAnimator;
    private Animation mBackgroundInfoInAnimation;

    private OnCheckStateChangedListener mListener;

    public CheckTicketSlideView(Context context) {
        this(context, null);
    }

    public CheckTicketSlideView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initView();
        mBackgroundInfoInAnimation = AnimationUtils.loadAnimation(context, R.anim.sofa_check_ticket_background_info_in);
        mBackgroundInfoInAnimation.setInterpolator(new AnticipateOvershootInterpolator());

    }

    private void initView() {
        this.mRootView = LayoutInflater.from(getContext()).inflate(R.layout.checkticket_slide_view, this);
        this.mForegroundView = this.mRootView.findViewById(R.id.layout_foreground_view);
        this.mBackgroundView = this.mRootView.findViewById(R.id.layout_background_view);

        this.mForegroundIcon = (ImageView) this.mRootView.findViewById(R.id.layout_foreground_icon);
        this.mBackgroundIcon = (ImageView) this.mRootView.findViewById(R.id.layout_background_icon);
        this.mPhoneNumberTextView = (TextView) this.mRootView.findViewById(R.id.tv_phone_number);
        this.mBackgroundText = (TextView) this.mRootView.findViewById(R.id.layout_background_text);

        this.mBackgroundInfoView = this.mRootView.findViewById(R.id.layout_background_info);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        logMessage("onTouchEvent event: " + event);
//        logMessage(String.format(Locale.getDefault(),
//                "onTouchEvent x: %f, y: %f, rawX: %f, rawY: %f", event.getX(), event.getY(), event.getRawX(), event.getRawY()));
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                endForegroundViewAnimationIfNecessary("onTouchEvent ACTION_DOWN");
                mStartX = (int) event.getX();
                mDeltaX = 0;
                startBackgroundInfoViewAnimation();
                break;
            case MotionEvent.ACTION_MOVE:
                mCurX = (int) event.getX();
                // 不可向左滑动
                if (mCurX - mStartX < mDeltaX) {
                    break;
                }
                mDeltaX = mCurX - mStartX;
                logMessage(String.format(Locale.getDefault(), "onTouchEvent ACTION_MOVE mStartX: %d mCurX: %d mDeltaX: %d", mStartX, mCurX, mDeltaX));
                if (mDeltaX > 0) {
                    mForegroundView.setX(mDeltaX);
                    logMessage(String.format(Locale.getDefault(), "onTouchEvent ACTION_MOVE after mForegroundView.getX(): %f", mForegroundView.getX()));
                }
                // 滑动过程中，当滑动距离达到阈值时，就认为确认操作，不需要等到ACTION_UP
                if (mForegroundView.getX() >= SLIDE_DISTANCE_RATIO * mForegroundViewWidth) {
                    if (!mConfirmed) {
                        mConfirmed = true;
//                        updateForegroundViewStateOnSlide();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                logMessage("onTouchEvent ACTION_UP mConfirmed: " + mConfirmed);
                if (mConfirmed) {
                    this.mForegroundViewAnimator = confirmAction();
                } else {// cancel action
                    this.mForegroundViewAnimator = cancelAction();
                }
                break;
        }
        return true;
    }

    private Animator confirmAction() {
        mForegroundViewAnimatorRunning = true;
        final Animator translateAnimator = ObjectAnimator.ofFloat(mForegroundView, "X", mForegroundView.getX(), mForegroundViewWidth);
        translateAnimator.setDuration(500);
//        translateAnimator.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//                super.onAnimationStart(animation);
//                logMessage("[confirmAction] translateAnimator onAnimationStart[" + animation + "]");
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                super.onAnimationEnd(animation);
//                logMessage("[confirmAction] translateAnimator onAnimationEnd[" + animation + "]"
//                        + " mForegroundView.getX(): " + mForegroundView.getX());
//            }
//        });

        final Animator alphaAnimator = ObjectAnimator.ofFloat(mForegroundView, "Alpha", 0f, 1f);
        alphaAnimator.setStartDelay(1000);
        alphaAnimator.setDuration(200);
        alphaAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                logMessage("[confirmAction] alphaAnimator onAnimationStart[" + animation + "]");
                updateForegroundViewByCheckedState(!mChecked);
                mForegroundView.setX(mForegroundViewInitialX);
                mForegroundView.setAlpha(0f);
            }
        });

        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(translateAnimator, alphaAnimator);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                logMessage("[confirmAction] animatorSet onAnimationEnd[" + animation + "]"
                        + " mForegroundView.getX(): " + mForegroundView.getX());
                setChecked(!mChecked);
                if (mListener != null) {
                    mListener.onCheckStateChanged(CheckTicketSlideView.this, mChecked);
                }
                mConfirmed = false;
                mForegroundViewAnimatorRunning = false;
            }
        });
        animatorSet.start();

        return animatorSet;
    }

    private Animator cancelAction() {
        final Animator translateAnimator = ObjectAnimator.ofFloat(mForegroundView, "X", mForegroundView.getX(), mForegroundViewInitialX);
        translateAnimator.setDuration(500);
        translateAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                logMessage("[cancelAction] translateAnimator onAnimationStart[" + animation + "]");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                logMessage("[cancelAction] translateAnimator onAnimationEnd[" + animation + "]"
                        + " mForegroundView.getX(): " + mForegroundView.getX());
            }
        });
        translateAnimator.start();

        return translateAnimator;
    }

    private void endForegroundViewAnimationIfNecessary(final String source) {
        final boolean animatorRunning = mForegroundViewAnimator != null && mForegroundViewAnimator.isRunning();
        logMessage("["+ source + "]endForegroundViewAnimationIfNecessary animatorRunning: " + animatorRunning
                + " mForegroundViewAnimatorRunning: " + mForegroundViewAnimatorRunning);
        if ((animatorRunning || mForegroundViewAnimatorRunning)) {
            if (mForegroundViewAnimator != null) {
                mForegroundViewAnimator.end();
            }
        }
    }

    /**
     * 滑动时，当满足确认操作条件时，更新前景View
     * 如果当前checked=true，表示即将取消勾选
     * 否则，表示即将勾选
     */
    private void updateForegroundViewStateOnSlide() {
//        mForegroundView.setBackgroundColor(Color.TRANSPARENT);
//        mPhoneNumberTextView.setTextColor(Color.WHITE);
//        mForegroundIcon.setVisibility(INVISIBLE);
    }

    private void updateViewByCheckedState(final boolean checked) {
        updateForegroundViewByCheckedState(checked);
        updateBackgroundViewByCheckedState(checked);
    }

    private void updateForegroundViewByCheckedState(final boolean checked) {
        logMessage("updateForegroundView mChecked: " + mChecked + " new Checked: " + checked);
        mForegroundView.setBackgroundColor(Color.WHITE);
        mForegroundIcon.setVisibility(VISIBLE);
        if (checked) {
            mForegroundIcon.setImageResource(R.drawable.sofa_ticket_ic_checked);
            mPhoneNumberTextView.setTextColor(getResources().getColor(R.color.provider_color_orange));
            // TODO: 2017/3/17 补票View
        } else {
            mForegroundIcon.setImageResource(R.drawable.sofa_ticket_ic_unchekced);
            mPhoneNumberTextView.setTextColor(getResources().getColor(R.color.provider_color_font_33));
            // TODO: 2017/3/17 补票View
        }
    }

    private void updateBackgroundViewByCheckedState(boolean checked) {
        logMessage("updateBackgroundView mChecked: " + mChecked + " new Checked: " + checked);
        if (checked) {
            // 背景色
            mBackgroundView.setBackgroundColor(getResources().getColor(R.color.provider_color_f1594e));
            mBackgroundIcon.setImageResource(R.drawable.sofa_ticket_ic_unchecking);
            mBackgroundText.setText("已取消");
        } else {
            mBackgroundView.setBackgroundColor(getResources().getColor(R.color.provider_color_orange));
            mBackgroundIcon.setImageResource(R.drawable.sofa_ticket_ic_checking);
            mBackgroundText.setText("已上车");
        }
    }

    private void logMessage(final String message) {
        Log.d("CheckTicketSlideView", message);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        logMessage("onLayout mInitialized: " + mInitialized
                + " mForegroundViewInitialX: " + mForegroundViewInitialX
                + " mForegroundView.getX(): " + mForegroundView.getX()
                + " mForegroundView.getWidth(): " + mForegroundView.getWidth());
        if (!this.mInitialized) {
            this.mForegroundViewInitialX = (int) mForegroundView.getX();
            this.mForegroundViewWidth = mForegroundView.getWidth();
            this.mInitialized = true;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        endForegroundViewAnimationIfNecessary("onDetachedFromWindow");
        mBackgroundInfoInAnimation.cancel();
    }

    public void startBackgroundInfoViewAnimation() {
        mBackgroundInfoView.startAnimation(mBackgroundInfoInAnimation);
    }

    public void setPhoneNumber(final CharSequence text) {
        this.mPhoneNumberTextView.setText(text);
    }

    public void setChecked(final boolean checked) {
        logMessage("setChecked: " + checked);
        this.mChecked = checked;
        updateViewByCheckedState(checked);
    }

    public void setOnCheckStateChangedListener(OnCheckStateChangedListener listener) {
        this.mListener = listener;
    }
}
