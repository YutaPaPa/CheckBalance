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
}
