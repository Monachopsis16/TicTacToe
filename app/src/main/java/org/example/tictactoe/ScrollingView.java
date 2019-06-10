package org.example.tictactoe;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * 该自定义的视图实现绘制一幅滚动的图像
 */

public class ScrollingView extends View {
    private Drawable mBackground;
    private int mScrollPos;

    public ScrollingView(Context context) {
        super(context);
        init(null, 0);
    }

    public ScrollingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ScrollingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // 加入自定义的视图属性
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ScrollingView, defStyle, 0);

        // 获取背景图像
        if (a.hasValue(R.styleable.ScrollingView_scrollingDrawable)) {
            mBackground = a.getDrawable(
                    R.styleable.ScrollingView_scrollingDrawable);
            mBackground.setCallback(this);
        }

        // 回收自定义的视图属性
        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 获取视图的大小
        int contentWidth = getWidth();
        int contentHeight = getHeight();

        // 绘制背景
        if (mBackground != null) {
            // Make the background bigger than it needs to be
            int max = Math.max(mBackground.getIntrinsicHeight(),
                    mBackground.getIntrinsicWidth());
            mBackground.setBounds(0, 0, contentWidth * 4, contentHeight * 4);

            // 当图像绘制完成后进行移动
            mScrollPos += 2;
            if (mScrollPos >= max) mScrollPos -= max;
            canvas.translate(-mScrollPos, -mScrollPos);

            // 指出下次刷新时仍需绘制
            mBackground.draw(canvas);
            this.invalidate();
        }
    }
}
