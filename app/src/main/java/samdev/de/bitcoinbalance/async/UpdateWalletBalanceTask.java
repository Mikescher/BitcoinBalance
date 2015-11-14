package samdev.de.bitcoinbalance.async;

import android.os.AsyncTask;
import android.util.Log;

import samdev.de.bitcoinbalance.btc.BitcoinWallet;

public class UpdateWalletBalanceTask extends AsyncTask<BitcoinWallet, Integer, Boolean> {
    private final TaskListener listener;

    public UpdateWalletBalanceTask(TaskListener _listener) {
        listener = _listener;
    }

    @Override
    protected void onPreExecute() {
        listener.onTaskStarted();
    }

    @Override
    protected Boolean doInBackground(BitcoinWallet... params) {

        Log.d("BTCBW", String.format("[+] Update %d wallets", params.length));

        for (BitcoinWallet wallet: params) {
            Log.d("BTCBW", String.format("[+] Update wallets[%s] (%d adresses)", wallet.hashCode(), wallet.getAddressCount()));
            wallet.updateValue();
            Log.d("BTCBW", String.format("[-] Update wallets[%s] balance := %d", wallet.hashCode(), wallet.getBalance()));
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
