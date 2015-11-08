package samdev.de.bitcoinbalance;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

public class WidgetBackgroundView extends View {
    private int mBackgroundColor = Color.RED;
    private float mCurrencyRadius = 0;
    private float mCurrencyBorder = 0;
    private float mPadding = 0;

    public WidgetBackgroundView(Context context) {
        super(context);
    }

    public WidgetBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WidgetBackgroundView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float left = getPadding();
        float top = getPadding();
        float right = getWidth() - getPadding();
        float bottom = getHeight() - getPadding();

        Path pBackground = new Path();
        pBackground.moveTo(left, top);
        pBackground.moveTo(right, top);
        pBackground.moveTo(right, bottom);
        pBackground.moveTo(left, bottom);
        pBackground.close();

        // dann 2. path for circle
        // dann merge
        // evtl hier auch noch btc icon zeichnen

        Paint p = new Paint();
        p.setColor(getBackgroundColor());

        canvas.drawPath(pBackground, p);
    }

    public int getBackgroundColor() { return mBackgroundColor; }
    public void setBackgroundColor(int mBackgroundColor) { this.mBackgroundColor = mBackgroundColor; }

    public float getCurrencyRadius() { return mCurrencyRadius; }
    public void setCurrencyRadius(float mCurrencyRadius) { this.mCurrencyRadius = mCurrencyRadius; }

    public float getCurrencyBorder() { return mCurrencyBorder; }
    public void setCurrencyBorder(float mCurrencyBorder) { this.mCurrencyBorder = mCurrencyBorder; }

    public float getPadding() { return mPadding; }
    public void setPadding(float mPadding) { this.mPadding = mPadding; }
}
