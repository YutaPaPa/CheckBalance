package com.example.masayuki.checkbalance;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.content.Context;
import android.content.SharedPreferences;
import android.app.AlertDialog;
import android.content.res.Resources;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import android.content.DialogInterface;
import android.nfc.NfcAdapter;
import android.content.Intent;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextEdy;
    private EditText editTextSuica;
    private EditText editTextPasumo;
    private EditText editTextBunkadou;
    private Button btnEdy;
    private SharedPreferences CheckBalance;
    private EditText editTextDate;
    private String cardNameArray[] = new String[4];
    private int moneyArray[] = new int[4];
    //protected NfcAdapter mNfcAdapter;
    private static final String TAG = "NFCSample";
    private final byte[] polling_common_area_command = new byte[]{(byte)0x06, (byte)0x00, (byte) 0xFE, (byte) 0x00, (byte)0x00, (byte)0x0F};
    private byte[] request_service_edy_no_command = new byte[]{(byte)0x0d, (byte)0x02, (byte) 0x00, (byte) 0x00, (byte)0x00, (byte)0x00, (byte) 0x00, (byte) 0x00, (byte)0x00, (byte)0x00,
            (byte)0x01, (byte)0x0b, (byte)0x11};
    private byte[] read_wo_encryption_edy_command = new byte[]{(byte)0x22, (byte)0x06, (byte) 0x00, (byte) 0x00, (byte)0x00, (byte)0x00, (byte) 0x00, (byte) 0x00, (byte)0x00, (byte)0x00,
            (byte)0x03, (byte)0x0b, (byte)0x11, (byte)0x17, (byte)0x13, (byte)0x0f, (byte)0x17,
            (byte)0x08, (byte)0x80, (byte)0x00, (byte)0x81, (byte)0x00, (byte)0x82, (byte)0x00, (byte)0x82, (byte)0x01, (byte)0x82, (byte)0x02, (byte)0x82, (byte)0x03, (byte)0x82, (byte)0x04, (byte)0x82, (byte)0x05};
    private byte[] edyResult = new byte[16*8];
    private StringBuffer sb1 = new StringBuffer();
    private EditText editTextNFC;
    private String nfcTitle = "NFCの残額は";
    public static final String SYSTEMCODE_SUICA = "0003";       // Suica (=サイバネ領域) int型だと0x0003
    public static final String SYSTEMCODE_PASMO = "0003";       // Pasmo (=サイバネ領域) int型だと0x0003
    public static final int SYSTEMCODE_EDY = 0xfe00;         // Edy (=共通領域)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // コントロールのセット
        findViews();

        // 各ICカードごとの残額と登録日をセット
        Map<String, String> map = new HashMap<String, String>();
        map = load();

        if (map.containsKey("edy"))
        {
            editTextEdy.setText(map.get("edy"));
        }

        if (map.containsKey("suica"))
        {
            editTextSuica.setText(map.get("suica"));
        }

        if (map.containsKey("pasumo"))
        {
            editTextPasumo.setText(map.get("pasumo"));
        }

        if (map.containsKey("bunkadou"))
        {
            editTextBunkadou.setText(map.get("bunkadou"));
        }

        if (map.containsKey("date"))
        {
            editTextDate.setText(map.get("date"));
        }

        // 登録ボタンにClickイベント設定
        btnEdy.setOnClickListener(this);

        Intent intent = getIntent();
        String action = intent.getAction();

        // NFCかどうかActionの判定
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)
                ||  NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                ||  NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {

            byte[] IDm = new byte[]{0};
            Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                IDm = tag.getId();
            }

            //NfcF nfc = NfcF.get(tag);
            NfcF nfc = NfcF.get(tag);
            String systemCode = asHex(nfc.get(tag).getSystemCode());

            try {
                nfc.connect();

                if (systemCode.equals(this.SYSTEMCODE_SUICA) || systemCode.equals(this.SYSTEMCODE_PASMO))
                {
                    // Suica or Pasumo
                    Felica felica = new Felica();
                    byte[] req = felica.readWithoutEncryption(IDm, 10);
                    //Log.d(TAG, "req:" + toHex(req));
                    // カードにリクエスト送信
                    byte[] res = nfc.transceive(req);
                    //Log.d(TAG, "res:" + toHex(res));
                    nfc.close();
                    // 結果を文字列に変換して表示
                    //textView1.setText(parse(res));
                    //Log.d("res", parse(res));
                    editTextNFC.setText(this.nfcTitle + String.format("%1$,3d円", felica.parse(res)) + "です。");
                }
                else
                {
                    // Edy
                    byte[] idm =  new byte[8];
                    byte[] responce = null;

                    responce = nfc.transceive(polling_common_area_command);
                    if (responce != null) {
                        for (int i = 0; i < 8; i++) {
                            idm[i] = responce[i + 2];
                        }
                    }

                    byte[] responce3 = null;
                    for (int i = 0; i < 8; i++) {
                        read_wo_encryption_edy_command[2 + i] = idm[i];
                    }
                    responce3 = nfc.transceive(read_wo_encryption_edy_command);

                    for (int i = 0; i < 16 * 8; i++) {
                        edyResult[i] = responce3[13 + i];
                    }

                    int edyZandaka = dispEdyResult(edyResult);
                    editTextNFC.setText(this.nfcTitle + String.format("%1$,3d円", edyZandaka) + "です。");
                }
                // -----------------------------------------------------------End
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
                //textView1.setText(e.toString());
            }
        }
    }

    /**
     * 画面コントロールを設定
     */
    protected void findViews(){
        // 登録日を入力不可設定
        editTextDate = (EditText)findViewById(R.id.editTextDate);
        editTextDate.setKeyListener(null);

        // Edy
        editTextEdy = (EditText)findViewById(R.id.editTextEdy);
        editTextEdy.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // EditTextのフォーカスが外れた場合
                if (hasFocus == false) {
                    // ソフトキーボードを非表示にする
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });

        // Suica
        editTextSuica = (EditText)findViewById(R.id.editTextSuica);
        editTextSuica.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // EditTextのフォーカスが外れた場合
                if (hasFocus == false) {
                    // ソフトキーボードを非表示にする
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });

        // Pasumo
        editTextPasumo = (EditText)findViewById(R.id.editTextPasumo);
        editTextPasumo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // EditTextのフォーカスが外れた場合
                if (hasFocus == false) {
                    // ソフトキーボードを非表示にする
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });

        // Bunkadou
        editTextBunkadou = (EditText)findViewById(R.id.editTextBunkadou);
        editTextBunkadou.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // EditTextのフォーカスが外れた場合
                if (hasFocus == false) {
                    // ソフトキーボードを非表示にする
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });

        btnEdy = (Button)findViewById(R.id.button);
        editTextDate = (EditText)findViewById(R.id.editTextDate);
        editTextDate.setWidth(710);

        // NFC読み込み時の残額表示領域を入力不可設定
        editTextNFC = (EditText)findViewById(R.id.editTextNFC);
        editTextNFC.setKeyListener(null);
        editTextNFC.setWidth(710);
    }

    /**
     * Click時処理の設定
     * @param v View
     */
    @Override
    public void onClick(View v) {
        // ボタンにフォーカスを移動させる
        btnEdy.setFocusable(true);
        btnEdy.setFocusableInTouchMode(true);
        btnEdy.requestFocus();

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(editTextEdy, InputMethodManager.SHOW_FORCED);
        inputMethodManager.showSoftInput(editTextSuica, InputMethodManager.SHOW_FORCED);
        inputMethodManager.showSoftInput(editTextPasumo, InputMethodManager.SHOW_FORCED);
        inputMethodManager.showSoftInput(editTextBunkadou, InputMethodManager.SHOW_FORCED);

        moneyArray[0] = Integer.parseInt(editTextEdy.getText().toString());
        moneyArray[1] = Integer.parseInt(editTextSuica.getText().toString());
        moneyArray[2] = Integer.parseInt(editTextPasumo.getText().toString());
        moneyArray[3] = Integer.parseInt(editTextBunkadou.getText().toString());

        String cardNameArray[] = {"edy", "suica", "pasumo", "bunkadou", "date"};

        //this.saveEdy(edy);
        save(cardNameArray, moneyArray);
    }

    /**
     * Preferences にパーソナルデータを保存する
     * @param cardNameArray カード名
     * @param moneyArray 残額
     */
    private void save(String[] cardNameArray, int[] moneyArray){
        // アプリ標準の Preferences を取得する
        SharedPreferences dataStore = getSharedPreferences("CheckBalance", MODE_PRIVATE);
        // Preferences に書き込むための Editor クラスを取得する
        SharedPreferences.Editor editor = dataStore.edit();

        // Edy残額
        editor.putInt(cardNameArray[0], moneyArray[0]);
        // Suica残額
        editor.putInt(cardNameArray[1], moneyArray[1]);
        // Pasumo残額
        editor.putInt(cardNameArray[2], moneyArray[2]);
        // 文化堂残額
        editor.putInt(cardNameArray[3], moneyArray[3]);

        // 登録日のセット
        Date date1 = new Date();
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy'年'MM'月'dd'日'");
        editor.putString(cardNameArray[4], "登録日：" + sdf1.format(date1));

        // 書き込みを確定する
        editor.commit();

        // 登録完了メッセージ表示
        finishMessage();
    }

    /**
     * Preferences から各ICカードごとの残額を読み出す
     */
    private Map<String, String> load(){
        /*Log.d("ここまできている", "1");
        // アプリ標準の Preferences を取得する
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Log.d("ここまできている", "2");*/

        // "DataStore"という名前でインスタンスを生成
        CheckBalance = getSharedPreferences("CheckBalance", MODE_PRIVATE);
        final String edy = String.valueOf(CheckBalance.getInt("edy", 0));
        final String suica = String.valueOf(CheckBalance.getInt("suica", 0));
        final String pasumo = String.valueOf(CheckBalance.getInt("pasumo", 0));
        final String bunkadou = String.valueOf(CheckBalance.getInt("bunkadou", 0));
        final String date = CheckBalance.getString("date", "");

        Map<String, String> map = new HashMap<String, String>() {
            {
                put("edy", edy);
                put("suica", suica);
                put("pasumo", pasumo);
                put("bunkadou", bunkadou);
                put("date", date);
            }
        };

        return map;
    }

    /**
     * 正常登録時のメッセージを表示する
     */
    private void finishMessage()
    {
        Resources res = getResources();

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(res.getString(R.string.finish_message_title).toString());
        alert.setMessage(res.getString(R.string.finish_message).toString());
        alert.setIcon(android.R.drawable.ic_dialog_info);
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // ボタンをクリックしたときの動作(何もしない)
            }
        });
        alert.show();
    }

    /**
     * NFCからEdyの残額を読み出す処理
     * @param EdyResult NFCから読み取ったEdy情報(バイト配列)
     */
    public int dispEdyResult(byte[] EdyResult) {
        ByteBuffer bf = ByteBuffer.allocate(4);
        bf.put(EdyResult[19]);
        bf.put(EdyResult[18]);
        bf.put(EdyResult[17]);
        bf.put(EdyResult[16]);

        int zandaka = 0;
        int size = bf.getInt(0);
        zandaka = size;

        return zandaka;
    }

    /**
     * バイト配列を16進数の文字列に変換する。
     *
     * @param bytes バイト配列
     * @return 16進数の文字列
     */
    public static String asHex(byte bytes[]) {
        // バイト配列の２倍の長さの文字列バッファを生成。
        StringBuffer strbuf = new StringBuffer(bytes.length * 2);

        // バイト配列の要素数分、処理を繰り返す。
        for (int index = 0; index < bytes.length; index++) {
            // バイト値を自然数に変換。
            int bt = bytes[index] & 0xff;

            // バイト値が0x10以下か判定。
            if (bt < 0x10) {
                // 0x10以下の場合、文字列バッファに0を追加。
                strbuf.append("0");
            }

            // バイト値を16進数の文字列に変換して、文字列バッファに追加。
            strbuf.append(Integer.toHexString(bt));
        }

        /// 16進数の文字列を返す。
        return strbuf.toString();
    }
}
