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
import java.text.NumberFormat;
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Calendar;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // コントロールのセット
        findViews();

        // 登録日のセット
        editTextDate.setKeyListener(null);  // 入力不可設定
        Date date1 = new Date();
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy'年'MM'月'dd'日'");
        editTextDate.setText("登録日付：" + sdf1.format(date1));

        // Edy残額のセット
/*        int edyMoney = 0;
        edyMoney = loadEdy();*/

        // 各ICカードごとの残額をセット
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

        // 登録ボタンにClickイベント設定
        btnEdy.setOnClickListener(this);

        Intent intent = getIntent();
        String action = intent.getAction();

        // NFCかどうかActionの判定
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)
                ||  NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                ||  NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {

            // IDm(固有識別子)を表示させる
            /*String idm = getIdm(getIntent());
            if (idm != null) {
                //TextView idmView = (TextView) findViewById(R.id.idm);
                //idmView.setText(idm);
                Log.d("カード情報", idm);
            }*/

            byte[] IDm = new byte[]{0};
            Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                IDm = tag.getId();
            }

            //if (IDm[0] == "1") {
                NfcF nfc = NfcF.get(tag);
                try {
                    nfc.connect();

                    // edy用処理 -------------------------------------------------Start
                    byte[] idm =  new byte[8];
                    byte[] responce = null;

                    responce = nfc.transceive(polling_common_area_command);
                    for(int i = 0; i < 8; i++) {
                        idm[i] = responce[i+2];
                    }

                    byte[] responce2 = null;
                    for(int i = 0; i < 8; i++) {
                        request_service_edy_no_command[2+i] = idm[i];
                    }
                    responce2 = nfc.transceive(request_service_edy_no_command);

                    if((responce2[11] == (byte)0xFF) && (responce2[12] == (byte)0xFF))
                    {
                        // Suica or Pasumo
                        byte[] req = readWithoutEncryption(IDm, 10);
                        //Log.d(TAG, "req:" + toHex(req));
                        // カードにリクエスト送信
                        byte[] res = nfc.transceive(req);
                        Log.d(TAG, "res:" + toHex(res));
                        nfc.close();
                        // 結果を文字列に変換して表示
                        //textView1.setText(parse(res));
                        //Log.d("res", parse(res));
                        editTextPasumo.setText(String.valueOf(parse(res)));
                    }
                    else
                    {
                        // Edy
                        byte[] responce3 = null;
                        for (int i = 0; i < 8; i++) {
                            read_wo_encryption_edy_command[2 + i] = idm[i];
                        }
                        responce3 = nfc.transceive(read_wo_encryption_edy_command);

                        for (int i = 0; i < 16 * 8; i++) {
                            edyResult[i] = responce3[13 + i];
                        }

                        int edyZandaka = dispEdyResult(edyResult);
                        editTextEdy.setText(String.valueOf(edyZandaka));
                    }
                    // -----------------------------------------------------------End
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                    //textView1.setText(e.toString());
                }
            //}
        }
    }

    /**
     * 履歴読み込みFelicaコマンドの取得。
     * - Sonyの「Felicaユーザマニュアル抜粋」の仕様から。
     * - サービスコードは http://sourceforge.jp/projects/felicalib/wiki/suica の情報から
     * - 取得できる履歴数の上限は「製品により異なります」。
     * @param idm カードのID
     * @param size 取得する履歴の数
     * @return Felicaコマンド
     * @throws IOException
     */
    private byte[] readWithoutEncryption(byte[] idm, int size)
            throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(100);

        bout.write(0);           // データ長バイトのダミー
        bout.write(0x06);        // Felicaコマンド「Read Without Encryption」
        bout.write(idm);         // カードID 8byte
        bout.write(1);           // サービスコードリストの長さ(以下２バイトがこの数分繰り返す)
        bout.write(0x0f);        // 履歴のサービスコード下位バイト
        bout.write(0x09);        // 履歴のサービスコード上位バイト
        bout.write(size);        // ブロック数
        for (int i = 0; i < size; i++) {
            bout.write(0x80);    // ブロックエレメント上位バイト 「Felicaユーザマニュアル抜粋」の4.3項参照
            bout.write(i);       // ブロック番号
        }

        byte[] msg = bout.toByteArray();
        msg[0] = (byte) msg.length; // 先頭１バイトはデータ長
        return msg;
    }

    /**
     * 履歴Felica応答の解析。
     * @param res Felica応答
     * @return 文字列表現
     * @throws Exception
     */
    private int parse(byte[] res) throws Exception {
        // res[0] = データ長
        // res[1] = 0x07
        // res[2〜9] = カードID
        // res[10,11] = エラーコード。0=正常。
        if (res[10] != 0x00) throw new RuntimeException("Felica error.");

        // res[12] = 応答ブロック数
        // res[13+n*16] = 履歴データ。16byte/ブロックの繰り返し。
        //int size = res[12];
        int size = res[2];
        String str = "";
        int zandaka = 0;
        for (int i = 0; i < size; i++) {
            // 個々の履歴の解析。
            Rireki rireki = Rireki.parse(res, 13 + i * 16);
            zandaka += rireki.remain;
        }
        return zandaka;
    }

    private String toHex(byte[] id) {
        StringBuilder sbuf = new StringBuilder();
        for (int i = 0; i < id.length; i++) {
            String hex = "0" + Integer.toString((int) id[i] & 0x0ff, 16);
            if (hex.length() > 2)
                hex = hex.substring(1, 3);
            sbuf.append(" " + i + ":" + hex);
        }
        return sbuf.toString();
    }


    /**
     * IDmを取得する
     * @param intent
     * @return
     */
    private String getIdm(Intent intent) {
        String idm = null;
        StringBuffer idmByte = new StringBuffer();
        byte[] rawIdm = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
        if (rawIdm != null) {
            for (int i = 0; i < rawIdm.length; i++) {
                idmByte.append(Integer.toHexString(rawIdm[i] & 0xff));
            }
            idm = idmByte.toString();
        }
        return idm;
    }

    protected void findViews(){
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
    }

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

        String cardNameArray[] = {"edy", "suica", "pasumo", "bunkadou"};

        //this.saveEdy(edy);
        save(cardNameArray, moneyArray);
    }

    /**
     * Preferences にパーソナルデータを保存する
     * @param money Edyの残額
     */
    private void saveEdy(int money){
        // アプリ標準の Preferences を取得する
        SharedPreferences dataStore = getSharedPreferences("CheckBalance", MODE_PRIVATE);
        // Preferences に書き込むための Editor クラスを取得する
        SharedPreferences.Editor editor = dataStore.edit();
        // putXxxx("キー",データ) にて書き込むデータを登録する
        editor.putInt("edy", money);

        // 書き込みを確定する
        editor.commit();

        // 登録完了メッセージ表示
        finishMessage();
    }

    /**
     * Preferences からEdy残額を読み出す
     * @return Edyの残額(なければ 0 を返す)
     */
    private int loadEdy(){
        /*Log.d("ここまできている", "1");
        // アプリ標準の Preferences を取得する
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Log.d("ここまできている", "2");*/

        // "DataStore"という名前でインスタンスを生成
        CheckBalance = getSharedPreferences("CheckBalance", MODE_PRIVATE);
        return CheckBalance.getInt("edy", 0);
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

/*        for (int cnt =0; cnt <= cardNameArray.length; cnt++) {
            // putXxxx("キー",データ) にて書き込むデータを登録する
            editor.putInt(cardNameArray[cnt], moneyArray[cnt]);

            // 書き込みを確定する
            editor.commit();
        }*/

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

        Map<String, String> map = new HashMap<String, String>() {
            {
                put("edy", edy);
                put("suica", suica);
                put("pasumo", pasumo);
                put("bunkadou", bunkadou);
            }
        };

        return map;
    }

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

    public int dispEdyResult(byte[] EdyResult) {
        /*sb1.append("Edy番号: ");
        for(int i = 2; i <= 9 ; i++) {
            sb1.append(String.format("%02X",EdyResult[i]));
        }
        sb1.append("\n");*/

        //sb1.append("Edy残高: ");
        ByteBuffer bf = ByteBuffer.allocate(4);
        bf.put(EdyResult[19]);
        bf.put(EdyResult[18]);
        bf.put(EdyResult[17]);
        bf.put(EdyResult[16]);
        /*sb1.append(String.format("%05d",bf.getInt(0)));
        sb1.append("円");
        sb1.append("\n");*/

       /* sb1.append("\n");
        sb1.append("　　　　 日時　　　　, 種別 , 　金額　 , 　残高\n");
        for(int i = 0; i < 6; i++) {
            byte[] EdyHistory = new byte[16];
            for(int j = 0; j < 16; j++) {
                EdyHistory[j] = EdyResult[16 * (i + 2) + j];
            }
            dispEdyHistory(EdyHistory);
            sb1.append("\n");
        }*/

        //tv1.setText(sb1);
        int zandaka = 0;
        int size = bf.getInt(0);
        zandaka = size;

        return zandaka;
    }

    public void dispEdyHistory(byte[] EdyHistory) {

        // 日時
        ByteBuffer timebf = ByteBuffer.allocate(4);
        timebf.put(EdyHistory[4]);
        timebf.put(EdyHistory[5]);
        timebf.put(EdyHistory[6]);
        timebf.put(EdyHistory[7]);
        int timebfDiff = timebf.getInt(0);
        int dateDiff = timebfDiff >>> 17;
        int timeDiff = (timebfDiff << 15) >>> 15;

        Calendar historyDate = Calendar.getInstance();
        historyDate.set(2000, 0, 1, 0, 0, 0);
        historyDate.add(Calendar.DATE, dateDiff);
        sb1.append(String.format("%04d",historyDate.get(Calendar.YEAR)));
        sb1.append("/");
        sb1.append(String.format("%02d",historyDate.get(Calendar.MONTH) + 1));
        sb1.append("/");
        sb1.append(String.format("%02d",historyDate.get(Calendar.DATE)));
        sb1.append(", ");

        Calendar historyTime = Calendar.getInstance();
        historyTime.set(2000, 0, 1, 0, 0, 0);
        historyTime.add(Calendar.SECOND, timeDiff);
        sb1.append(String.format("%02d",historyTime.get(Calendar.HOUR_OF_DAY)));
        sb1.append(":");
        sb1.append(String.format("%02d",historyTime.get(Calendar.MINUTE)));
        sb1.append(":");
        sb1.append(String.format("%02d",historyTime.get(Calendar.SECOND)));
        sb1.append(", ");

        // 処理通番
        //for(int i = 1; i <= 3; i++) {
        //	sb1.append(String.format("%02X",EdyHistory[i]));
        //}
        //sb1.append(", ");

        // 処理種別
        if(EdyHistory[0] == (byte)0x20) {
            sb1.append("支払  ");
        } else if (EdyHistory[0] == (byte)0x02) {
            sb1.append("ﾁｬｰｼﾞ");
        } else if (EdyHistory[0] == (byte)0x04) {
            sb1.append("ｷﾞﾌﾄ  ");
        } else {
            sb1.append("不明  ");
        }
        sb1.append(", ");

        // 金額
        ByteBuffer bf1 = ByteBuffer.allocate(4);
        bf1.put(EdyHistory[8]);
        bf1.put(EdyHistory[9]);
        bf1.put(EdyHistory[10]);
        bf1.put(EdyHistory[11]);
        sb1.append(String.format("%05d",bf1.getInt(0)));
        sb1.append("円");
        sb1.append(", ");

        // 残高
        ByteBuffer bf2 = ByteBuffer.allocate(4);
        bf2.put(EdyHistory[12]);
        bf2.put(EdyHistory[13]);
        bf2.put(EdyHistory[14]);
        bf2.put(EdyHistory[15]);
        sb1.append(String.format("%05d",bf2.getInt(0)));
        sb1.append("円");
    }

    /*@Override
    protected void onResume() {
        super.onResume();
        if (mNfcAdapter != null) {
            // 起動中のActivityが優先的にNFCを受け取れるよう設定
            Intent intent = new Intent(this, this.getClass())
                    .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    getApplicationContext(), 0, intent, 0);
            mNfcAdapter.enableForegroundDispatch(this, pendingIntent, null,
                    null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            // Activityが非表示になる際に優先的にNFCを受け取る設定を解除
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }*/

}
