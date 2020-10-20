package com.xzhou.book.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class DrawableButton extends AppCompatTextView {

    public DrawableButton(Context context) {
        super(context);
    }

    public DrawableButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawableButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable[] drawables = getCompoundDrawables();
        Drawable drawableLeft = drawables[0];
        if (drawableLeft != null) {
            float textWidth = getPaint().measureText(getText().toString());
            int drawablePadding = getCompoundDrawablePadding();
            float bodyWidth = textWidth + drawableLeft.getIntrinsicWidth() + drawablePadding;
            canvas.translate((getWidth() - bodyWidth) / 2, 0);
        }
        Drawable drawableTop = drawables[1];
        if (drawableTop != null) {
            float textSize = getPaint().getTextSize();
            int drawablePadding = getCompoundDrawablePadding();
            float bodyH = textSize + drawableTop.getIntrinsicHeight() + drawablePadding;
            float dy = ((float) getHeight() - bodyH) / 2f;
            canvas.translate(0, dy - (dy / 5f));
        }
        Drawable drawableRight = drawables[2];
        if (drawableRight != null) {
            float textWidth = getPaint().measureText(getText().toString());
            int drawablePadding = getCompoundDrawablePadding();
            float bodyWidth = textWidth + drawableRight.getIntrinsicWidth() + drawablePadding;
            setPadding(getPaddingLeft(), getPaddingTop(), (int) (getWidth() - bodyWidth), getPaddingBottom());
            canvas.translate((getWidth() - bodyWidth) / 2, 0);
        }
        super.onDraw(canvas);
    }
}