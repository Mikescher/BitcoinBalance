package samdev.de.bitcoinbalance;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RemoteViews;

import samdev.de.bitcoinbalance.async.TaskListener;
import samdev.de.bitcoinbalance.async.UpdateAddressBalanceTask;
import samdev.de.bitcoinbalance.async.UpdateWalletBalanceTask;
import samdev.de.bitcoinbalance.btc.BTCUnit;
import samdev.de.bitcoinbalance.btc.BitcoinWallet;
import samdev.de.bitcoinbalance.helper.PerferencesHelper;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link BTCWidgetConfigureActivity BTCWidgetConfigureActivity}
 */
public class BTCWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            PerferencesHelper.deletePrefWallet(context, appWidgetIds[i]);
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
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.btcwidget);
        BitcoinWallet wallet = PerferencesHelper.loadPrefWallet(context, appWidgetId);

        updateDisplay(wallet, context, appWidgetId, views);
        initClickListener(context, appWidgetId, views);
        updateBalance(wallet, context, appWidgetId, views);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @NonNull
    private static RemoteViews initClickListener(Context context, int appWidgetId, RemoteViews views) {
        //TODO nicht exen wenn auch der btc button geklickt ist - vllt am besten nur wenn wirklich text erwisch twurde
        Intent configIntent = new Intent(context, ShowAddressActivity.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, appWidgetId, configIntent, 0);
        views.setOnClickPendingIntent(R.id.appwidget_btcvalue, configPendingIntent);
        configIntent.setAction(ShowAddressActivity.class.toString() + Integer.toString(appWidgetId));

        return views;
    }

    private static void updateDisplay(BitcoinWallet wallet, Context context, int appWidgetId, RemoteViews views) {

        views.setTextViewText(R.id.appwidget_btcvalue, wallet.getFormattedBalance());
        views.setImageViewResource(R.id.appwidget_btcicon, wallet.getUnitIconResource());
        views.setTextViewText(R.id.appwidget_state, wallet.getStateText());
    }

    private static void updateBalance(final BitcoinWallet wallet, final Context context, final int appWidgetId, final RemoteViews views) {
        new UpdateWalletBalanceTask(new TaskListener() {
            @Override
            public void onTaskStarted() {
                Log.d("BTCBW", "Update Wallet started");
            }

            @Override
            public void onTaskFinished() {
                Log.d("BTCBW", "Update Wallet finished");
                updateDisplay(wallet, context, appWidgetId, views);
            }
        }).execute(wallet);
    }
}

