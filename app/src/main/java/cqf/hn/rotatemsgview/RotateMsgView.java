package cqf.hn.rotatemsgview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * TODO 未完成()
 * 1.传输数据，数据发生（notifyDataSetChange）改变布局也发生相应的改变（改变可平滑的变化）
 * 2.传输与数据对应的View
 * 3.确定点击对应的View触发相应的事件(setOnItemClick(int position ,T data))
 * 4.确定布局方式：可自定义适配消息的高度（可以显示全部消息时，不动作）
 * 5.移动只移动一条消息（扩展可设定移动多条消息）
 * 6.固定动画方式向上移动|向下移动|渐变消失显示
 * 7.可设定动画时间，停留时间
 * 如何处理当数据传输进来，子View读取数据，并按照数据的排序，顺序显示在View上，
 * 如何提供第三方人员对子View设置数据
 */
public class RotateMsgView extends ViewGroup {

    private CustomAdapter adapter;
    private int firstVisibleIndex = 0;
    private int childHeightSum = 0;
    private int moveY = 0;
    private int mode;
    private int duration = 3000;
    private long mDelayMillis = 1 * 1000;//延时启动动画的时间
    private ValueAnimator animator;
    private PropertyValuesHolder propertyValuesHolder;
    private float animatedValue;
    private boolean isRequestLayout;
    private boolean isStartAnimator;
    private boolean isFirstLayout = true;

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
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(generateDefaultLayoutParams());
        linearLayout.setOrientation(LinearLayout.VERTICAL);

    }

    public CustomAdapter getAdapter() {
        return adapter;
    }

    public int getFirstVisibleIndex() {
        return firstVisibleIndex;
    }

    public void setFirstVisibleIndex(int firstVisibleIndex) {
        this.firstVisibleIndex = firstVisibleIndex;
    }

    public void setAdapter(CustomAdapter adapter) {
        this.adapter = adapter;
        for (int i = 0; i < adapter.getCount(); i++) {
            View view = adapter.getView(i, this);
            addView(view);
        }
        //放在start处调用
        requestLayout();
        invalidate();
    }

    @Mode
    public int getMode() {
        return mode;
    }

    public void setMode(@Mode int mode) {
        this.mode = mode;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        childHeightSum = 0;
        moveY = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = this.getChildAt(i);
            setItemViewLayoutParams(child);
            this.measureChild(child, widthMeasureSpec, heightMeasureSpec);
            int ch = child.getMeasuredHeight();
            childHeightSum += ch;
            if (firstVisibleIndex != 0) {
                if (i < firstVisibleIndex) {
                    moveY += ch;
                }
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (isFirstLayout || (animator != null && animator.isRunning())) {
            if (getChildCount() == 0) {
                return;
            }
            int startHeight;
            if (isStartAnimator) {
                startHeight = (int) (animatedValue - getChildAt(firstVisibleIndex).getMeasuredHeight());
            } else {
                startHeight = 0;
            }
            for (int i = firstVisibleIndex; i < getChildCount(); i++) {
                View child = this.getChildAt(i);
                int ch = child.getMeasuredHeight();
                child.layout(0, startHeight, child.getMeasuredWidth(), startHeight + ch);
                startHeight += ch;
            }
            for (int i = 0; i < firstVisibleIndex; i++) {
                View child = this.getChildAt(i);
                int ch = child.getMeasuredHeight();
                child.layout(0, startHeight, child.getMeasuredWidth(), startHeight + ch);
                startHeight += ch;
            }
            isFirstLayout = false;
        }
        if (animator != null && !animator.isRunning() && !isStartAnimator) {
            int index = getGoneViewIndex();
            View view = getChildAt(index);

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

    private int getGoneViewIndex() {
        int index = 0;
        switch (mode) {
            case Mode.MODE_ALPHA:
            case Mode.MODE_TRANSLATION_UP:
                index = firstVisibleIndex - 1;
                break;
            case Mode.MODE_TRANSLATION_DOWN:
                index = firstVisibleIndex + 1;
                break;
        }
        if (index >= getChildCount()) {
            index = 0;
        }
        if (index < 0) {
            index = getChildCount() - 1;
        }
        return index;
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
        int MODE_ALPHA = 2;
    }

    public void start() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                switch (mode) {
                    case Mode.MODE_ALPHA:
                        startAnimator(1, 0, duration, mDelayMillis, mode);
                        break;
                    case Mode.MODE_TRANSLATION_UP:
                        startAnimator(getChildAt(firstVisibleIndex).getMeasuredHeight(), 0, duration, mDelayMillis, mode);
                        break;
                    case Mode.MODE_TRANSLATION_DOWN:
                        startAnimator(0, getChildAt(firstVisibleIndex).getMeasuredHeight(), duration, mDelayMillis, mode);
                        break;
                }
            }
        }, mDelayMillis);

    }

    private void startAnimator(float start, float end, long duration, long delayMillis, @Mode final int mode) {
        if (animator == null) {
            animator = ValueAnimator.ofFloat(start, end);
            animator.setDuration(duration);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    isStartAnimator = true;
                    switch (mode) {
                        case Mode.MODE_ALPHA:

                            break;
                        case Mode.MODE_TRANSLATION_UP:

                            break;
                        case Mode.MODE_TRANSLATION_DOWN:

                            break;
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    switch (mode) {
                        case Mode.MODE_ALPHA:
                        case Mode.MODE_TRANSLATION_UP:
                            firstVisibleIndex++;
                            break;
                        case Mode.MODE_TRANSLATION_DOWN:
                            firstVisibleIndex--;
                            break;
                    }
                    if (firstVisibleIndex >= getChildCount()) {
                        firstVisibleIndex = 0;
                    }
                    if (firstVisibleIndex < 0) {
                        firstVisibleIndex = getChildCount() - 1;
                    }

                    isStartAnimator = false;
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            switch (mode) {
                                case Mode.MODE_ALPHA:
                                    startAnimator(1, 0, RotateMsgView.this.duration, mDelayMillis, mode);
                                    break;
                                case Mode.MODE_TRANSLATION_UP:
                                    startAnimator(getChildAt(firstVisibleIndex).getMeasuredHeight(), 0, RotateMsgView.this.duration, mDelayMillis, mode);
                                    break;
                                case Mode.MODE_TRANSLATION_DOWN:
                                    startAnimator(0, getChildAt(firstVisibleIndex).getMeasuredHeight(), RotateMsgView.this.duration, mDelayMillis, mode);
                                    break;
                            }
                        }
                    }, mDelayMillis);
                    requestLayout();
                }
            });
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    animatedValue = (Float) animation.getAnimatedValue();
//                    switch (mode) {
//                        case Mode.MODE_ALPHA:
//
//                            break;
//                        case Mode.MODE_TRANSLATION_UP:
//                            for (int i = 0; i < getChildCount(); i++) {
//                                View view = getChildAt(i);
//                                view.setTranslationY((animatedValue - getChildAt(firstVisibleIndex).getMeasuredHeight()));
//                            }
//                            break;
//                        case Mode.MODE_TRANSLATION_DOWN:
//
//                            break;
//                    }
                    requestLayout();
                }
            });
            //animator.setStartDelay(delayMillis);
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
                //animator.setStartDelay(delayMillis);// 这一行代码不能再判断条件后执行
                animator.start();
            }
        }
    }
}