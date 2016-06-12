package com.example.didi.slidebutton;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RotateDrawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;

import com.romainpiel.shimmer.ShimmerViewBase;
import com.romainpiel.shimmer.ShimmerViewHelper;

/**
 * Created by sxl on 16/5/15.
 */
public class SlideButton extends ImageButton implements ShimmerViewBase {
    private static final String TAG = SlideButton.class.getSimpleName();
    private static final float ACTION_CONFIRM_DISTANCE_FRACTION = 0.3f;

    /**
     * 由于滑动的时候会更新View的x坐标，因此使用MotionEvent的rawX来计算
     */
    private float mRawXStart;
    private float mRawXMove;
    private float mRawXEnd;
    private float mMoveDeltaX;
    /**
     * View 初始状态的x坐标（滑动时需考虑padding，margin的情况）
     */
    private float mViewInitialX;
    private int mViewWidth;

    private OnSlideActionListener mOnSlideActionListener;

    private ShimmerViewHelper mShimmerViewHelper;
    private float mTranslateX;

    private TextPaint mTextPaint;
    private String mText = "右滑到达车站";
    private Rect mTextBounds;
    private ObjectAnimator mLoadingAnimator;
    private boolean mLoading = false;
    private RotateDrawable mLoadingDrawable;

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
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.density = getResources().getDisplayMetrics().density;

        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(60);
        mTextBounds = new Rect();

        setImageResource(android.R.color.transparent);
        setBackgroundResource(R.color.provider_color_orange);
        mLoadingDrawable = (RotateDrawable) getResources().getDrawable(R.drawable.provider_progress_button_loading);
        mLoadingAnimator = ObjectAnimator.ofInt(mLoadingDrawable, "Level", 0, 10000);
        mLoadingAnimator.setInterpolator(new LinearInterpolator());
        mLoadingAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mLoadingAnimator.setDuration(800);
        mLoadingAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                logMsg("mLoadingAnimator onAnimationEnd");
                mLoadingDrawable.setLevel(0);
            }
        });

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
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        logMsg("onTouchEvent event: " + event + " mLoading: " + mLoading);
        if (mLoading) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mRawXStart = event.getRawX();
                logMsg("ACTION_DOWN mRawXStart: " + mRawXStart);
                break;
            case MotionEvent.ACTION_UP:
                mRawXEnd = event.getRawX();
                mMoveDeltaX = mRawXEnd - mRawXStart;
                logMsg("ACTION_UP mRawXEnd: " + mRawXEnd + " mMoveDeltaX:" + mMoveDeltaX + " mViewWidth: " + mViewWidth);
                if (mMoveDeltaX <= mViewWidth * ACTION_CONFIRM_DISTANCE_FRACTION){
                    logMsg("action not confirmed");
                    // 未确认操作，退回原位
                    final float curX = mTranslateX;
                    final float targetX = 0;
                    final long duration = (long) (Math.abs(targetX - curX) /getWidth() * 1000);
                    ObjectAnimator animator = ObjectAnimator.ofFloat(SlideButton.this, "SlideX", curX, targetX);
                    animator.setDuration(duration);
                    animator.start();
                } else {
                    logMsg("action confirmed");
                    // 滑动手势抬起后，剩余部分自动滑出
                    // 保持按下时的颜色
//                    SlideButton.this.setBackgroundResource(R.color.provider_color_bottom_bar_online_bg_pressed);
                    final float curX = mTranslateX;
                    final float targetX = 0 + getWidth();
                    final long duration = (long) (Math.abs(targetX - curX) /getWidth() * 1000);
                    ObjectAnimator animator = ObjectAnimator.ofFloat(SlideButton.this, "SlideX", curX, targetX);
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
                break;
            case MotionEvent.ACTION_MOVE:
                mRawXMove = event.getRawX();
                // 用滑动的delta值来更新view的x坐标，即将更新的值不可超出view的初始位置
                mMoveDeltaX = mRawXMove - mRawXStart;
                logMsg("ACTION_MOVE mRawXMove: " + mRawXMove + " mRawXStart: " + mRawXStart + " mMoveDeltaX: " + mMoveDeltaX);
                if (Math.abs(mMoveDeltaX) > mViewWidth * 0.02f) {
                    final float targetX = mMoveDeltaX > mViewInitialX ? mMoveDeltaX : 0;
//                            mViewInitialX;
                    setImageResource(R.color.provider_color_bottom_bar_online_bg_pressed);
                    updateX(targetX);
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    public void reset() {
//        setVisibility(VISIBLE);
        stopLoading();
        updateX(0);
//        setBackgroundResource(R.drawable.provider_bottom_bar_online_bg_selector);
    }

    private void actionConfirmed() {
//        setVisibility(GONE);
//        if (mOnSlideActionListener != null) {
//            mOnSlideActionListener.onActionConfirmed();
//        }
        startLoading();
    }

    private void startLoading() {
        logMsg("startLoading");
        if (!mLoadingAnimator.isRunning()) {
            mLoading = true;
            setImageDrawable(mLoadingDrawable);
            mLoadingAnimator.start();
        }
    }

    private void stopLoading() {
        logMsg("stopLoading mLoadingAnimator.isRunning(): " + mLoadingAnimator.isRunning());
        if (mLoadingAnimator.isRunning()) {
            mLoadingAnimator.end();
            setImageResource(android.R.color.transparent);
            mLoading = false;
        }
        logMsg("stopLoading mLoadingAnimator.isRunning() after: " + mLoadingAnimator.isRunning());
    }

    public static void logMsg(String s) {
        Log.d(TAG, s);
    }

    private void updateX(float x) {
        setSlideX(x);
//        setX(x);
    }

    private void setSlideX(float x) {
        final Drawable drawable = getDrawable();
        logMsg("setSlideX: " + drawable);
        mTranslateX = x;
        Rect rect = drawable.getBounds();
        rect.set((int) mTranslateX, rect.top, rect.right, rect.bottom);
        drawable.setBounds(rect);

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mShimmerViewHelper != null) {
            mShimmerViewHelper.onDraw();
        }

        if (mLoading) {
            final Rect rect = mLoadingDrawable.getBounds();
            logMsg("onDraw loading drawable rect: " + rect);
            mLoadingDrawable.setBounds(30, 30, 162, 162);
        }

        super.onDraw(canvas);

        logMsg("onDraw mLoading: " + mLoading);
        if (!mLoading) {
            mTextPaint.getTextBounds(mText, 0, mText.length(), mTextBounds);
            canvas.drawText(mText, getWidth()/2 + mTranslateX, (getHeight()/2)+((mTextBounds.bottom- mTextBounds.top)/2) , mTextPaint);

            final Paint originalPaint = getPaint();
            final Rect textBounds = new Rect();
            originalPaint.getTextBounds(getText().toString(), 0, getText().length(), textBounds);
            logMsg("textBounds.centerX(): " + textBounds.centerX() + " textBounds.right: " + textBounds.right);

            final String text = ">";
            final Paint arrowPaint = new Paint(originalPaint);
            arrowPaint.setTextScaleX(0.5f);
            arrowPaint.setTextSize(getTextSize() * 1.4f);
            arrowPaint.setTypeface(Typeface.SERIF);

            final Rect arrowBounds = new Rect();
            arrowPaint.getTextBounds(text, 0, text.length(), arrowBounds);
            logMsg("arrowBounds.centerX(): " + arrowBounds.centerX() + " arrowBounds.width(): " + arrowBounds.width());
            final int arrowWidth = arrowBounds.width();
            final int gap = 12;
            final int textCenterXOffset = arrowWidth + gap / 2;
            setPadding(0, 0, textCenterXOffset, 0);// button本身的text的向左偏移

            // Draw two arrow
            final int x = getWidth() / 2 + textBounds.centerX() + gap - textCenterXOffset;// arrow向左偏移
            final int y = getHeight() / 2 + 34;
            canvas.drawText(text, x, y, arrowPaint);
            arrowPaint.setAlpha(115);
            canvas.drawText(text, x + arrowWidth, y, arrowPaint);
        }
    }

    private CharSequence getText() {
        return mText;
    }

    private float getTextSize() {
        return mTextPaint.getTextSize();
    }

    public Paint getPaint() {
        return mTextPaint;
    }

    public int getCurrentTextColor() {
        return mTextPaint.getColor();
    }

    public void setOnSlideActionListener(OnSlideActionListener onSlideActionListener) {
        this.mOnSlideActionListener = onSlideActionListener;
    }

    public interface OnSlideActionListener {
        public void onActionConfirmed();
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

    //    @Override
    public void setTextColor(int color) {
//        super.setTextColor(color);
        mTextPaint.setColor(color);
        if (mShimmerViewHelper != null) {
            mShimmerViewHelper.setPrimaryColor(getCurrentTextColor());
        }
    }

//    @Override
//    public void setTextColor(ColorStateList colors) {
//        super.setTextColor(colors);
//        mTextPaint.setColor(colors);
//        mTextPaint.set
//        if (mShimmerViewHelper != null) {
//            mShimmerViewHelper.setPrimaryColor(getCurrentTextColor());
//        }
//    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mShimmerViewHelper != null) {
            mShimmerViewHelper.onSizeChanged();
        }
    }
}
