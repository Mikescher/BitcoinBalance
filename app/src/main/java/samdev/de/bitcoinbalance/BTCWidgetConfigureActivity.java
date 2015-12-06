package samdev.de.bitcoinbalance;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import samdev.de.bitcoinbalance.async.TaskListener;
import samdev.de.bitcoinbalance.async.UpdateAddressBalanceTask;
import samdev.de.bitcoinbalance.btc.BTCUnit;
import samdev.de.bitcoinbalance.btc.BitcoinAddress;
import samdev.de.bitcoinbalance.btc.BitcoinHelper;
import samdev.de.bitcoinbalance.btc.BitcoinWallet;
import samdev.de.bitcoinbalance.helper.PreferencesHelper;

/**
 * The configuration screen for the {@link BTCWidget BTCWidget} AppWidget.
 */
public class BTCWidgetConfigureActivity extends Activity {
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
                    "1BitPoPevGTcnSGWqHGrFiVg6fVC7y9NVK",
                    "1BitcoinEaterAddressDontSendf59kuE",
                    "1CounterpartyXXXXXXXXXXXXXXXUWLpVr",
                    "1111111111111111111114oLvT2",
                    "1QLbz7JHiBTspS962RLKV8GndWFwi5j6Qr"
            };

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private RadioButton btnUnit0;
    private RadioButton btnUnit1;
    private RadioButton btnUnit2;
    private RadioButton btnUnit3;
    private Button btnFinish;

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

        ListView addressView = (ListView) findViewById(R.id.adressesList);

        btnUnit0    = (RadioButton) findViewById(R.id.rbUnit0);
        btnUnit1    = (RadioButton) findViewById(R.id.rbUnit1);
        btnUnit2    = (RadioButton) findViewById(R.id.rbUnit2);
        btnUnit3    = (RadioButton) findViewById(R.id.rbUnit3);
        btnFinish   = (Button) findViewById(R.id.btnAdd);

        addressAdapter = new AddressListAdapter(addresses, this);
        addressView.setAdapter(addressAdapter);

        addressAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                btnFinish.setEnabled(!addresses.isEmpty());
            }
        });

        findViewById(R.id.btnAddressAddManual).setOnClickListener(mOnAddAdressManual);
        findViewById(R.id.btnAddressAddManual).setOnLongClickListener(mOnAddAdressExample);
        findViewById(R.id.btnAddressAddManual).setOnTouchListener(mLongClickAddListener);
        findViewById(R.id.btnAddressAddQR).setOnClickListener(mOnAddAdressQR);
        findViewById(R.id.btnAdd).setOnClickListener(mOnFinish);
        findViewById(R.id.btnAbort).setOnClickListener(mOnAbort);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        addressAdapter.notifyDataSetChanged();
    }

    View.OnClickListener mOnAddAdressManual = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = BTCWidgetConfigureActivity.this;

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Bitcoin Address");

            final EditText input = new EditText(context);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

            input.setText("");
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

                    addressAdapter.notifyDataSetChanged();
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

    View.OnTouchListener mLongClickAddListener = new View.OnTouchListener() {
        private final static int LONG_PRESS_TIME = 3000; // 3 seconds long click for debug

        private boolean isLongPress = false;
        private long startTime = 0;
        private Handler handler = new Handler();
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                isLongPress = true;
                startTime = SystemClock.uptimeMillis();
                handler.removeCallbacks(Run);
                handler.postDelayed(Run, LONG_PRESS_TIME);

                return false;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                isLongPress = false;
                return SystemClock.uptimeMillis()-startTime >= LONG_PRESS_TIME;
            }
            return false;
        }

        private Runnable Run = new Runnable() {
            @Override
            public void run() {
                if (isLongPress) {
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(100);

                    AddNewAddress(EXAMPLES[new Random().nextInt(EXAMPLES.length)]);
                }
            }
        };
    };

    View.OnLongClickListener mOnAddAdressExample = new View.OnLongClickListener() {
        public boolean onLongClick(View v) {
            return false;
        }
    };

    private void AddNewAddress(String addr) {
        AddNewAddress(new BitcoinAddress(addr));
    }

    private void AddNewAddress(BitcoinAddress new_address) {
        Log.d("BTCBW", "Add address: " + new_address.getFullAddress());

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
            IntentIntegrator scanner = new IntentIntegrator(BTCWidgetConfigureActivity.this);

            scanner.setBeepEnabled(false);
            scanner.setDesiredBarcodeFormats(Collections.singletonList("QR_CODE"));
            scanner.setOrientationLocked(true);
            scanner.setPrompt("Scan yout bitcoin address QR code");
            scanner.setCaptureActivity(PortraitCaptureActivity.class);

            scanner.initiateScan();
        }
    };

    View.OnClickListener mOnFinish = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = BTCWidgetConfigureActivity.this;

            PreferencesHelper.savePrefWallet(context, mAppWidgetId, getWallet());

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            BTCWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId, false);

            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    View.OnClickListener mOnAbort = new View.OnClickListener() {
        public void onClick(View v) {
            setResult(RESULT_CANCELED);
            finish();
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (scanningResult == null) return;
        String content = scanningResult.getContents();
        if (content == null) return;

        List<BitcoinAddress> addrList = BitcoinAddress.parse(content);

        if (addrList != null) {
            for (BitcoinAddress addr: addrList) {
                AddNewAddress(addr);
            }
        } else {
            Toast.makeText(getApplicationContext(), "This is not a valid bitcoin address", Toast.LENGTH_LONG).show();
        }
    }

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

    //TODO Add update every x option
}

