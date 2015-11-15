package samdev.de.bitcoinbalance;

import android.appwidget.AppWidgetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.QRCode;

import java.util.HashMap;
import java.util.Map;

import samdev.de.bitcoinbalance.btc.BitcoinAddress;
import samdev.de.bitcoinbalance.btc.BitcoinWallet;
import samdev.de.bitcoinbalance.helper.PreferencesHelper;
import samdev.de.bitcoinbalance.views.QRCodeView;

public class ShowAddressActivity extends AppCompatActivity {

    private BitcoinWallet wallet;

    private QRCodeView qrView;
    private ListView addressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_address);

        wallet = PreferencesHelper.loadPrefWallet(this, getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1));
        qrView = (QRCodeView)findViewById(R.id.showaddr_qrview);
        addressView = (ListView) findViewById(R.id.showaddr_addresslist);

        addressView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        addressView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setQRCode(wallet.getAddress(position));
            }
        });
        addressView.setAdapter(new AddressListBigAdapter(wallet.getAddresses(), this));

        if (wallet.hasMainAddress()) setQRCode(wallet.getMainAddress());
    }

    private void setQRCode(BitcoinAddress addr) {

        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            BitMatrix qr = writer.encode(addr.getBIP21Address(), BarcodeFormat.QR_CODE, QRCodeView.BTC_QR_SIZE, QRCodeView.BTC_QR_SIZE, hints);

            qrView.setQR(qr);
        } catch (WriterException e) {
            Log.e("BTCBW", "Can't create QR Code: " + e.toString());
            e.printStackTrace();
        }
    }

    //TODO edit  button (back to configure)
}