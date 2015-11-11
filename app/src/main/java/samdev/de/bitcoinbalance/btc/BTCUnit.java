package samdev.de.bitcoinbalance.btc;

import android.util.Log;

public enum BTCUnit {
    BTC(0),
    MBTC(1),
    BITS(2),
    SATOSHI(3);

    private int value;

    BTCUnit(int numVal) {
        this.value = numVal;
    }

    public static BTCUnit ofNumericValue(String unit) {
        if (unit.equals("" + BTC.value)) return BTC;
        if (unit.equals("" + BTC.value)) return MBTC;
        if (unit.equals("" + BTC.value)) return BITS;
        if (unit.equals("" + BTC.value)) return SATOSHI;

        Log.e("BTCBW", "ofNumericValue parse error: " + unit);
        return BTC;
    }
}
