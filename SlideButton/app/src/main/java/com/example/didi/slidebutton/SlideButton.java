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
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
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
    private static final float ACTION_CONFIRM_DISTANCE_FRACTION = 0.4f;

    private float x1;
    private float x2;
    /**
     * View 在滑动前的x坐标（需考虑padding，margin的情况）
     */
    private float mViewInitialX;
    private float mMoveDeltaX;
    private float mPreMoveDeltaX = 0f;

    private OnSlideActionListener mOnSlideActionListener;

    private ShimmerViewHelper mShimmerViewHelper;

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
        mViewInitialX = getX();
        logMsg("init mViewInitialX: " + mViewInitialX);

        mShimmerViewHelper = new ShimmerViewHelper(this, getPaint(), attrs);
        mShimmerViewHelper.setPrimaryColor(getCurrentTextColor());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        logMsg("onLayout mViewInitialX: " + mViewInitialX + " getWidth: " + getWidth() + " changed: " + changed);
        if (changed) {
            mViewInitialX = getX();

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
                x1 = event.getX();
                logMsg("ACTION_DOWN x1: " + x1);
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                logMsg("ACTION_UP x2: " + x2 + " x2 - x1:" + (x2 - x1) + " getWidth(): " +getWidth());
                if ((x2 - x1) <= getWidth() * ACTION_CONFIRM_DISTANCE_FRACTION){
                    logMsg("action not confirmed");
                    reset();
                } else {
                    logMsg("action confirmed");
                    // 滑动手势抬起后，剩余部分自动滑出
                    // 保持按下时的颜色
                    SlideButton.this.setBackgroundResource(R.color.provider_color_bottom_bar_online_bg_pressed);
                    final float curX = getX();
                    final float targetX = mViewInitialX + getWidth();
                    ObjectAnimator animator = ObjectAnimator.ofFloat(SlideButton.this, "X", curX, targetX);
                    animator.setDuration((long) ((targetX - curX) / getWidth() * 1000));
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
                x2 = event.getX();
                if (x2 > x1) {// move from left to right
//                    updateScrollX(x1 - x2);
                    mMoveDeltaX = x2 - x1;
                    logMsg("ACTION_MOVE mPreMoveDeltaX: " + mPreMoveDeltaX + " mMoveDeltaX: " + mMoveDeltaX);
                    // 避免滑动时太过灵敏导致UI抖动
                    if (mMoveDeltaX <= getWidth() * 0.05f) {
                        break;
                    }
                    if (mMoveDeltaX > mPreMoveDeltaX) {
                        logMsg("ACTION_MOVE updateX");
                        updateX(mMoveDeltaX);
                        mPreMoveDeltaX = mMoveDeltaX;
                    }
//                    if (mPreMoveDeltaX == 0f) {
//                        mPreMoveDeltaX = mMoveDeltaX;
//                    } else {
//                    }
                }
                break;
        }
        return super.onTouchEvent(event);
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

    public void reset() {
        setVisibility(VISIBLE);
        updateX(mViewInitialX);
        setBackgroundResource(R.drawable.provider_bottom_bar_online_bg_selector);
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
