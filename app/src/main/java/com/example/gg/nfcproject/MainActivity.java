package com.example.gg.nfcproject;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.gg.nfcproject.util.LogUtil;
import com.example.gg.nfcproject.util.NfcBankCardReader;

public class MainActivity extends AppCompatActivity {
    private NfcAdapter mNfcAdapter;
    private static final String TAG = "MainActivity";

    // 检查是否支持nfc，如果支持，确保nfc打开
    private void nfcCheck() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "本机不支持NFC", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (!mNfcAdapter.isEnabled()) {
                Intent setNfc = new Intent(Settings.ACTION_NFC_SETTINGS);
                startActivity(setNfc);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nfcCheck();

        Intent intent = getIntent();
        if (intent != null) {
            final Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                IsoDep isoDep = IsoDep.get(tag);
                if (isoDep != null) {
                    new NfcTask(this).execute(isoDep);
                }
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        LogUtil.i(TAG, intent.getAction());

        final Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) {
            return;
        }
        // LogUtil.i(TAG, "tag.getId(): " + bytesToString(tag.getId()));

        String[] strings = tag.getTechList();
        for (String s : strings) {
            LogUtil.i(TAG, s);
        }
        // getBankCard
//        NfcBankCardReader reader = new NfcBankCardReader(tag);
//        String cardNumber = reader.getCardNumber();
//        if (cardNumber != null) {
//            LogUtil.d(TAG, "银行卡号: " + cardNumber);
//            Toast.makeText(MainActivity.this, "card: " + cardNumber, Toast.LENGTH_SHORT).show();
//        } else {
//            LogUtil.d(TAG, "读取失败");
//        }

        //test
        IsoDep isoDep = IsoDep.get(tag);
        if (isoDep != null) {
            new NfcTask(this).execute(isoDep);
        }

        if (NfcA.get(tag) != null) {
            LogUtil.i(TAG, "nfca != null");
        }

        if (NfcB.get(tag) != null) {
            LogUtil.i(TAG, "nfcb != null");
        }

        if (NfcV.get(tag) != null) {
            LogUtil.i(TAG, "nfcv != null");
        }

        if (NfcF.get(tag) != null) {
            LogUtil.i(TAG, "nfcf != null");
        }

        if (Ndef.get(tag) != null) {
            LogUtil.i(TAG, "ndef != null");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        PendingIntent pendingIntent;
        String[][] techList;

        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // 只针对ACTION_TECH_DISCOVERED
        techList = new String[][] {
                {IsoDep.class.getName()}, {NfcA.class.getName()}, {NfcB.class.getName()},
                {NfcV.class.getName()}, {NfcF.class.getName()}, {Ndef.class.getName()}};
        for (String[] strings : techList) {
            LogUtil.i(TAG, strings[0]);
        }
        mNfcAdapter.enableForegroundDispatch(this, pendingIntent, null, techList);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNfcAdapter.disableForegroundDispatch(this);
    }
}
