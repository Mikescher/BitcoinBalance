package samdev.de.bitcoinbalance.btc;

import android.util.Log;

public enum BTCUnit {
    BTC(0),
    MBTC(1),
    BITS(2),
    SATOSHI(3);

    public final static double CONVERSION_BTC     = 100 * 1000 * 1000;
    public final static double CONVERSION_MBTC    = 100 * 1000;
    public final static double CONVERSION_BITS    = 100;
    public final static double CONVERSION_SATOSHI = 1;

    private int value;

    BTCUnit(int numVal) {
        this.value = numVal;
    }

    @SuppressWarnings("unused")
    public static BTCUnit ofNumericValue(String unit) {
        if (unit.equals("" + BTC.value)) return BTC;
        if (unit.equals("" + MBTC.value)) return MBTC;
        if (unit.equals("" + BITS.value)) return BITS;
        if (unit.equals("" + SATOSHI.value)) return SATOSHI;

        Log.e("BTCBW", "ofNumericValue parse error: " + unit);
        return BTC;
    }

    public static BTCUnit ofNumericValue(int unit) {
        if (unit == BTC.value) return BTC;
        if (unit == MBTC.value) return MBTC;
        if (unit == BITS.value) return BITS;
        if (unit == SATOSHI.value) return SATOSHI;

        Log.e("BTCBW", "ofNumericValue parse error: " + unit);
        return BTC;
    }

    public int getIDValue() {
        return value;
    }
}
