package samdev.de.bitcoinbalance;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import samdev.de.bitcoinbalance.async.TaskListener;
import samdev.de.bitcoinbalance.async.UpdateAddressBalanceTask;
import samdev.de.bitcoinbalance.btc.BTCUnit;
import samdev.de.bitcoinbalance.btc.BitcoinAddress;
import samdev.de.bitcoinbalance.btc.BitcoinHelper;
import samdev.de.bitcoinbalance.btc.BitcoinWallet;
import samdev.de.bitcoinbalance.helper.PerferencesHelper;

/**
 * The configuration screen for the {@link BTCWidget BTCWidget} AppWidget.
 */
public class BTCWidgetConfigureActivity extends Activity {
    //TODO REMOVE ME ARGH
    String [] EXAMPLES = new String[]
            {
                    "1FmvtS66LFh6ycrXDwKRQTexGJw4UWiqDX",
                    "1JEiV9CiJmhfYhE7MzeSdmH82xRYrbYrtb",
                    "1HceWtheh1yfeCN85GfXG84hYJjDz1JPsQ",
                    "1NB1KFnFqnP3WSDZQrWV3pfmph5fWRyadz",
                    "1LipeR1AjHL6gwE7WQECW4a2H4tuqm768N",
                    "19QkqAza7BHFTuoz9N8UQkryP4E9jHo4N3",
                    "1Kz25jm6pjNTaz8bFezEYUeBYfEtpjuKRG",
                    "15KXVQv7UGtUoTe5VNWXT1bMz46MXuePba",
                    "1VESCU4YLvNYhmTsJRgFKKn3bLFeeWtJm",
                    "1BitPoPevGTcnSGWqHGrFiVg6fVC7y9NVK"
            };

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private RadioButton btnUnit0;
    private RadioButton btnUnit1;
    private RadioButton btnUnit2;
    private RadioButton btnUnit3;
    private Button btnFinish;
    private ListView addressView;

    private AddressListAdapter addressAdapter;
    private ArrayList<BitcoinAddress> addresses = new ArrayList<>();

    public BTCWidgetConfigureActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setResult(RESULT_CANCELED);

        setContentView(R.layout.btcwidget_configure);

        btnUnit0    = (RadioButton) findViewById(R.id.rbUnit0);
        btnUnit1    = (RadioButton) findViewById(R.id.rbUnit1);
        btnUnit2    = (RadioButton) findViewById(R.id.rbUnit2);
        btnUnit3    = (RadioButton) findViewById(R.id.rbUnit3);
        btnFinish   = (Button) findViewById(R.id.btnAdd);
        addressView = (ListView) findViewById(R.id.adressesList);

        addressAdapter = new AddressListAdapter(addresses, this);
        addressView.setAdapter(addressAdapter);

        findViewById(R.id.btnAddressAddManual).setOnClickListener(mOnAddAdressManual);
        findViewById(R.id.btnAddressAddQR).setOnClickListener(mOnAddAdressQR);
        findViewById(R.id.btnAdd).setOnClickListener(mOnFinish);
        findViewById(R.id.btnAbort).setOnClickListener(mOnAbort);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        updateUI();
    }

    View.OnClickListener mOnAddAdressManual = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = BTCWidgetConfigureActivity.this;

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Bitcoin Address");

            final EditText input = new EditText(context);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

            input.setText(EXAMPLES[new Random().nextInt(EXAMPLES.length)]); //TODO 4 debug only --rem--
            builder.setView(input);

            builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String addr = input.getText().toString();
                    if (BitcoinHelper.ValidateBitcoinAddress(addr))  {
                        AddNewAddress(addr);
                    } else {
                        Toast.makeText(context, "Invalid Bitcoin adress" , Toast.LENGTH_LONG).show();
                    }
                    updateUI();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }
    };

    private void AddNewAddress(String addr) {
        Log.d("BTCBW", "Add address: " + addr);

        BitcoinAddress new_address = new BitcoinAddress(addr);
        addresses.add(new_address);

        new UpdateAddressBalanceTask(new TaskListener() {
            @Override
            public void onTaskStarted() {
                Log.d("BTCBW", "Update Addr Task started");
            }

            @Override
            public void onTaskFinished() {
                Log.d("BTCBW", "Update Addr Task finished");
                addressAdapter.notifyDataSetChanged();
            }
        }).execute(new_address);
    }

    View.OnClickListener mOnAddAdressQR = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = BTCWidgetConfigureActivity.this;

            //TODO qr reader
        }
    };

    View.OnClickListener mOnFinish = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = BTCWidgetConfigureActivity.this;

            // When the button is clicked, store the string locally
            PerferencesHelper.savePrefWallet(context, mAppWidgetId, getWallet());

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            BTCWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    View.OnClickListener mOnAbort = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = BTCWidgetConfigureActivity.this;

            setResult(RESULT_CANCELED);
            finish();
        }
    };

    private BTCUnit getSelectedUnit() {
        if (btnUnit0.isChecked()) return BTCUnit.BTC;
        if (btnUnit1.isChecked()) return BTCUnit.MBTC;
        if (btnUnit2.isChecked()) return BTCUnit.BITS;
        if (btnUnit3.isChecked()) return BTCUnit.SATOSHI;

        return BTCUnit.BTC;
    }

    private BitcoinWallet getWallet() {
        BitcoinWallet wallet = new BitcoinWallet(getSelectedUnit());

        for (BitcoinAddress addr: addresses) {
            wallet.addAddress(addr);
        }

        return wallet;
    }

    private void updateUI() {
        btnFinish.setEnabled(!addresses.isEmpty());

        addressAdapter.notifyDataSetChanged();
    }

    //TODO Add update every x option
}

