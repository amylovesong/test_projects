package com.example.didi.slidebutton;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
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
 * 可手势滑动确认操作的滑块。
 * 监听Touch事件处理手势，onDraw()中画上文字和指示箭头，使用drawable作为滑动确认后的Loading效果，使用线性渐变类型的shader处理闪烁效果
 * @author sxl  (sunxiaoling@didichuxing.com)
 * @date 16/5/15 16:29
 * @version V1.0
 */
public class SlideButton extends ImageButton implements ShimmerViewBase {

    public interface OnSlideActionListener {
        void onTouchActionDown(SlideButton button);

        void onTouchActionMove(SlideButton button);

        void onActionCancel(SlideButton button);

        void onActionConfirmed(SlideButton button);
    }

    private final float ACTION_CONFIRM_DISTANCE_FRACTION = 0.3f;
    private final int GAP_BETWEEN_TEXT_AND_ARROW = getResources().getDimensionPixelSize(R.dimen.provider_6dp);
    private final int ARROW_WIDTH = getResources().getDimensionPixelSize(R.dimen.provider_6dp);
    private final String TEXT_ARROW = ">";

    private enum STYLE{ORANGE, SLIDE}
    private STYLE mStyle = STYLE.ORANGE;

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

    private OnSlideActionListener mOnSlideActionListener;
    private ShimmerViewHelper mShimmerViewHelper;
    private Drawable mImageDrawable;
    private Drawable mBackgroundDrawable;
    private int mTextColor;
    private TextPaint mTextPaint;
    private String mText;
    private Rect mTextBounds;
    private AnimatorSet mLoadingAnimatorSet;
    private boolean mLoading = false;
    private RotateDrawable mLoadingDrawable;
    private int mTextSize;
    private int mTextCenterXOffset;
    private float mTranslateX;

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
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ProviderCustomButton);
        mTextSize = (int) a.getDimension(R.styleable.ProviderCustomButton_fontSize, 34);
        mText = a.getString(R.styleable.ProviderCustomButton_text);
        setTextColor(a.getColor(R.styleable.ProviderCustomButton_fontColor, Color.WHITE));
        a.recycle();

        mTextBounds = new Rect();
        mTextCenterXOffset = ARROW_WIDTH + GAP_BETWEEN_TEXT_AND_ARROW / 2;

//        setImageResource(android.R.color.transparent);
//        setBackgroundResource(R.color.provider_color_orange);
        setImageResource(R.drawable.sofa_bottom_bar_image_slide);
//        setImageResource(R.color.sofa_color_bottom_bar_slide);
        setBackgroundResource(android.R.color.black);

        mLoadingDrawable = (RotateDrawable) getResources().getDrawable(R.drawable.sofa_online_loading);
        initLoadingDrawableAnimator();

        mShimmerViewHelper = new ShimmerViewHelper(this, getPaint(), attrs);
        mShimmerViewHelper.setPrimaryColor(getCurrentTextColor());

        setScaleType(ScaleType.FIT_XY);
    }

    private void initLoadingDrawableAnimator() {
        final ObjectAnimator loadingDrawableAlphaAnimator = ObjectAnimator.ofInt(mLoadingDrawable, "Alpha", 0, 255);
        loadingDrawableAlphaAnimator.setDuration(300);
        final ObjectAnimator loadingRotateAnimator = ObjectAnimator.ofInt(mLoadingDrawable, "Level", 0, 10000);
        loadingRotateAnimator.setRepeatCount(ValueAnimator.INFINITE);
        loadingRotateAnimator.setDuration(800);

        mLoadingAnimatorSet = new AnimatorSet();
        mLoadingAnimatorSet.playTogether(loadingDrawableAlphaAnimator, loadingRotateAnimator);
        mLoadingAnimatorSet.setInterpolator(new LinearInterpolator());
        mLoadingAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                logMsg("mLoadingAnimatorSet onAnimationEnd");
                mLoadingDrawable.setLevel(0);
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mViewInitialX = 0;
            mViewWidth = getWidth();
            logMsg("onLayout mViewInitialX: " + mViewInitialX + " mViewWidth: " + mViewWidth);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mShimmerViewHelper != null) {
            mShimmerViewHelper.onDraw();
        }
        super.onDraw(canvas);

        if (!mLoading) {
            // Draw text
            setPaintForText();
            final Paint.FontMetricsInt textFontMetricsInt = mTextPaint.getFontMetricsInt();
//            logMsg("onDraw textFontMetricsInt" + textFontMetricsInt);
            final int textX = (int) (getWidth() / 2 - mTextCenterXOffset + mTranslateX);
            final int textBaseline = getHeight() / 2 - (textFontMetricsInt.bottom - textFontMetricsInt.top) / 2 - textFontMetricsInt.top;
            canvas.drawText(mText, textX, textBaseline , mTextPaint);

            // Draw two arrow
            mTextPaint.getTextBounds(mText, 0, mText.length(), mTextBounds);
            setPaintForArrow();
            final Paint.FontMetricsInt arrowFontMetricsInt = mTextPaint.getFontMetricsInt();
//            logMsg("onDraw arrowFontMetricsInt: " + arrowFontMetricsInt);
            final int arrowX = (int) (getWidth() / 2 + mTextBounds.centerX() + GAP_BETWEEN_TEXT_AND_ARROW - mTextCenterXOffset + mTranslateX);
            final int arrowBaseline = getHeight() / 2 - (arrowFontMetricsInt.bottom - arrowFontMetricsInt.top) / 2 - arrowFontMetricsInt.top;
            canvas.drawText(TEXT_ARROW, arrowX, arrowBaseline, mTextPaint);
            mTextPaint.setAlpha(115);
            canvas.drawText(TEXT_ARROW, arrowX + ARROW_WIDTH, arrowBaseline, mTextPaint);
            mTextPaint.setAlpha(255);// Reset alpha
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
    public final boolean onTouchEvent(MotionEvent event) {
        logMsg("onTouchEvent event: " + event + " mLoading: " + mLoading);
        if (mLoading) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mRawXStart = event.getRawX();
                logMsg("ACTION_DOWN mRawXStart: " + mRawXStart);
                actionDown();
                break;
            case MotionEvent.ACTION_UP:
                mRawXEnd = event.getRawX();
                mMoveDeltaX = mRawXEnd - mRawXStart;
                logMsg("ACTION_UP mRawXEnd: " + mRawXEnd + " mMoveDeltaX:" + mMoveDeltaX + " mViewWidth: " + mViewWidth);
                if (mMoveDeltaX <= mViewWidth * ACTION_CONFIRM_DISTANCE_FRACTION){
                    logMsg("action not confirmed");
                    // 未确认操作，退回原位
                    final float curX = mTranslateX;
                    final float targetX = mViewInitialX;
                    final long duration = (long) (Math.abs(targetX - curX) / mViewWidth * 1000);
                    ObjectAnimator animator = ObjectAnimator.ofFloat(SlideButton.this, "SlideX", curX, targetX);
                    animator.setDuration(duration);
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            actionCancel();
                        }
                    });
                    animator.start();
                } else {
                    logMsg("action confirmed");
                    // 滑动手势抬起后，剩余部分自动滑出
                    final float curX = mTranslateX;
                    final float targetX = mViewInitialX + mViewWidth;
                    final long duration = (long) (Math.abs(targetX - curX) / mViewWidth * 1000);
                    ObjectAnimator animator = ObjectAnimator.ofFloat(SlideButton.this, "SlideX", curX, targetX);
                    animator.setDuration(duration);
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            actionConfirmed();
                        }
                    });
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            invalidate();
                            logMsg("onAnimationUpdate " + animation.getAnimatedValue());
                        }
                    });
                    animator.start();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                mRawXMove = event.getRawX();
                // 用滑动的delta值来更新x坐标，即将更新的值不可小于初始值（即只可滑动退回到初始位置，不可向左滑出）
                mMoveDeltaX = mRawXMove - mRawXStart;
                logMsg("ACTION_MOVE mRawXMove: " + mRawXMove + " mRawXStart: " + mRawXStart + " mMoveDeltaX: " + mMoveDeltaX);
                if (Math.abs(mMoveDeltaX) > mViewWidth * 0.02f) {
                    final float targetX = mMoveDeltaX > mViewInitialX ? mMoveDeltaX : mViewInitialX;
                    actionMove();
                    updateX(targetX);
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private void actionDown() {
        if (mOnSlideActionListener != null) {
            mOnSlideActionListener.onTouchActionDown(SlideButton.this);
        }
        mTextColor = getCurrentTextColor();
        mImageDrawable = getDrawable();
        mBackgroundDrawable = getBackground();
    }

    private void actionMove() {
        if (mOnSlideActionListener != null) {
            mOnSlideActionListener.onTouchActionMove(SlideButton.this);
        }
        setStyleSlide();
    }

    private void actionCancel() {
        logMsg("actionCancel");
        if (mOnSlideActionListener != null) {
            mOnSlideActionListener.onActionCancel(SlideButton.this);
        }
        // restore UI
        setTextColor(mTextColor);
        setImageDrawable(mImageDrawable);
        setBackgroundDrawable(mBackgroundDrawable);
        this.mStyle = STYLE.ORANGE;
//        setScaleType(ScaleType.FIT_XY);
    }

    private void actionConfirmed() {
        if (mOnSlideActionListener != null) {
            mOnSlideActionListener.onActionConfirmed(SlideButton.this);
        }
        startLoading();
    }

    private void updateX(float x) {
        setSlideX(x);
    }

    private void setSlideX(float x) {
//        setScaleType(ScaleType.MATRIX);
        mTranslateX = x;
        final Drawable drawable = getDrawable();
        logMsg("setSlideX: " + drawable + " mTranslateX: " + mTranslateX);
        Rect rect = drawable.getBounds();
        logMsg("setSlideX original bounds: " + rect);
//        Matrix matrix=getImageMatrix();
//        matrix.setTranslate(mTranslateX,0);
//        setImageMatrix(matrix);
//        logMsg("setSlideX after setImageMatrix bounds: " + drawable.getBounds());
        final int translateX = (int) mTranslateX;
//        rect.set(translateX, rect.top, translateX+1440, rect.bottom);
//        drawable.setBounds(rect);
        drawable.setBounds(translateX, rect.top, rect.width() + translateX, rect.bottom);
        logMsg("setSlideX final bounds: " + drawable.getBounds());

        invalidate();
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

    private float getTextSize() {
        return mTextSize;
    }

    private void logMsg(String s) {
//        sLogger.debug(s);
        Log.d(SlideButton.class.getSimpleName(), s);
    }

    public void startLoading() {
        setScaleType(ScaleType.CENTER);
        if (!mLoadingAnimatorSet.isRunning()) {
            mLoading = true;
            setImageDrawable(mLoadingDrawable);
            mLoadingAnimatorSet.start();
        }
    }

    private void stopLoading() {
        setScaleType(ScaleType.FIT_XY);
        if (mLoadingAnimatorSet.isRunning()) {
            mLoadingAnimatorSet.end();
            mLoading = false;
        }
    }

    public void setText(int resId) {
        setText(getContext().getText(resId));
    }

    public void setText(CharSequence text) {
        this.mText = text.toString();
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

    @Override
    public final void setOnClickListener(OnClickListener l) {
        // This button do not support click event
    }

    @Override
    public void setVisibility(int visibility) {
        updateX(mViewInitialX);
        super.setVisibility(visibility);
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

    public void setTextColor(int color) {
        mTextPaint.setColor(color);
        if (mShimmerViewHelper != null) {
            mShimmerViewHelper.setPrimaryColor(getCurrentTextColor());
        }
    }

//    @Override
//    public void setTextColor(ColorStateList colors) {
//        super.setTextColor(colors);
//        if (mShimmerViewHelper != null) {
//            mShimmerViewHelper.setPrimaryColor(getCurrentTextColor());
//        }
//    }

    private void setStyleSlide() {
        logMsg("setStyleSlide mStyle: " + mStyle);
        if (this.mStyle == STYLE.SLIDE) {
            return;
        }
        this.mStyle = STYLE.SLIDE;
//        setTextColor(getContext().getResources().getColor(R.color.white));
//        if (mStyle == STYLE.DEPART) {
//            setImageResource(R.color.sofa_color_bottom_bar_depart_bg_pressed);
//        } else {
            setImageResource(R.drawable.sofa_bottom_bar_image_slide);
//        setImageResource(R.color.sofa_color_bottom_bar_slide);
//        }
//        setBackgroundResource(R.drawable.sofa_bottom_bar_bg_slide);
        setBackgroundResource(android.R.color.black);
    }

    public void setStyleOrange() {
        this.mStyle = STYLE.ORANGE;
        this.stopLoading();
        this.setTextColor(getContext().getResources().getColor(android.R.color.white));
//        this.setImageResource(android.R.color.transparent);
//        this.setBackgroundResource(R.drawable.sofa_bottom_bar_bg_normal);
        setImageResource(R.drawable.sofa_bottom_bar_image_slide);
//        setImageResource(R.color.sofa_color_bottom_bar_slide);
        setBackgroundResource(android.R.color.black);
        this.mShimmerViewHelper.setReflectionColor(getResources().getColor(R.color.sofa_color_bottom_bar_shimmer));
    }

    public void setStyleNormal() {
        this.stopLoading();
        this.setTextColor(getContext().getResources().getColor(R.color.provider_color_orange));
        this.setImageResource(android.R.color.transparent);
//        this.setBackgroundResource(R.drawable.sofa_bottom_bar_arrive_bg_normal);
    }

//    public void setStyleDepart() {
//        this.mStyle = STYLE.DEPART;
//        this.stopLoading();
//        this.setTextColor(getContext().getResources().getColor(R.color.white));
//        this.setImageResource(android.R.color.transparent);
//        this.setBackgroundResource(R.color.sofa_color_bottom_bar_depart_bg_normal);
//        // Shimmer的色值跟常态一致
//        this.mShimmerViewHelper.setReflectionColor(getResources().getColor(R.color.sofa_color_bottom_bar_depart_bg_normal));
//    }
}
