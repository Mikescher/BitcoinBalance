package samdev.de.bitcoinbalance.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.google.zxing.common.BitMatrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import samdev.de.bitcoinbalance.BuildConfig;
import samdev.de.bitcoinbalance.R;
import samdev.de.bitcoinbalance.helper.CanvasHelper;

public class QRCodeView extends View {
    public final static int BTC_QR_SIZE = 29;
    private final static Random RAND = new Random();

    private String master = "unset";

    private Paint blackPaint;
    private List<QRCodelet> codelets = new ArrayList<>();
    private BitMatrix currentMatrix = null;
    private int qrWidth = 1;
    private int qrHeight = 1;

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

    private boolean isTransforming = false;
    private long mLastUpdate = 0;
    private Handler mHandler = new Handler();
    private Runnable mTick = new Runnable() {
        public void run() {
            long delta = Math.min(SystemClock.uptimeMillis() - mLastUpdate, 100);
            mLastUpdate = SystemClock.uptimeMillis();
            isTransforming = true;

            boolean finished = updateAnimation(delta);

            invalidate();

            if (! finished) {
                mHandler.postDelayed(this, 20); // 20ms == 50fps
            } else {
                int codelets_before = codelets.size();

                for (int i = codelets.size() - 1; i >= 0; i--) {
                    if (codelets.get(i).deleteAfterMovement) codelets.remove(i);
                }

                isTransforming = false;
                Log.d("BTCBW", String.format("Codelet transformation finished codelets: %d --> %d", codelets_before, codelets.size()));
            }
        }
    };

    private boolean updateAnimation(long delta) {
        boolean finished = true;

        for (QRCodelet codelet : codelets) {
            boolean fin = codelet.update(delta);

            finished &= fin;
        }

        return finished;
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes( attrs, R.styleable.QRCodeView, defStyle, 0);

        master = a.getString(R.styleable.QRCodeView_master);

        a.recycle();

        blackPaint = new Paint();
        blackPaint.setColor(Color.BLACK);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.TRANSPARENT);

        float w = (canvas.getWidth() * 1f) / qrWidth;
        float h = (canvas.getHeight() * 1f) / qrHeight;
        float sz = Math.min(w, h);

        for (QRCodelet codelet : codelets) {
            if (codelet.transformation == 1f) {
                canvas.drawRect(codelet.posX * w, codelet.posY * h, (codelet.posX + 1) * w, (codelet.posY + 1) * h, blackPaint);
            } else if (codelet.transformation == 0f) {
                canvas.drawCircle((codelet.posX + 0.5f) * w, (codelet.posY + 0.5f) * h, sz/2, blackPaint);
            } else {
                CanvasHelper.drawRoundedSquare(canvas, codelet.posX * w, codelet.posY * h, sz, 1 - codelet.transformation, blackPaint);
            }
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int displaySize = getDisplaySize();
        setMeasuredDimension(displaySize, displaySize);
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

    public void setQR(BitMatrix code) {
        if (!isTransforming && currentMatrix != null && MatrixEquals(currentMatrix, code)) return;
        currentMatrix = code;

        qrWidth = code.getWidth();
        qrHeight = code.getHeight();

        if (codelets.isEmpty()) {
            InitQRTransformation(code);
        } else {
            startQRTransformation(code);
        }

        mLastUpdate = SystemClock.uptimeMillis();
        mHandler.removeCallbacks(mTick);
        mHandler.post(mTick);
    }

    private boolean MatrixEquals(BitMatrix ma, BitMatrix mb) {
        if (ma.getWidth() != mb.getWidth()) return false;
        if (ma.getHeight() != mb.getHeight()) return false;

        for (int x = 0; x < ma.getWidth(); x++) {
            for (int y = 0; y < ma.getHeight(); y++) {
                if (ma.get(x, y) ^ mb.get(x, y)) return false;
            }
        }

        return true;
    }

    private void InitQRTransformation(BitMatrix code) {
        float centerX = (qrWidth - 1)/2f;
        float centerY = (qrHeight - 1)/2f;

        for (int x = 0; x < qrWidth; x++) {
            for (int y = 0; y < qrHeight; y++) {
                if (code.get(x, y)) {
                    QRCodelet codelet = new QRCodelet(centerX, centerY, x, y, true);

                    codelets.add(codelet);
                }
            }
        }

        for (QRCodelet clet : codelets) {
            clet.speedMultiplier = 0.6f + RAND.nextFloat() * 0.8f;
            clet.movementMultiplier = 1f;
        }
    }

    private void startQRTransformation(BitMatrix code) {
        if (BuildConfig.DEBUG && codelets.isEmpty()) throw new AssertionError();

        List<QRCodelet> unused = new ArrayList<>(codelets);
        List<QRCodelet> used = new ArrayList<>();

        for (int x = 0; x < qrWidth; x++) {
            for (int y = 0; y < qrHeight; y++) {
                if (code.get(x, y)) {
                    QRCodelet clet;

                    if (unused.size() == 0) {
                        clet = new QRCodelet(codelets.get(RAND.nextInt(codelets.size())));
                        codelets.add(clet);
                    } else {
                        clet = unused.get(RAND.nextInt(unused.size()));
                        unused.remove(clet);
                        used.add(clet);
                    }

                    clet.targetX = x;
                    clet.targetY = y;
                    clet.deleteAfterMovement = false;
                }
            }
        }

        while (! unused.isEmpty()) {
            QRCodelet target = used.get(RAND.nextInt(used.size()));
            QRCodelet source = unused.get(0);
            unused.remove(source);

            source.targetX = target.targetX;
            source.targetY = target.targetY;

            source.deleteAfterMovement = true;
        }

        for (QRCodelet clet : codelets) {
            clet.speedMultiplier = 1f + RAND.nextFloat() * 0.8f;
            clet.movementMultiplier = 1.35f;
        }
    }

    public void hardReset(boolean refresh) {
        codelets.clear();
        currentMatrix = null;

        mHandler.removeCallbacks(mTick);
        if (refresh) invalidate();
    }
}