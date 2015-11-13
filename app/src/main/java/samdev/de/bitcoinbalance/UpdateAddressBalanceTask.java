package samdev.de.bitcoinbalance;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InvalidObjectException;
import java.net.HttpURLConnection;
import java.net.URL;

import samdev.de.bitcoinbalance.btc.BitcoinAddress;

public class UpdateAddressBalanceTask extends AsyncTask<BitcoinAddress, Integer, Long> {

    private final static String URL_BLOCKCHAIN    = "https://blockchain.info/q/addressbalance/%s?confirmations=6";
    private final static String URL_BLOCKEXPLORER = "https://blockexplorer.com/api/addr/%s/balance";
    private final static String URL_TOSHI         = "https://bitcoin.toshi.io/api/v0/addresses/%s";
    private final static String URL_BLOCKCYPHER   = "https://api.blockcypher.com/v1/btc/main/addrs/%s/balance";

    private final UpdateSource source;

    public SuccessValue success = SuccessValue.INTERMEDIATE;

    public UpdateAddressBalanceTask(UpdateSource _source) {
        source = _source;
    }

    @Override
    protected Long doInBackground(BitcoinAddress... params) {

        success = SuccessValue.INTERMEDIATE;

        try {
            long result;

            switch (source) {
                case BLOCKCHAIN:
                    result = QueryBlockchain(params[0]);
                    success = SuccessValue.SUCCESS;
                    return result;
                case BLOCKEXPLORER:
                    result = QueryBlockexplorer(params[0]);
                    success = SuccessValue.SUCCESS;
                    return result;
                case TOSHI:
                    result = QueryToshi(params[0]);
                    success = SuccessValue.SUCCESS;
                    return result;
                case BLOCKCYPHER:
                    result = QueryBlockcypher(params[0]);
                    success = SuccessValue.SUCCESS;
                    return result;
            }

            Log.e("BTCBW", "Unknwon source type: " + source);
            success = SuccessValue.ERROR;
            return (long) -1;

        } catch (Exception e) {
            Log.e("BTCBW", e.toString());
            success = SuccessValue.ERROR;
            return (long) -1;
        }

    }
    private long QueryBlockchain(BitcoinAddress btcaddr) throws IOException {
        String content = GetHttpContent(String.format(URL_BLOCKCHAIN, btcaddr.address));

        long value = Long.parseLong(content);

        if (value < 0) throw new InvalidObjectException("value is " + value + "' for blockchain.info in " + btcaddr.address);

        return value;
    }

    private long QueryBlockexplorer(BitcoinAddress btcaddr) throws IOException {
        String content = GetHttpContent(String.format(URL_BLOCKEXPLORER, btcaddr.address));

        long value = Long.parseLong(content);

        if (value < 0) throw new InvalidObjectException("value is " + value + "' for blockexplorer.com in " + btcaddr.address);

        return value;
    }

    private long QueryToshi(BitcoinAddress btcaddr) throws JSONException, IOException {
        String content = GetHttpContent(String.format(URL_TOSHI, btcaddr.address));
        JSONObject obj = new JSONObject(content);

        return obj.getLong("balance");
    }

    private long QueryBlockcypher(BitcoinAddress btcaddr) throws JSONException, IOException {
        String content = GetHttpContent(String.format(URL_BLOCKCYPHER, btcaddr.address));
        JSONObject obj = new JSONObject(content);

        return obj.getLong("balance");
    }

    private String GetHttpContent(String url) throws IOException {
        StringBuilder result = new StringBuilder();
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) new URL(url).openConnection();

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

        }
        finally {
            if (urlConnection != null) urlConnection.disconnect();
        }

        return result.toString();
    }

    protected void onProgressUpdate(Integer... progress) {
        // NOP
    }

    protected void onPostExecute(Long result) {
        // NOP
    }

}
