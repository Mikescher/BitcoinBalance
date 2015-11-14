package samdev.de.bitcoinbalance.helper;

import android.content.Context;
import android.content.SharedPreferences;

import samdev.de.bitcoinbalance.btc.BitcoinWallet;

public final class PerferencesHelper {
    private static final String PREFS_NAME = "samdev.de.bitcoinbalance.BTCWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";

    public static void savePrefWallet(Context context, int appWidgetId, BitcoinWallet wallet) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId + "_" + "wallet", wallet.serialize());
        prefs.commit();
    }

    public static BitcoinWallet loadPrefWallet(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String value = prefs.getString(PREF_PREFIX_KEY + appWidgetId + "_" + "wallet", null);

        return BitcoinWallet.deserialize(value);
    }

    public static void deletePrefWallet(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId + "_" + "wallet");
        prefs.commit();
    }
}
