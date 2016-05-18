package com.example.didi.slidebutton;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;

import com.romainpiel.shimmer.ShimmerViewBase;
import com.romainpiel.shimmer.ShimmerViewHelper;

/**
 * Created by sxl on 16/5/15.
 */
public class SlideButton extends Button implements ShimmerViewBase {
    private static final String TAG = SlideButton.class.getSimpleName();
    private static final float ACTION_CONFIRM_DISTANCE_FRACTION = 0.3f;

    /**
     * 由于滑动的时候会更新View的x坐标，因此使用MotionEvent的rawX来计算
     */
    private float mRawXStart;
    private float mRawXMove;
    private float mRawXEnd;
    private float mPreviousRawXMove;
    private float mMoveDeltaX;
    private float mPreMoveDeltaX = 0f;
    /**
     * View 初始状态的x坐标（滑动时需考虑padding，margin的情况）
     */
    private float mViewInitialX;
    private int mViewWidth;

    private OnSlideActionListener mOnSlideActionListener;

    private ShimmerViewHelper mShimmerViewHelper;
    private boolean mStartMove;

    public SlideButton(Context context) {
        super(context);
        init(null);
    }

    public SlideButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public SlideButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        mShimmerViewHelper = new ShimmerViewHelper(this, getPaint(), attrs);
        mShimmerViewHelper.setPrimaryColor(getCurrentTextColor());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mViewInitialX = getX();
            mViewWidth = getWidth();
            logMsg("onLayout mViewInitialX: " + mViewInitialX + " mViewWidth: " + mViewWidth);

            // Set drawRight for arrows
            final Paint paint=new Paint();
            final Rect textBounds = new Rect();
            paint.setTextSize(getTextSize());
            paint.getTextBounds(getText().toString(), 0, getText().length(), textBounds);
            logMsg("textBounds.centerX() " + textBounds.centerX() + " textBounds.right: " + textBounds.right
                    + " textBounds.left:" + textBounds.left);

            final Drawable[] drawables = getCompoundDrawables();
            final Drawable drawableRight = getResources().getDrawable(R.drawable.sofa_slide_icon_arrow);

            final int x = - (getWidth() / 2 - textBounds.centerX() - 32 - 16 );
            drawableRight.setBounds(x, 0, x + 32, 32);
            setCompoundDrawables(drawables[0], drawables[1], drawableRight, drawables[3]);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartMove = false;
                mRawXStart = event.getRawX();
                mPreviousRawXMove = mRawXStart;
                logMsg("ACTION_DOWN mRawXStart: " + mRawXStart);
                break;
            case MotionEvent.ACTION_UP:
                mStartMove = false;
                mRawXEnd = event.getRawX();
                mMoveDeltaX = mRawXEnd - mRawXStart;
                logMsg("ACTION_UP mRawXEnd: " + mRawXEnd + " mMoveDeltaX:" + mMoveDeltaX + " mViewWidth: " + mViewWidth);
                if (mMoveDeltaX <= mViewWidth * ACTION_CONFIRM_DISTANCE_FRACTION){
                    logMsg("action not confirmed");
                    // 未确认操作，退回原位
                    final float curX = getX();
                    final float targetX = mViewInitialX;
                    final long duration = (long) (Math.abs(targetX - curX) /getWidth() * 1000);
                    ObjectAnimator animator = ObjectAnimator.ofFloat(SlideButton.this, "X", curX, targetX);
                    animator.setDuration(duration);
                    animator.start();
                } else {
                    logMsg("action confirmed");
                    // 滑动手势抬起后，剩余部分自动滑出
                    // 保持按下时的颜色
                    SlideButton.this.setBackgroundResource(R.color.provider_color_bottom_bar_online_bg_pressed);
                    final float curX = getX();
                    final float targetX = mViewInitialX + getWidth();
                    final long duration = (long) (Math.abs(targetX - curX) /getWidth() * 1000);
                    ObjectAnimator animator = ObjectAnimator.ofFloat(SlideButton.this, "X", curX, targetX);
                    animator.setDuration(duration);
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            actionConfirmed();
                        }
                    });
                    animator.start();
                }
                mPreMoveDeltaX = 0f;
                break;
            case MotionEvent.ACTION_MOVE:
                mRawXMove = event.getRawX();
                // 最新思路：用滑动的delta值来更新view的x坐标，在每次更新前检查即将更新的值是否在初始位置的左边，如果是，则不更新

                logMsg("ACTION_MOVE mRawXMove: " + mRawXMove + " mPreviousRawXMove: " + mPreviousRawXMove);
                // 每次按下到开始滑动必须先从左向右（即从按下到开始滑动不能从右向左），滑动过程中可以从右向左
                mMoveDeltaX = mRawXMove - mPreviousRawXMove;
                logMsg("ACTION_MOVE mStartMove: " + mStartMove + " mMoveDeltaX: " + mMoveDeltaX);
                if (!mStartMove && mMoveDeltaX > mViewWidth * 0.02f) {
                    mStartMove = true;
                }
                if (mStartMove) {
                    logMsg("ACTION_MOVE updateX");
                    updateX(mMoveDeltaX);
//                    if (mMoveDeltaX > mPreMoveDeltaX) {
//                        logMsg("ACTION_MOVE updateX");
//                        updateX(mMoveDeltaX);
//                        mPreMoveDeltaX = mMoveDeltaX;
//                    }
                }
//                mPreviousRawXMove = mRawXMove;
                break;
        }
        return super.onTouchEvent(event);
    }

    public void reset() {
        setVisibility(VISIBLE);
        updateX(mViewInitialX);
        setBackgroundResource(R.drawable.provider_bottom_bar_online_bg_selector);
    }

    private void actionConfirmed() {
        setVisibility(GONE);
        if (mOnSlideActionListener != null) {
            mOnSlideActionListener.onActionConfirmed();
        }
    }

    public static void logMsg(String s) {
        Log.d(TAG, s);
    }

    private void updateScrollX(float x) {
        setScrollX((int) x);
        invalidate();
    }

    private void updateX(float x) {
        setX(x);
        invalidate();
    }

    public void setOnSlideActionListener(OnSlideActionListener onSlideActionListener) {
        this.mOnSlideActionListener = onSlideActionListener;
    }

    @Override
    public float getGradientX() {
        return mShimmerViewHelper.getGradientX();
    }

    @Override
    public void setGradientX(float gradientX) {
        mShimmerViewHelper.setGradientX(gradientX);
    }

    @Override
    public boolean isShimmering() {
        return mShimmerViewHelper.isShimmering();
    }

    @Override
    public void setShimmering(boolean isShimmering) {
        mShimmerViewHelper.setShimmering(isShimmering);
    }

    @Override
    public boolean isSetUp() {
        return mShimmerViewHelper.isSetUp();
    }

    @Override
    public void setAnimationSetupCallback(ShimmerViewHelper.AnimationSetupCallback callback) {
        mShimmerViewHelper.setAnimationSetupCallback(callback);
    }

    @Override
    public int getPrimaryColor() {
        return mShimmerViewHelper.getPrimaryColor();
    }

    @Override
    public void setPrimaryColor(int primaryColor) {
        mShimmerViewHelper.setPrimaryColor(primaryColor);
    }

    @Override
    public int getReflectionColor() {
        return mShimmerViewHelper.getReflectionColor();
    }

    @Override
    public void setReflectionColor(int reflectionColor) {
        mShimmerViewHelper.setReflectionColor(reflectionColor);
    }

    @Override
    public void setTextColor(int color) {
        super.setTextColor(color);
        if (mShimmerViewHelper != null) {
            mShimmerViewHelper.setPrimaryColor(getCurrentTextColor());
        }
    }

    @Override
    public void setTextColor(ColorStateList colors) {
        super.setTextColor(colors);
        if (mShimmerViewHelper != null) {
            mShimmerViewHelper.setPrimaryColor(getCurrentTextColor());
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mShimmerViewHelper != null) {
            mShimmerViewHelper.onSizeChanged();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mShimmerViewHelper != null) {
            mShimmerViewHelper.onDraw();
        }
        super.onDraw(canvas);
    }

    public interface OnSlideActionListener {
        public void onActionConfirmed();
    }
}
