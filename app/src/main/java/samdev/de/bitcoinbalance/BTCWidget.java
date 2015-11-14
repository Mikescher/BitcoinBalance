package samdev.de.bitcoinbalance;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

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
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            BTCWidgetConfigureActivity.deletePrefWallet(context, appWidgetIds[i]);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        BitcoinWallet wallet = BitcoinWallet.deserialize(PerferencesHelper.loadPrefWallet(context, appWidgetId));

        if (wallet == null) return;

        //CharSequence widgetText = BTCWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        //// Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.btcwidget);

        views.setTextViewText(R.id.appwidget_btcvalue, wallet.getFormattedBalance());
        views.setImageViewBitmap(R.id.appwidget_btcicon, wallet.getDrawableUnitIcon());

        //TODO nicht exen wenn auch der btc button geklickt ist - vllt am besten nur wenn wirklich text erwisch twurde
        Intent configIntent = new Intent(context, ShowAddressActivity.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, appWidgetId, configIntent, 0);
        views.setOnClickPendingIntent(R.id.appwidget_btcvalue, configPendingIntent);
        configIntent.setAction(ShowAddressActivity.class.toString() + Integer.toString(appWidgetId));

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}

