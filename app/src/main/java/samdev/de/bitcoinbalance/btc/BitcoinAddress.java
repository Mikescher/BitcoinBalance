package samdev.de.bitcoinbalance.btc;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.text.DecimalFormat;

import samdev.de.bitcoinbalance.async.UpdateState;
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

    private final String address;

    private long balance = -1;

    private UpdateState LastUpdateState = UpdateState.INITIAL;

    public BitcoinAddress(String addr) {
        address = addr;
    }

    public boolean UpdateValue() {
        LastUpdateState = UpdateState.PENDING;

        try {
            long v = QueryBlockchain();
            balance = v;
            LastUpdateState = UpdateState.SUCCESS;
            return true;
        } catch (Exception e) {
            Log.w("BTCBW", "Site not reachable: blockchain.info");
        }

        try {
            long v = QueryBlockexplorer();
            balance = v;
            LastUpdateState = UpdateState.SUCCESS;
            return true;
        } catch (Exception e) {
            Log.w("BTCBW", "Site not reachable: blockexplorer.com");
        }

        try {
            long v = QueryToshi();
            balance = v;
            LastUpdateState = UpdateState.SUCCESS;
            return true;
        } catch (Exception e) {
            Log.w("BTCBW", "Site not reachable: toshi.io");
        }

        try {
            long v = QueryBlockcypher();
            balance = v;
            LastUpdateState = UpdateState.SUCCESS;
            return true;
        } catch (Exception e) {
            Log.w("BTCBW", "Site not reachable: blockcypher.com");
        }

        LastUpdateState = UpdateState.ERROR;
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

        switch (LastUpdateState) {

            case INITIAL:
                return "";
            case SUCCESS:
                switch (unit) {
                    case BTC:     return String.format("(%s BTC)", new DecimalFormat("#.####").format(balance / CONVERSION_BTC));
                    case MBTC:    return String.format("(%s mBTC)", new DecimalFormat("#.####").format(balance / CONVERSION_MBTC));
                    case BITS:    return String.format("(%s bits)", new DecimalFormat("#.####").format(balance / CONVERSION_BITS));
                    case SATOSHI: return String.format("(%s sat.)", new DecimalFormat("#.####").format(balance / CONVERSION_SATOSHI));
                }

                Log.e("BTCBW", "WTF BTCUnit := " + unit);
                return "?";
            case ERROR:
                return "ERROR";
            case PENDING:
                return "...";
        }

        Log.e("BTCBW", "WTF LastUpdateState := " + LastUpdateState);
        return "?";
    }
}
