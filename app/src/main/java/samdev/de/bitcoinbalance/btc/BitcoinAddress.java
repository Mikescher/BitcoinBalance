package samdev.de.bitcoinbalance.btc;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.text.DecimalFormat;

import samdev.de.bitcoinbalance.helper.NetworkHelper;

public class BitcoinAddress {
    private final static String URL_BLOCKCHAIN    = "https://blockchain.info/q/addressbalance/%s?confirmations=6";
    private final static String URL_BLOCKEXPLORER = "https://blockexplorer.com/api/addr/%s/balance";
    private final static String URL_TOSHI         = "https://bitcoin.toshi.io/api/v0/addresses/%s";
    private final static String URL_BLOCKCYPHER   = "https://api.blockcypher.com/v1/btc/main/addrs/%s/balance";

    private final static double CONVERSION_BTC     = 100 * 1000 * 1000;
    private final static double CONVERSION_MBTC    = 100 * 1000;
    private final static double CONVERSION_BITS    = 100;
    private final static double CONVERSION_SATOSHI = 1;

    public final String address;

    public long balance = -1;

    public BitcoinAddress(String addr) {
        address = addr;
    }

    public boolean UpdateValue() {
        try {
            long v = QueryBlockchain();
            balance = v;
            return true;
        } catch (Exception e) {
            Log.w("BTCBW", "Site not reachable: blockchain.info");
        }

        try {
            long v = QueryBlockexplorer();
            balance = v;
            return true;
        } catch (Exception e) {
            Log.w("BTCBW", "Site not reachable: blockchain.info");
        }

        try {
            long v = QueryToshi();
            balance = v;
            return true;
        } catch (Exception e) {
            Log.w("BTCBW", "Site not reachable: blockchain.info");
        }

        try {
            long v = QueryBlockcypher();
            balance = v;
            return true;
        } catch (Exception e) {
            Log.w("BTCBW", "Site not reachable: blockchain.info");
        }

        return false;
    }

    private long QueryBlockchain() throws IOException {
        String content = NetworkHelper.GetHttpContent(String.format(URL_BLOCKCHAIN, address));

        long value = Long.parseLong(content);

        if (value < 0) throw new InvalidObjectException("value is " + value + "' for blockchain.info in " + address);

        return value;
    }

    private long QueryBlockexplorer() throws IOException {
        String content = NetworkHelper.GetHttpContent(String.format(URL_BLOCKEXPLORER, address));

        long value = Long.parseLong(content);

        if (value < 0) throw new InvalidObjectException("value is " + value + "' for blockexplorer.com in " + address);

        return value;
    }

    private long QueryToshi() throws JSONException, IOException {
        String content = NetworkHelper.GetHttpContent(String.format(URL_TOSHI, address));
        JSONObject obj = new JSONObject(content);

        return obj.getLong("balance");
    }

    private long QueryBlockcypher() throws JSONException, IOException {
        String content = NetworkHelper.GetHttpContent(String.format(URL_BLOCKCYPHER, address));
        JSONObject obj = new JSONObject(content);

        return obj.getLong("balance");
    }

    public String getShortAddress() {
        return address.substring(0, 8) + " ... " + address.substring(address.length() - 4);
    }


    public boolean isBalanceSet() {
        return balance >= 0;
    }

    public String getFormattedBalance(BTCUnit unit) {
        switch (unit) {

            case BTC:     return new DecimalFormat("#.####").format(balance / CONVERSION_BTC)     + " BTC";
            case MBTC:    return new DecimalFormat("#.####").format(balance / CONVERSION_MBTC)    + " mBTC";
            case BITS:    return new DecimalFormat("#.####").format(balance / CONVERSION_BITS)    + " bits";
            case SATOSHI: return new DecimalFormat("#.####").format(balance / CONVERSION_SATOSHI) + " sat.";
        }

        Log.e("BTCBW", "WTF BTCUnit := " + unit);

        return "?";
    }
}
