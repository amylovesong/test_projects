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

    private final int GAP_BETWEEN_TEXT_AND_ARROW = 24;
    private final int ARROW_WIDTH = 20;

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

    private String mTextArrow = ">";
    private int mTextSize;
    private int mTextCenterXOffset;

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
        mTextSize = 60;
        mTextCenterXOffset = ARROW_WIDTH + GAP_BETWEEN_TEXT_AND_ARROW / 2;

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.density = getResources().getDisplayMetrics().density;
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

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
        setImageResource(android.R.color.transparent);
        mLoading = false;
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
        mTranslateX = x;
        final Drawable drawable = getDrawable();
        logMsg("setSlideX: " + drawable);
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

        logMsg("onDraw mLoading: " + mLoading);
        if (mLoading) {
            final Rect rect = mLoadingDrawable.getBounds();
            logMsg("onDraw loading drawable rect: " + rect);
            mLoadingDrawable.setBounds(30, 30, 162, 162);
        }

        super.onDraw(canvas);

        if (!mLoading) {
            // Draw text
            setPaintForText();
            final Paint.FontMetricsInt textFontMetricsInt = mTextPaint.getFontMetricsInt();
            logMsg("onDraw textFontMetricsInt" + textFontMetricsInt);
            final int textX = (int) (getWidth() / 2 - mTextCenterXOffset + mTranslateX);
            final int textBaseline = getHeight() / 2 - (textFontMetricsInt.bottom - textFontMetricsInt.top) / 2 - textFontMetricsInt.top;
            canvas.drawText(mText, textX, textBaseline , mTextPaint);

            // Draw two arrow
            mTextPaint.getTextBounds(mText, 0, mText.length(), mTextBounds);
            setPaintForArrow();
            final Paint.FontMetricsInt arrowFontMetricsInt = mTextPaint.getFontMetricsInt();
            logMsg("onDraw arrowFontMetricsInt: " + arrowFontMetricsInt);
            final int arrowX = (int) (getWidth() / 2 + mTextBounds.centerX() + GAP_BETWEEN_TEXT_AND_ARROW - mTextCenterXOffset + mTranslateX);
            final int arrowBaseline = getHeight() / 2 - (arrowFontMetricsInt.bottom - arrowFontMetricsInt.top) / 2 - arrowFontMetricsInt.top;
            canvas.drawText(mTextArrow, arrowX, arrowBaseline, mTextPaint);
            mTextPaint.setAlpha(115);
            canvas.drawText(mTextArrow, arrowX + ARROW_WIDTH, arrowBaseline, mTextPaint);
        }
    }

    private void setPaintForArrow() {
        mTextPaint.setTextScaleX(0.5f);
        mTextPaint.setTextSize(getTextSize() * 1.4f);
        mTextPaint.setTypeface(Typeface.SERIF);
    }

    private void setPaintForText() {
        mTextPaint.setTextScaleX(1.0f);
        mTextPaint.setTextSize(getTextSize());
        mTextPaint.setTypeface(Typeface.DEFAULT);
        mTextPaint.setAlpha(255);
    }

    private CharSequence getText() {
        return mText;
    }

    private float getTextSize() {
        return mTextSize;
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
