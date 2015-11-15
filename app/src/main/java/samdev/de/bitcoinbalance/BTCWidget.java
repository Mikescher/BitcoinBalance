package samdev.de.bitcoinbalance;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;


import samdev.de.bitcoinbalance.async.TaskListener;
import samdev.de.bitcoinbalance.async.UpdateWalletBalanceTask;
import samdev.de.bitcoinbalance.btc.BitcoinWallet;
import samdev.de.bitcoinbalance.helper.PreferencesHelper;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link BTCWidgetConfigureActivity BTCWidgetConfigureActivity}
 */
public class BTCWidget extends AppWidgetProvider {
    private final static String ACTION_UPDATE = "BTCBW_UPDATE_";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            PreferencesHelper.deletePrefWallet(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // NOP
    }

    @Override
    public void onDisabled(Context context) {
        // NOP
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Log.d("BTCBW", String.format("updateAppWidget(%d)", appWidgetId));

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.btcwidget);
        BitcoinWallet wallet = PreferencesHelper.loadPrefWallet(context, appWidgetId);

        updateDisplay(wallet, views, false);
        initClickListener(context, appWidgetId, views);
        updateBalance(wallet, context, appWidgetId, views);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static RemoteViews initClickListener(Context context, int appWidgetId, RemoteViews views) {
        Log.d("BTCBW", String.format("initClickListener(%d)", appWidgetId));

        // Balance Textbox

        Intent showAddressIntent = new Intent(context, ShowAddressActivity.class);
        showAddressIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent showAddressPendingIntent = PendingIntent.getActivity(context, appWidgetId, showAddressIntent, 0);
        views.setOnClickPendingIntent(R.id.appwidget_btcvalue, showAddressPendingIntent);
        showAddressIntent.setAction(ShowAddressActivity.class.toString() + Integer.toString(appWidgetId));

        // BTC Icon

        views.setOnClickPendingIntent(R.id.appwidget_btcicon, getPendingSelfIntent(context, appWidgetId, ACTION_UPDATE + appWidgetId));

        return views;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.getAction().startsWith(ACTION_UPDATE)) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.btcwidget);
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);

            Log.d("BTCBW", String.format("onRecieve(ACTION_UPDATE, %d)", appWidgetId));

            BitcoinWallet wallet = PreferencesHelper.loadPrefWallet(context, appWidgetId);

            updateBalance(wallet, context, appWidgetId, views);
        }
    }

    // Needs appWidgetManager.updateAppWidget after call
    private static void updateDisplay(BitcoinWallet wallet, RemoteViews views, boolean updatePending) {
        if (updatePending) {
            views.setTextViewText(R.id.appwidget_btcvalue, "...");
            views.setTextViewText(R.id.appwidget_state, "updating");
        } else {
            views.setTextViewText(R.id.appwidget_btcvalue, wallet.getFormattedBalance());
            views.setTextViewText(R.id.appwidget_state, wallet.getStateText());
        }

        views.setImageViewResource(R.id.appwidget_btcicon, wallet.getUnit().getUnitIconResource());
    }

    private static void updateBalance(final BitcoinWallet wallet, final Context context, final int appWidgetId, final RemoteViews views) {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        updateDisplay(wallet, views, true);
        appWidgetManager.updateAppWidget(appWidgetId, views);

        new UpdateWalletBalanceTask(new TaskListener() {
            @Override
            public void onTaskStarted() {
                Log.d("BTCBW", "Update Wallet started");
            }

            @Override
            public void onTaskFinished() {
                Log.d("BTCBW", "Update Wallet finished");

                PreferencesHelper.savePrefWallet(context, appWidgetId, wallet);

                updateDisplay(wallet, views, false);
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        }, 650).execute(wallet);
    }

    private static PendingIntent getPendingSelfIntent(Context context, int appWidgetId, String action) {
        Intent intent = new Intent(context, BTCWidget.class);
        intent.setAction(action);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }
}

