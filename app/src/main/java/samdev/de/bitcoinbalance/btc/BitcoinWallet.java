package samdev.de.bitcoinbalance.btc;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import samdev.de.bitcoinbalance.R;
import samdev.de.bitcoinbalance.async.UpdateState;

public class BitcoinWallet {
    private List<BitcoinAddress> addresses = new ArrayList<>();
    private final BTCUnit displayUnit;

    public BitcoinWallet(BTCUnit unit) {
        displayUnit = unit;
    }

    public BitcoinWallet() {
        displayUnit = BTCUnit.BTC;
    }

    public long getBalance() {
        long balance = 0;

        for (BitcoinAddress addr: addresses) {
            if (addr.getStatus() != UpdateState.INITIAL)
            balance += addr.getBalance();
        }

        return balance;
    }

    public String getFormattedBalance() {
        if (addresses.size() == 0) return "EMPTY";

        long balance = 0;

        for (BitcoinAddress addr: addresses) {
            if (addr.getStatus() == UpdateState.INITIAL) return "...";
            if (addr.getStatus() == UpdateState.ERROR) return "ERROR";
                balance += addr.getBalance();
        }

        return formatLengthSensitive(balance / displayUnit.getConversionFactor());
    }

    private String formatLengthSensitive(double v) {
        if ((int)v == 0) return new DecimalFormat("#.####").format(v);

        return new DecimalFormat("#.##").format(v);
    }

    public BTCUnit getUnit() {
        return displayUnit;
    }

    public BitcoinAddress getMainAddress() {
        return (addresses.size() > 0) ? addresses.get(0) : null;
    }

    public void addAddress(BitcoinAddress addr) {
        addresses.add(addr);
    }

    public String serialize() {
        try {
            JSONObject obj = new JSONObject();

            JSONArray addrArray = new JSONArray();
            for (BitcoinAddress addr : addresses){
                addrArray.put(addr.serialize());
            }

            obj.put("unit", displayUnit.getIDValue());
            obj.put("addresses", addrArray);

            return obj.toString();
        } catch (JSONException e) {
            Log.e("BTCBW", e.toString());
            return "";
        }
    }

    public static BitcoinWallet deserialize(String data) {
        if (data == null) {
            Log.w("BTCBW", "No wallet prefs found - using empty");
            return new BitcoinWallet();
        }

        try {
            JSONObject obj = new JSONObject(data);

            BTCUnit unit = BTCUnit.ofNumericValue(obj.getInt("unit"));
            BitcoinWallet wallet = new BitcoinWallet(unit);

            JSONArray addrArray = obj.getJSONArray("addresses");
            for (int i = 0; i < addrArray.length(); i++) {
                try {
                    wallet.addAddress(BitcoinAddress.deserialize(addrArray.getJSONObject(i)));
                } catch (Exception e) {
                    Log.e("BTCBW", "ERROR loading address from wallet: " + e.toString());
                }
            }

            return wallet;
        } catch (JSONException e) {
            Log.e("BTCBW", "ERROR loading wallet: " + e.toString());
            return new BitcoinWallet();
        }
    }

    public void updateValue() {
        for (BitcoinAddress addr : addresses){
            addr.updateValue();
        }
    }

    public int getAddressCount() {
        return addresses.size();
    }

    public boolean hasMainAddress() {
        return addresses.size() > 0;
    }

    public String getStateText() {
        if (addresses.size() == 0) return "empty wallet";

        for (BitcoinAddress addr: addresses) {
            if (addr.getStatus() == UpdateState.ERROR) return "(update error)";
        }

        return "";
    }
}
