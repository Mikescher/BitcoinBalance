package samdev.de.bitcoinbalance.async;

import android.os.AsyncTask;
import android.util.Log;

import samdev.de.bitcoinbalance.btc.BitcoinAddress;

public class UpdateAddressBalanceTask extends AsyncTask<BitcoinAddress, Integer, Boolean> {
    private final TaskListener listener;

    public UpdateAddressBalanceTask(TaskListener _listener) {
        listener = _listener;
    }

    @Override
    protected void onPreExecute() {
        listener.onTaskStarted();
    }

    @Override
    protected Boolean doInBackground(BitcoinAddress... params) {

        Log.d("BTCBW", String.format("[+] Update %d addresses", params.length));

        for (BitcoinAddress addr: params) {
            Log.d("BTCBW", String.format("[+] Update addr[%s]", addr.getFullAddress()));
            addr.updateValue();
            Log.d("BTCBW", String.format("[-] Update addr[%s] balance := %d", addr.getFullAddress(), addr.getBalance()));
        }

        return true;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        // NOP
    }

    @Override
    protected void onPostExecute(Boolean result) {
        listener.onTaskFinished();
    }

}
