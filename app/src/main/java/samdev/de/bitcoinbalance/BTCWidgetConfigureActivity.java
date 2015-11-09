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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * The configuration screen for the {@link BTCWidget BTCWidget} AppWidget.
 */
public class BTCWidgetConfigureActivity extends Activity {

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private static final String PREFS_NAME = "samdev.de.bitcoinbalance.BTCWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";

    private RadioButton btnUnit0;
    private RadioButton btnUnit1;
    private RadioButton btnUnit2;
    private RadioButton btnUnit3;
    private Button btnFinish;
    private ListView addressView;

    private AddressListAdapter addressAdapter;
    private ArrayList<String> addresses = new ArrayList<>();

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
            builder.setTitle("Title");

            final EditText input = new EditText(context);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            builder.setView(input);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String addr = input.getText().toString();
                    if (BitcoinHelper.isBitcoinAdress(addr))  {
                        addresses.add(addr);
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

    View.OnClickListener mOnAddAdressQR = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = BTCWidgetConfigureActivity.this;

            //TODO
        }
    };

    View.OnClickListener mOnFinish = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = BTCWidgetConfigureActivity.this;

            // When the button is clicked, store the string locally
            savePref(context, mAppWidgetId, "unit", getSelectedUnit());
            savePref(context, mAppWidgetId, "addresses", getAddressList());

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            BTCWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
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

    private String getSelectedUnit() {
        if (btnUnit0.isChecked()) return "0";
        if (btnUnit1.isChecked()) return "1";
        if (btnUnit2.isChecked()) return "2";
        if (btnUnit3.isChecked()) return "3";

        return "-1";
    }

    private String getAddressList() {
        String result = "";
        for (int i = 0; i < addresses.size(); i++) {
            if (i > 0) result += "|";
            result += addresses.get(i);
        }
        return result;
    }

    private void updateUI() {
        btnFinish.setEnabled(! addresses.isEmpty());

        addressAdapter.notifyDataSetChanged();
    }

    static void savePref(Context context, int appWidgetId, String name, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId + "_" + name, text);
        prefs.commit();
    }

    static String loadPref(Context context, int appWidgetId, String name) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String value = prefs.getString(PREF_PREFIX_KEY + appWidgetId + "_" + name, null);

        return value;
    }

    static void deletePref(Context context, int appWidgetId, String name) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId + "_" + name);
        prefs.commit();
    }
}

