package samdev.de.bitcoinbalance.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.zxing.common.BitMatrix;

import samdev.de.bitcoinbalance.R;

public class QRCodeView extends ImageView {
    public final static int BTC_QR_SIZE = 29;

    private String master = "unset";
    private BitMatrix qrCode;


    public QRCodeView(Context context) {
        super(context);
        init(null, 0);
    }

    public QRCodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public QRCodeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }


    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes( attrs, R.styleable.QRCodeView, defStyle, 0);

        master = a.getString(R.styleable.QRCodeView_master);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int displaySize = getDisplaySize();
        setMeasuredDimension(displaySize, displaySize);

        if (master.equalsIgnoreCase("width")) {
            int w = this.getMeasuredWidth();
            int h = w;

            setMeasuredDimension(w, h);
        } else if (master.equalsIgnoreCase("height")) {
            int h = this.getMeasuredHeight();
            int w = h;

            setMeasuredDimension(w, h);
        } else  {
            int h = this.getMeasuredHeight();
            int w = this.getMeasuredWidth();

            int squareDim = Math.max(w, h);

            setMeasuredDimension(squareDim, squareDim);
        }
    }

    private int getDisplaySize() {
        if (master.equalsIgnoreCase("width")) {
            return this.getMeasuredWidth();
        } else if (master.equalsIgnoreCase("height")) {
            return this.getMeasuredHeight();
        } else  {
            return Math.max(this.getMeasuredWidth(), this.getMeasuredHeight());
        }
    }

    private void updateImage() {
        final int w = qrCode.getWidth();
        final int h = qrCode.getHeight();

        Bitmap img = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                img.setPixel(x, y, qrCode.get(x, y) ? Color.BLACK: Color.WHITE);
            }
        }

        BitmapDrawable bmp = new BitmapDrawable(getResources(), img);
        bmp.setAntiAlias(false);
        bmp.setFilterBitmap(false);
        setImageDrawable(bmp);
    }

    public void setQR(BitMatrix code) {
        qrCode = code;

        updateImage();
    }
}