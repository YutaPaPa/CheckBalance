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
import android.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextEdy;
    private Button btnEdy;
    private SharedPreferences CheckBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // コントロールのセット
        findViews();

        // Edy残額のセット
        int edyMoney = 0;
        edyMoney = loadEdy();
        editTextEdy.setText(String.valueOf(edyMoney));

        // 登録ボタンにClickイベント設定
        btnEdy.setOnClickListener(this);
    }

    protected void findViews(){
        editTextEdy = (EditText)findViewById(R.id.editTextEdy);
        btnEdy = (Button)findViewById(R.id.button);
    }

    @Override
    public void onClick(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(editTextEdy, InputMethodManager.SHOW_FORCED);

        int edy = Integer.parseInt(editTextEdy.getText().toString());
        this.saveEdy(edy);
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
}
