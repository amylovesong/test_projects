package com.sun.checkticketslidebutton;

import android.animation.Animator;
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
    private static final float SLIDE_DISTANCE_RATIO = 0.33f;

    private View mRootView;
    private View mForegroundView;
    private View mBackgroundView;

    private ImageView mForegroundIcon;
    private ImageView mBackgroundIcon;
    private TextView mPhoneNumberTextView;
    private TextView mBackgroundText;

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

    private Animator mForegroundViewAnimator;
    private Animation mBackgroundIconInAnimation;

    public CheckTicketSlideView(Context context) {
        this(context, null);
    }

    public CheckTicketSlideView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initView();
        mBackgroundIconInAnimation = AnimationUtils.loadAnimation(context, R.anim.sofa_check_ticket_background_check_icon_in);
        mBackgroundIconInAnimation.setInterpolator(new AnticipateOvershootInterpolator());

    }

    private void initView() {
        this.mRootView = LayoutInflater.from(getContext()).inflate(R.layout.checkticket_slide_view, this);
        this.mForegroundView = this.mRootView.findViewById(R.id.layout_foreground_view);
        this.mBackgroundView = this.mRootView.findViewById(R.id.layout_background_view);

        this.mForegroundIcon = (ImageView) this.mRootView.findViewById(R.id.layout_foreground_icon);
        this.mBackgroundIcon = (ImageView) this.mRootView.findViewById(R.id.layout_background_icon);
        this.mPhoneNumberTextView = (TextView) this.mRootView.findViewById(R.id.tv_phone_number);
        this.mBackgroundText = (TextView) this.mRootView.findViewById(R.id.layout_background_text);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        logMessage("onTouchEvent event: " + event);
//        logMessage(String.format(Locale.getDefault(),
//                "onTouchEvent x: %f, y: %f, rawX: %f, rawY: %f", event.getX(), event.getY(), event.getRawX(), event.getRawY()));
        mCurX = (int) event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                endForegroundViewAnimationIfNecessary("onTouchEvent ACTION_DOWN");
                mStartX = (int) event.getX();
                startBackgroundIconAnimation();
                break;
            case MotionEvent.ACTION_MOVE:
                mDeltaX = mCurX - mStartX;
                logMessage(String.format(Locale.getDefault(), "onTouchEvent ACTION_MOVE mStartX: %d mCurX: %d mDeltaX: %d", mStartX, mCurX, mDeltaX));
                // TODO: 2017/3/17 先向左再向右滑动时，从向右滑动开始的那一刻更新mStartX
                if (mDeltaX > 0) {
                    mForegroundView.setX(mDeltaX);
                    logMessage(String.format(Locale.getDefault(), "onTouchEvent ACTION_MOVE after mForegroundView.getX(): %f", mForegroundView.getX()));
                }
                // 滑动过程中，当滑动距离达到阈值时，就认为确认操作，不需要等到ACTION_UP
                if (mForegroundView.getX() >= SLIDE_DISTANCE_RATIO * mForegroundViewWidth) {
                    if (!mConfirmed) {
                        mConfirmed = true;
                        updateForegroundViewStateOnSlide();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                logMessage("onTouchEvent ACTION_UP mConfirmed: " + mConfirmed);
                if (mConfirmed) {
                    updateForegroundViewByCheckedState(!mChecked);
                }
                mForegroundViewAnimator = ObjectAnimator.ofFloat(mForegroundView, "X", mForegroundView.getX(), mForegroundViewInitialX);
                mForegroundViewAnimator.setDuration(2000);
                mForegroundViewAnimator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        logMessage("ACTION_UP mForegroundViewAnimator onAnimationStart[" + animation + "]");
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        logMessage("ACTION_UP mForegroundViewAnimator onAnimationEnd[" + animation + "]"
                                + " mForegroundView.getX(): " + mForegroundView.getX());
                        // 根据滑动距离得到的确认状态更新前景view
                        if (mConfirmed) {
                            setChecked(!mChecked);
                            mConfirmed = false;
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                mForegroundViewAnimator.start();
                break;
        }
        return true;
    }

    private void endForegroundViewAnimationIfNecessary(final String source) {
        boolean isForegroundViewAnimatorRunning = mForegroundViewAnimator != null && mForegroundViewAnimator.isRunning();
        logMessage("["+ source + "]endForegroundViewAnimationIfNecessary mForegroundViewAnimator.isRunning(): " + isForegroundViewAnimatorRunning);
        if (isForegroundViewAnimatorRunning) {
            mForegroundViewAnimator.end();
        }
    }

    /**
     * 滑动时，当满足确认操作条件时，更新前景View
     * 如果当前checked=true，表示即将取消勾选
     * 否则，表示即将勾选
     */
    private void updateForegroundViewStateOnSlide() {
        mForegroundView.setBackgroundColor(Color.TRANSPARENT);
        mPhoneNumberTextView.setTextColor(Color.WHITE);
        mForegroundIcon.setVisibility(INVISIBLE);
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
        mBackgroundIconInAnimation.cancel();
    }

    public void startBackgroundIconAnimation() {
        mBackgroundIcon.startAnimation(mBackgroundIconInAnimation);
    }

    public void setPhoneNumber(final CharSequence text) {
        this.mPhoneNumberTextView.setText(text);
    }

    public void setChecked(final boolean checked) {
        logMessage("setChecked: " + checked);
        this.mChecked = checked;
        updateViewByCheckedState(checked);
    }

    private void logMessage(final String message) {
        Log.d("CheckTicketSlideView", message);
    }
}
