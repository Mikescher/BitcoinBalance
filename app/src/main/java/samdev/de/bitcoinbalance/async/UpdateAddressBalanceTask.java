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

        for (BitcoinAddress addr: params) {
            boolean result = addr.UpdateValue();

            Log.d("BTCBW", String.format("Updated addr value:%d = %s", addr.balance, result));
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
