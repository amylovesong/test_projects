package com.sun.checkticketslidebutton;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
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
    private float mForegroundViewInitialX;
    private int mActionConfirmThreshold;
    private int mActionClickThreshold;
    /**
     * 滑动操作的结果：是否确认当前操作
     */
    private boolean mConfirmed = false;
    /**
     * 当前的验票状态
     */
    private boolean mChecked = false;

    private Animator mForegroundViewTranslateAnimator;
    private Animator mForegroundViewClickAnimator;
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

        mActionClickThreshold = getResources().getDimensionPixelSize(R.dimen.provider_3dp);
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.checkticket_slide_view, this);
        this.mForegroundView = findViewById(R.id.layout_foreground_view);
        this.mBackgroundView = findViewById(R.id.layout_background_view);

        this.mForegroundIcon = (ImageView) findViewById(R.id.layout_foreground_icon);
        this.mBackgroundIcon = (ImageView) findViewById(R.id.layout_background_icon);
        this.mPhoneNumberTextView = (TextView) findViewById(R.id.tv_phone_number);
        this.mBackgroundText = (TextView) findViewById(R.id.layout_background_text);

        this.mBackgroundInfoView = findViewById(R.id.layout_background_info);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        logMessage("onTouchEvent event: " + event);
//        logMessage(String.format(Locale.getDefault(),
//                "onTouchEvent x: %f, y: %f, rawX: %f, rawY: %f", event.getX(), event.getY(), event.getRawX(), event.getRawY()));
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                endAllAnimations("onTouchEvent ACTION_DOWN");
                mStartX = (int) event.getX();
                mDeltaX = 0;
                startBackgroundInfoViewAnimation();
                break;
            case MotionEvent.ACTION_MOVE:
                mCurX = (int) event.getX();
                mDeltaX = mCurX - mStartX;
                logMessage(String.format(Locale.getDefault(), "onTouchEvent ACTION_MOVE mStartX: %d mCurX: %d mDeltaX: %d mActionClickThreshold: %d",
                        mStartX, mCurX, mDeltaX, mActionClickThreshold));
                // 是否超过点击阈值
                if (mDeltaX > mActionClickThreshold) {
                    mForegroundView.setX(mDeltaX);
//                    logMessage(String.format(Locale.getDefault(), "onTouchEvent ACTION_MOVE after mForegroundView.getX(): %f", mForegroundView.getX()));
                    // 滑动过程中，根据滑动距离是否达到确认操作阈值更改前景view的状态
                    if (mForegroundView.getX() >= mActionConfirmThreshold) {
                        // 变为目标状态
                        updateForegroundViewByCheckedState(!mChecked);
                        mForegroundIcon.setVisibility(INVISIBLE);
                    } else {// 回到当前状态
                        updateForegroundViewByCheckedState(mChecked);
                    }
                } else {
                    mForegroundView.setX(mForegroundViewInitialX);
                }
                break;
            case MotionEvent.ACTION_UP:
                logMessage("onTouchEvent ACTION_UP mDeltaX: " + mDeltaX);
                // 向左滑动时不做任何处理
                if (mDeltaX <= -mActionClickThreshold) {
                    break;
                }
                // 先判定是否为点击事件
                if (mDeltaX <= mActionClickThreshold) {
                    mForegroundViewClickAnimator = startForegroundViewClickAnimation();
                    break;
                }
                // 超过操作确认阈值，判定为确认本次操作
                if (mForegroundView.getX() >= mActionConfirmThreshold) {
                    mConfirmed = true;
                }
                logMessage("onTouchEvent ACTION_UP mConfirmed: " + mConfirmed);
                mForegroundViewTranslateAnimator = startForegroundViewTranslateAnimation(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        logMessage("ACTION_UP mForegroundViewTranslateAnimator onAnimationStart[" + animation + "]");
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        logMessage("ACTION_UP mForegroundViewTranslateAnimator onAnimationEnd[" + animation + "]"
                                + " mForegroundView.getX(): " + mForegroundView.getX());
                        // 确认本次操作，更改check状态
                        if (mConfirmed) {
                            setChecked(!mChecked);
                            if (mListener != null) {
                                mListener.onCheckStateChanged(CheckTicketSlideView.this, mChecked);
                            }
                            mConfirmed = false;
                        }
                    }
                });
                break;
        }
        return true;
    }

    private void endAllAnimations(final String source) {
        endForegroundViewTranslateAnimation(source);
        endForegroundViewClickAnimation(source);
        endBackgroundInfoViewAnimation(source);
    }

    private Animator startForegroundViewTranslateAnimation(Animator.AnimatorListener listener) {
        final Animator translateAnimator = ObjectAnimator.ofFloat(mForegroundView, "X", mForegroundView.getX(), mForegroundViewInitialX);
        translateAnimator.setDuration(500);
        translateAnimator.setInterpolator(new AccelerateInterpolator());
        if (listener != null) {
            translateAnimator.addListener(listener);
        }
        translateAnimator.start();

        return translateAnimator;
    }

    private void endForegroundViewTranslateAnimation(final String source) {
        final boolean isRunning = mForegroundViewTranslateAnimator != null && mForegroundViewTranslateAnimator.isRunning();
        logMessage("["+ source + "]endForegroundView[Translate]Animation isRunning(): " + isRunning);
        if (isRunning) {
            mForegroundViewTranslateAnimator.end();
        }
    }

    private Animator startForegroundViewClickAnimation() {
        final float animationStartValue = getResources().getDimensionPixelSize(R.dimen.provider_40dp);
        final Animator animator = ObjectAnimator.ofFloat(mForegroundView, "X", animationStartValue, 0);
        animator.setInterpolator(new BounceInterpolator());
        animator.setDuration(500);
        animator.start();
        return animator;
    }

    private void endForegroundViewClickAnimation(final String source) {
        final boolean isRunning = mForegroundViewClickAnimator != null && mForegroundViewClickAnimator.isRunning();
        logMessage("["+ source + "]endForegroundView[Click]Animation isRunning(): " + isRunning);
        if (isRunning) {
            mForegroundViewClickAnimator.end();
        }
    }

    private void startBackgroundInfoViewAnimation() {
        mBackgroundInfoView.startAnimation(mBackgroundInfoInAnimation);
    }

    private void endBackgroundInfoViewAnimation(final String source) {
        logMessage("["+ source + "]endBackgroundInfoViewAnimation");
        mBackgroundInfoInAnimation.cancel();
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
            mBackgroundView.setBackgroundColor(getResources().getColor(R.color.provider_color_4a4c5b));
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
                + " mForegroundViewInitialX: " + mForegroundViewInitialX + " mForegroundView.getX(): " + mForegroundView.getX()
                + " mActionConfirmThreshold: " + mActionConfirmThreshold + " mBackgroundText.getRight(): " + mBackgroundText.getRight());
        if (!this.mInitialized) {
            this.mForegroundViewInitialX = mForegroundView.getX();
            this.mActionConfirmThreshold = mBackgroundText.getRight();
            this.mInitialized = true;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        endAllAnimations("onDetachedFromWindow");
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
