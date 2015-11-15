package samdev.de.bitcoinbalance.async;

import android.os.AsyncTask;
import android.util.Log;

import samdev.de.bitcoinbalance.btc.BitcoinWallet;

public class UpdateWalletBalanceTask extends AsyncTask<BitcoinWallet, Integer, Boolean> {
    private final TaskListener listener;

    private final long minimumExecutionTime;

    public UpdateWalletBalanceTask(TaskListener _listener, long minMillis) {
        listener = _listener;
        minimumExecutionTime = minMillis;
    }

    @Override
    protected void onPreExecute() {
        listener.onTaskStarted();
    }

    @Override
    protected Boolean doInBackground(BitcoinWallet... params) {
        Log.d("BTCBW", String.format("[+] Update %d wallets", params.length));

        final long start = System.currentTimeMillis();

        for (BitcoinWallet wallet: params) {
            Log.d("BTCBW", String.format("[+] START Update wallets[%s] (%d adresses)", wallet.hashCode(), wallet.getAddressCount()));
            wallet.updateValue();
            Log.d("BTCBW", String.format("[-] END   Update wallets[%s] balance := %d", wallet.hashCode(), wallet.getBalance()));
        }

        while (System.currentTimeMillis() - start < minimumExecutionTime) {
            try { Thread.sleep(100); } catch (InterruptedException e) { /**/ }
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
