package samdev.de.bitcoinbalance.async;

import android.util.Log;

public enum UpdateState {
    INITIAL(0),
    SUCCESS(100),
    FALLBACK(99), // Use old value
    ERROR(-1);    // value = -1

    private int value;

    UpdateState(int numVal) {
        this.value = numVal;
    }

    public static UpdateState ofNumericValue(String unit) {
        if (unit.equals("" + INITIAL.value))  return INITIAL;
        if (unit.equals("" + SUCCESS.value))  return SUCCESS;
        if (unit.equals("" + FALLBACK.value)) return FALLBACK;
        if (unit.equals("" + ERROR.value))    return ERROR;

        Log.e("BTCBW", "ofNumericValue parse error: " + unit);
        return INITIAL;
    }

    public static UpdateState ofNumericValue(int unit) {
        if (unit == INITIAL.value)  return INITIAL;
        if (unit == SUCCESS.value)  return SUCCESS;
        if (unit == FALLBACK.value) return FALLBACK;
        if (unit == ERROR.value)    return ERROR;

        Log.e("BTCBW", "ofNumericValue parse error: " + unit);
        return INITIAL;
    }

    public int getIDValue() {
        return value;
    }
}
