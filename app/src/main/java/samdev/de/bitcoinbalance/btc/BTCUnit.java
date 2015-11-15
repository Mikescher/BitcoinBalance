package samdev.de.bitcoinbalance.btc;

import android.util.Log;

import samdev.de.bitcoinbalance.R;

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



    public int getUnitIconResource() {
        switch(this) {
            case BTC:     return R.drawable.symbol_btc;
            case MBTC:    return R.drawable.symbol_mbtc;
            case BITS:    return R.drawable.symbol_ubtc;
            case SATOSHI: return R.drawable.symbol_sbtc;
        }

        Log.e("BTCBW", "Unknwon displayUnit: " + this);
        return -1;
    }

    public double getConversionFactor() {
        switch (this) {
            case BTC:     return CONVERSION_BTC;
            case MBTC:    return CONVERSION_MBTC;
            case BITS:    return CONVERSION_BITS;
            case SATOSHI: return CONVERSION_SATOSHI;
        }

        Log.e("BTCBW", "Unknwon displayUnit: " + this);
        return 1;
    }

    public int getIDValue() {
        return value;
    }
}
