package com.xzhou.book.read;

import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.xzhou.book.utils.AppSettings;
import com.xzhou.book.widget.SwipeLayout;

public class ReadViewPager extends ViewPager {
    private static final String TAG = "ReadViewPager";
    private boolean isCanScroll = true; //是否可以切换页面
    private boolean isCanTouch = true; //是否可以手势滑动
    private boolean isCanLeftTouch = true; //是否可以左滑
    private SwipeLayout mSwipeLayout;
    private float mDownX, mLastX;
    private float mDownY;
    private int mScaledTouchSlop;
    private RectF mCenterRect = new RectF();
    private boolean isMove = false;
    private ReadPageManager[] mPageManagers;

    private OnClickChangePageListener mClickChangePageListener;
    private OnClickListener mOnClickListener;

    public interface OnClickChangePageListener {
        void onPrevious();

        void onNext();
    }

    public ReadViewPager(@NonNull Context context) {
        this(context, null);
    }

    public ReadViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mScaledTouchSlop = configuration.getScaledTouchSlop();
    }

    public void setOnClickChangePageListener(OnClickChangePageListener listener) {
        mClickChangePageListener = listener;
    }

    public void setCanScroll(boolean isCanScroll) {
        this.isCanScroll = isCanScroll;
    }

    public void setCanTouch(boolean isCanScroll) {
        this.isCanTouch = isCanScroll;
    }

    public void setCanLeftTouch(boolean canLeftTouch) {
        isCanLeftTouch = canLeftTouch;
    }

    public void setPageManagers(ReadPageManager[] pageManagers) {
        mPageManagers = pageManagers;
    }

    public void setSwipeLayout(SwipeLayout layout) {
        mSwipeLayout = layout;
    }

    @Override
    public void scrollTo(int x, int y) {
        if (isCanScroll) {
            super.scrollTo(x, y);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float left = getMeasuredWidth() / 4f;
        float right = left * 3;
        float top = getMeasuredHeight() / 4f;
        float bottom = top * 3;
        mCenterRect.set(left, top, right, bottom);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
//        Log.d(TAG, "onTouchEvent:" + ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = mDownX = ev.getRawX();
                mDownY = ev.getRawY();
                isMove = false;
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!isMove) {
                    isMove = Math.abs(mDownX - ev.getRawX()) > mScaledTouchSlop || Math.abs(mDownY - ev.getRawY()) > mScaledTouchSlop;
                }
                if (mSwipeLayout != null) {
                    if (hasCurEndPager() || !isCanLeftTouch) {
                        if (mDownX - ev.getRawX() > 0) {
                            //左滑
                            int deltaX = (int) (mLastX - ev.getRawX());
                            mSwipeLayout.onMove(deltaX);
                            mLastX = ev.getRawX();
                            return false;
                        } else {
                            if (mSwipeLayout.getCurrentState() != SwipeLayout.STATE_CLOSED) {
                                //右滑
                                mSwipeLayout.smoothToCloseMenu();
                                return false;
                            }
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mSwipeLayout != null) {
                    mSwipeLayout.onUpOrCancel();
                }
                float upX = ev.getRawX();
                float upY = ev.getRawY();
                if (!isMove) {
                    if (mCenterRect.contains(upX, upY)) {
                        return performClick();
                    } else if (upX < mCenterRect.right && (upY < mCenterRect.bottom || upX < mCenterRect.left)) {
                        if (mClickChangePageListener != null) {
                            if (AppSettings.HAS_CLICK_NEXT_PAGE) {
                                mClickChangePageListener.onNext();
                            } else {
                                mClickChangePageListener.onPrevious();
                            }
                        }
                    } else {
                        if (mClickChangePageListener != null) {
                            mClickChangePageListener.onNext();
                        }
                    }
                    return true;
                }
                break;
        }
        return isCanTouch && super.onTouchEvent(ev);
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        mOnClickListener = listener;
    }

    @Override
    public boolean performClick() {
        if (mOnClickListener != null) {
            mOnClickListener.onClick(this);
            return true;
        }
        return super.performClick();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isCanTouch) {
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public boolean hasCurEndPager() {
        PagerAdapter adapter = getAdapter();
        if (adapter == null) {
            return false;
        }
        int curPos = getCurrentItem();
        PageContent page = mPageManagers[curPos].getReadPage().getPageContent();
        return page != null && page.isEnd;
    }
}
