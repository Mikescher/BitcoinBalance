package samdev.de.bitcoinbalance.btc;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.net.URI;
import java.text.DecimalFormat;

import samdev.de.bitcoinbalance.async.UpdateState;
import samdev.de.bitcoinbalance.helper.NetworkHelper;

public class BitcoinAddress {
    private final static String URL_BLOCKCHAIN    = "https://blockchain.info/q/addressbalance/%s?confirmations=6";
    private final static String URL_BLOCKEXPLORER = "https://blockexplorer.com/api/addr/%s/balance";
    private final static String URL_TOSHI         = "https://bitcoin.toshi.io/api/v0/addresses/%s";
    private final static String URL_BLOCKCYPHER   = "https://api.blockcypher.com/v1/btc/main/addrs/%s/balance";


    private final String address;

    private long balance = -1;

    private UpdateState lastUpdateState = UpdateState.INITIAL;

    public BitcoinAddress(String addr) {
        address = addr;
    }

    public boolean updateValue() {
        try {
            balance = QueryBlockchain();
            lastUpdateState = UpdateState.SUCCESS;
            return true;
        } catch (Exception e) {
            Log.w("BTCBW", "Site not reachable: blockchain.info");
        }

        try {
            balance = QueryBlockexplorer();
            lastUpdateState = UpdateState.SUCCESS;
            return true;
        } catch (Exception e) {
            Log.w("BTCBW", "Site not reachable: blockexplorer.com");
        }

        try {
            balance = QueryToshi();
            lastUpdateState = UpdateState.SUCCESS;
            return true;
        } catch (Exception e) {
            Log.w("BTCBW", "Site not reachable: toshi.io");
        }

        try {
            balance = QueryBlockcypher();
            lastUpdateState = UpdateState.SUCCESS;
            return true;
        } catch (Exception e) {
            Log.w("BTCBW", "Site not reachable: blockcypher.com");
        }

        if (lastUpdateState == UpdateState.SUCCESS || lastUpdateState == UpdateState.FALLBACK) {
            lastUpdateState = UpdateState.FALLBACK;
        } else {
            balance = -1;
            lastUpdateState = UpdateState.ERROR;
        }
        return false;
    }

    private long QueryBlockchain() throws IOException, NumberFormatException {
        String content = NetworkHelper.GetHttpContent(String.format(URL_BLOCKCHAIN, address));

        long value = Long.parseLong(content);

        if (value < 0) throw new InvalidObjectException("value is " + value + "' for blockchain.info in " + address);

        return value;
    }

    private long QueryBlockexplorer() throws IOException, NumberFormatException {
        String content = NetworkHelper.GetHttpContent(String.format(URL_BLOCKEXPLORER, address));

        long value = Long.parseLong(content);

        if (value < 0) throw new InvalidObjectException("value is " + value + "' for blockexplorer.com in " + address);

        return value;
    }

    private long QueryToshi() throws JSONException, IOException, NumberFormatException {
        String content = NetworkHelper.GetHttpContent(String.format(URL_TOSHI, address));
        JSONObject obj = new JSONObject(content);

        return obj.getLong("balance");
    }

    private long QueryBlockcypher() throws JSONException, IOException, NumberFormatException {
        String content = NetworkHelper.GetHttpContent(String.format(URL_BLOCKCYPHER, address));
        JSONObject obj = new JSONObject(content);

        return obj.getLong("balance");
    }

    public String getFullAddress() {
        return address;
    }

    public long getBalance() {
        return balance;
    }

    public String getFormattedBalance(BTCUnit unit) {
        switch (lastUpdateState) {
            case INITIAL:
                return "...";
            case SUCCESS:
            case FALLBACK:
                switch (unit) {
                    case BTC:     return String.format("(%s BTC)",  new DecimalFormat("#.######").format(balance / BTCUnit.CONVERSION_BTC));
                    case MBTC:    return String.format("(%s mBTC)", new DecimalFormat("#.######").format(balance / BTCUnit.CONVERSION_MBTC));
                    case BITS:    return String.format("(%s bits)", new DecimalFormat("#.######").format(balance / BTCUnit.CONVERSION_BITS));
                    case SATOSHI: return String.format("(%s sat.)", new DecimalFormat("#.######").format(balance / BTCUnit.CONVERSION_SATOSHI));
                }

                Log.e("BTCBW", "WTF BTCUnit := " + unit);
                return "?";
            case ERROR:
                return "ERROR";
        }

        Log.e("BTCBW", "WTF lastUpdateState := " + lastUpdateState);
        return "?";
    }

    public UpdateState getStatus() {
        return lastUpdateState;
    }

    public JSONObject serialize() {
        try {
            JSONObject obj = new JSONObject();

            obj.put("addr", address);
            obj.put("state", lastUpdateState.getIDValue());
            obj.put("balance", balance);

            return obj;
        } catch (JSONException e) {
            Log.e("BTCBW", e.toString());
            return null;
        }
    }

    public static BitcoinAddress deserialize(JSONObject object) throws JSONException {
        BitcoinAddress addr = new BitcoinAddress(object.getString("addr"));

        addr.lastUpdateState = UpdateState.ofNumericValue(object.getInt("state"));
        addr.balance = object.getLong("balance");

        return addr;
    }

    public String getBIP21Address() {
        return String.format("bitcoin:%s", getFullAddress());
    }

    public static BitcoinAddress parse(String contents) {
        if (contents == null) return null;

        if (contents.toLowerCase().startsWith("bitcoin:")) contents = contents.substring("bitcoin:".length());
        if (contents.contains("?")) contents = contents.substring(0, contents.indexOf('?'));

        if (BitcoinHelper.ValidateBitcoinAddress(contents)) return new BitcoinAddress(contents);

        return null;
    }
}
