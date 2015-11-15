package samdev.de.bitcoinbalance.helper;

import android.graphics.Canvas;
import android.graphics.Paint;

public final class CanvasHelper {
    public static void drawRoundedSquare(Canvas c, float x, float y, float sz, float roundness, Paint p) {
        float r = (sz)/2f * roundness;

        c.drawRect(x + r, y, x + sz - r, y + sz, p);
        c.drawRect(x, y + r, x + sz, y + sz - r, p);

        c.drawCircle(x + r, y + r, r, p);
        c.drawCircle(x + sz - r, y + r, r, p);
        c.drawCircle(x + r, y + sz - r, r, p);
        c.drawCircle(x + sz -  r, y + sz - r, r, p);
    }
}
