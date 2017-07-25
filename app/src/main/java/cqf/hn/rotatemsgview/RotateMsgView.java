package cqf.hn.rotatemsgview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.AnimRes;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

/**
 * TODO 未完成()
 * 1.传输数据，数据发生（notifyDataSetChange）改变布局也发生相应的改变（改变可平滑的变化）
 * 2.传输与数据对应的View
 * 3.确定点击对应的View触发相应的事件(setOnItemClick(int position ,T data))
 * 4.确定布局方式：可自定义适配消息的高度（可以显示全部消息时，不动作）
 * 5.移动只移动一条消息（扩展可设定移动多条消息）
 * 6.固定动画方式向上移动|向下移动|渐变消失显示
 * 7.可设定动画时间，停留时间
 *
 * 如何处理当数据传输进来，子View读取数据，并按照数据的排序，顺序显示在View上，
 * 如何提供第三方人员对子View设置数据
 */
public class RotateMsgView extends FrameLayout {

    int mWhichChild = 0;
    boolean mFirstTime = true;

    boolean mAnimateFirstTime = true;

    Animation mInAnimation;
    Animation mOutAnimation;

    public RotateMsgView(Context context) {
        super(context);
        initViewAnimator(context, null);
    }

    public RotateMsgView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RotateMsgView);
        int resource = a.getResourceId(R.styleable.RotateMsgView_inAnimation, 0);
        if (resource > 0) {
            setInAnimation(context, resource);
        }

        resource = a.getResourceId(R.styleable.RotateMsgView_outAnimation, 0);
        if (resource > 0) {
            setOutAnimation(context, resource);
        }

        boolean flag = a.getBoolean(R.styleable.RotateMsgView_animateFirstView, true);
        setAnimateFirstView(flag);

        a.recycle();

        initViewAnimator(context, attrs);
    }


    private void initViewAnimator(Context context, AttributeSet attrs) {
        if (attrs == null) {
            setMeasureAllChildren(true);
            return;
        }


        final TypedArray a = context.obtainStyledAttributes(attrs,
                com.android.internal.R.styleable.FrameLayout);
        final boolean measureAllChildren = a.getBoolean(
                com.android.internal.R.styleable.FrameLayout_measureAllChildren, true);
        setMeasureAllChildren(measureAllChildren);
        a.recycle();
    }

    public void setDisplayedChild(int whichChild) {
        mWhichChild = whichChild;
        if (whichChild >= getChildCount()) {
            mWhichChild = 0;
        } else if (whichChild < 0) {
            mWhichChild = getChildCount() - 1;
        }
        boolean hasFocus = getFocusedChild() != null;
        // This will clear old focus if we had it
        showOnly(mWhichChild);
        if (hasFocus) {
            // Try to retake focus if we had it
            requestFocus(FOCUS_FORWARD);
        }
    }


    public int getDisplayedChild() {
        return mWhichChild;
    }


    public void showNext() {
        setDisplayedChild(mWhichChild + 1);
    }


    public void showPrevious() {
        setDisplayedChild(mWhichChild - 1);
    }


    void showOnly(int childIndex, boolean animate) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (i == childIndex) {
                if (animate && mInAnimation != null) {
                    child.startAnimation(mInAnimation);
                }
                child.setVisibility(View.VISIBLE);
                mFirstTime = false;
            } else {
                if (animate && mOutAnimation != null && child.getVisibility() == View.VISIBLE) {
                    child.startAnimation(mOutAnimation);
                } else if (child.getAnimation() == mInAnimation)
                    child.clearAnimation();
                child.setVisibility(View.GONE);
            }
        }
    }

    void showOnly(int childIndex) {
        final boolean animate = (!mFirstTime || mAnimateFirstTime);
        showOnly(childIndex, animate);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        if (getChildCount() == 1) {
            child.setVisibility(View.VISIBLE);
        } else {
            child.setVisibility(View.GONE);
        }
        if (index >= 0 && mWhichChild >= index) {
            setDisplayedChild(mWhichChild + 1);
        }
    }

    @Override
    public void removeAllViews() {
        super.removeAllViews();
        mWhichChild = 0;
        mFirstTime = true;
    }

    @Override
    public void removeView(View view) {
        final int index = indexOfChild(view);
        if (index >= 0) {
            removeViewAt(index);
        }
    }

    @Override
    public void removeViewAt(int index) {
        super.removeViewAt(index);
        final int childCount = getChildCount();
        if (childCount == 0) {
            mWhichChild = 0;
            mFirstTime = true;
        } else if (mWhichChild >= childCount) {
            setDisplayedChild(childCount - 1);
        } else if (mWhichChild == index) {
            setDisplayedChild(mWhichChild);
        }
    }

    public void removeViewInLayout(View view) {
        removeView(view);
    }

    public void removeViews(int start, int count) {
        super.removeViews(start, count);
        if (getChildCount() == 0) {
            mWhichChild = 0;
            mFirstTime = true;
        } else if (mWhichChild >= start && mWhichChild < start + count) {
            setDisplayedChild(mWhichChild);
        }
    }

    public void removeViewsInLayout(int start, int count) {
        removeViews(start, count);
    }

    public View getCurrentView() {
        return getChildAt(mWhichChild);
    }

    public Animation getInAnimation() {
        return mInAnimation;
    }

    public void setInAnimation(Animation inAnimation) {
        mInAnimation = inAnimation;
    }

    public Animation getOutAnimation() {
        return mOutAnimation;
    }

    public void setOutAnimation(Animation outAnimation) {
        mOutAnimation = outAnimation;
    }

    public void setInAnimation(Context context, @AnimRes int resourceID) {
        setInAnimation(AnimationUtils.loadAnimation(context, resourceID));
    }

    public void setOutAnimation(Context context, @AnimRes int resourceID) {
        setOutAnimation(AnimationUtils.loadAnimation(context, resourceID));
    }

    public boolean getAnimateFirstView() {
        return mAnimateFirstTime;
    }

    public void setAnimateFirstView(boolean animate) {
        mAnimateFirstTime = animate;
    }

    @Override
    public int getBaseline() {
        return (getCurrentView() != null) ? getCurrentView().getBaseline() : super.getBaseline();
    }

    @Override
    public CharSequence getAccessibilityClassName() {
        return RotateMsgView.class.getName();
    }
}
