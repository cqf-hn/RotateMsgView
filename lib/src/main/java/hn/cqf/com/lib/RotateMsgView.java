package hn.cqf.com.lib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 仿RecyclerView或ListView的回收机制
 */
public class RotateMsgView extends ViewGroup {

    private CustomAdapter adapter;
    private int firstViewIndex = 0;//在初始值和动画结束后才会被赋值
    private int childHeightSum = 0;
    private int mode;//在动画结束后，才会触发效果
    private int lastMode;//在动画开始后保存mode，用以处理切换动画时的状态错乱
    private int duration = 3000;
    private long mDelayMillis = 3 * 1000;//延时启动动画的时间
    private ValueAnimator animator;
    private PropertyValuesHolder propertyValuesHolder;
    private float animatedValue;
    private boolean isFirstLayout = true;
    private Animation showAnimation = new AlphaAnimation(0f, 1f);
    private Animation hideAnimation = new AlphaAnimation(1f, 0f);
    private Runnable animatorRunnable;
    private boolean hasChangeFirstIndexWithMode;

    public RotateMsgView(Context context) {
        this(context, null);
    }

    public RotateMsgView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RotateMsgView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr);
    }

    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        showAnimation.setDuration(mDelayMillis);
        hideAnimation.setDuration(mDelayMillis);
        animatorRunnable = new Runnable() {
            @Override
            public void run() {
                if (hasChangeFirstIndexWithMode) {
                    if (lastMode != mode) {
                        switch (mode) {
                            case Mode.MODE_ALPHA:
                                break;
                            case Mode.MODE_TRANSLATION_UP:
                                firstViewIndex++;
                                break;
                            case Mode.MODE_TRANSLATION_DOWN:
                                firstViewIndex--;
                                break;
                        }
                        if (firstViewIndex >= getChildCount()) {
                            firstViewIndex = 0;
                        }
                        if (firstViewIndex < 0) {
                            firstViewIndex = getChildCount() - 1;
                        }
                    }
                }
                switch (mode) {
                    case Mode.MODE_ALPHA:
                        startAnimator(1, 0, duration, mDelayMillis);
                        break;
                    case Mode.MODE_TRANSLATION_UP:
                        startAnimator(0, getChildAt(firstViewIndex).getMeasuredHeight(), duration, mDelayMillis);
                        break;
                    case Mode.MODE_TRANSLATION_DOWN:
                        startAnimator(getChildAt(firstViewIndex).getMeasuredHeight(), 0, duration, mDelayMillis);
                        break;
                }
                lastMode = mode;
            }
        };
    }

    public CustomAdapter getAdapter() {
        return adapter;
    }

    public int getFirstViewIndex() {
        return firstViewIndex;
    }

    public void setFirstViewIndex(int firstViewIndex) {
        this.firstViewIndex = firstViewIndex;
    }

    public void setAdapter(CustomAdapter adapter) {
        this.adapter = adapter;
        for (int i = 0; i < adapter.getCount(); i++) {
            View view = adapter.getView(i, this);
            addView(view);
        }
        requestLayout();
        invalidate();
    }

    @Mode
    public int getMode() {
        return mode;
    }

    public void setMode(@Mode int mode) {
        if (animator != null && animator.isRunning()) {
            hasChangeFirstIndexWithMode = false;
        } else {
            hasChangeFirstIndexWithMode = true;
        }
        this.mode = mode;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        childHeightSum = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = this.getChildAt(i);
            setItemViewLayoutParams(child);
            this.measureChild(child, widthMeasureSpec, heightMeasureSpec);
            int ch = child.getMeasuredHeight();
            childHeightSum += ch;
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (isFirstLayout || (animator != null && animator.isRunning())) {
            if (getChildCount() == 0) {
                return;
            }
            int startHeight = 0;
            int firstViewHeight = getChildAt(firstViewIndex).getMeasuredHeight();
            if (animator != null && animator.isRunning()) {
                switch (lastMode) {
                    case Mode.MODE_ALPHA:
                        break;
                    case Mode.MODE_TRANSLATION_UP://↑（0~height）
                        if (Math.round(animatedValue * 10) < Math.round(firstViewHeight * 10)) {
                            startHeight = (int) -animatedValue;
                        } else {
                            startHeight = -firstViewHeight;
                        }
                        break;
                    case Mode.MODE_TRANSLATION_DOWN://↓（height~0）//Math.round(animatedValue * 10) != 0
                        if (Math.round(animatedValue * 10) != 0) {
                            startHeight = (int) -animatedValue;
                        } else {
                            startHeight = 0;
                        }
                        break;
                }
            } else {
                switch (lastMode) {
                    case Mode.MODE_ALPHA:
                        break;
                    case Mode.MODE_TRANSLATION_UP:
                        startHeight = 0;
                        break;
                    case Mode.MODE_TRANSLATION_DOWN:
                        startHeight = -firstViewHeight;
                        break;
                }
            }
            for (int i = firstViewIndex; i < getChildCount(); i++) {
                View child = this.getChildAt(i);
                int ch = child.getMeasuredHeight();
                child.layout(0, startHeight, child.getMeasuredWidth(), startHeight + ch);
                startHeight += ch;
            }
            for (int i = 0; i < firstViewIndex; i++) {
                View child = this.getChildAt(i);
                int ch = child.getMeasuredHeight();
                child.layout(0, startHeight, child.getMeasuredWidth(), startHeight + ch);
                startHeight += ch;
            }
            isFirstLayout = false;
        }
        //动画结束，运行到这里
        if (animator != null && !animator.isRunning()) {
            int index = getGoneViewIndex();
            View view = getChildAt(index);
            if (mode == Mode.MODE_TRANSLATION_UP && lastMode == Mode.MODE_TRANSLATION_UP) {
                view.layout(0, childHeightSum - view.getMeasuredHeight(), view.getMeasuredWidth(), childHeightSum);
                view.startAnimation(showAnimation);
            } else if (mode == Mode.MODE_TRANSLATION_DOWN && lastMode == Mode.MODE_TRANSLATION_DOWN) {
                view.layout(0, childHeightSum - view.getMeasuredHeight(), view.getMeasuredWidth(), childHeightSum);
                view.startAnimation(hideAnimation);
            }
        }
    }

    private int getGoneViewIndex() {
        int index = 0;
        switch (mode) {
            case Mode.MODE_ALPHA:
            case Mode.MODE_TRANSLATION_UP:
                index = firstViewIndex - 1;
                break;
            case Mode.MODE_TRANSLATION_DOWN:
                index = firstViewIndex;
                break;
        }
        if (index < 0) {
            index = getChildCount() - 1;
        }
        return index;
    }

    public void start() {
        postDelayed(animatorRunnable, mDelayMillis);
    }

    private void startAnimator(float start, float end, long duration, long delayMillis) {
        if (animator == null) {
            animator = ValueAnimator.ofFloat(start, end);
            animator.setDuration(duration);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    //初始化动画执行前的布局
                    switch (mode) {
                        case Mode.MODE_ALPHA:
                        case Mode.MODE_TRANSLATION_UP:
                            if (lastMode == mode) {
                                firstViewIndex++;
                            }
                            break;
                        case Mode.MODE_TRANSLATION_DOWN:
                            if (lastMode == mode) {
                                firstViewIndex--;
                            }
                            break;
                    }
                    if (firstViewIndex >= getChildCount()) {
                        firstViewIndex = 0;
                    }
                    if (firstViewIndex < 0) {
                        firstViewIndex = getChildCount() - 1;
                    }
                    postDelayed(animatorRunnable, mDelayMillis);
                    requestLayout();
                    Log.v("shan", "=====================================");
                }
            });
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    animatedValue = (Float) animation.getAnimatedValue();
                    Log.v("shan", "animatedValue:" + animatedValue);
                    requestLayout();
                }
            });
            animator.start();
        } else {//动画取消后再运行，会重置一切animator的参数
            if (!animator.isRunning()) {
                if (null == propertyValuesHolder) {
                    propertyValuesHolder = PropertyValuesHolder.ofFloat("y", start, end);
                } else {
                    propertyValuesHolder.setFloatValues(start, end);
                }
                animator.setValues(propertyValuesHolder);
                animator.setDuration(duration);
                animator.start();
            }
        }
    }

    /*===借鉴自AbsListView===*/
    private void setItemViewLayoutParams(View child) {
        final ViewGroup.LayoutParams vlp = child.getLayoutParams();
        LayoutParams lp;
        if (vlp == null) {
            lp = (LayoutParams) generateDefaultLayoutParams();
        } else if (!checkLayoutParams(vlp)) {
            lp = (LayoutParams) generateLayoutParams(vlp);
        } else {
            lp = (LayoutParams) vlp;
        }
        if (lp != vlp) {
            child.setLayoutParams(lp);
        }
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new RotateMsgView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new RotateMsgView.LayoutParams(getContext(), attrs);
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }
    /*===借鉴自AbsListView===*/

    @IntDef({
            Mode.MODE_TRANSLATION_UP,
            Mode.MODE_TRANSLATION_DOWN,
            Mode.MODE_ALPHA
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
        int MODE_TRANSLATION_UP = 0;
        int MODE_TRANSLATION_DOWN = 1;
        int MODE_ALPHA = 2;//淡入淡出的效果未实现
    }
}