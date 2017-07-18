package com.example.masayuki.checkbalance;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageButton btnSendEdy = (ImageButton) this.findViewById(R.id.imageButton1);
        btnSendEdy.setOnClickListener(this);

        int edyMoney = this.loadEdy(this);
        EditText editTextEdy = (EditText) this.findViewById(R.id.editTextEdy);
        editTextEdy.setText(edyMoney);
    }

    @Override
    public void onClick(View v) {
        EditText editTextEdy = (EditText) this.findViewById(R.id.editTextEdy);
        editTextEdy.requestFocus();

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(editTextEdy, InputMethodManager.SHOW_FORCED);
    }

    /**
     * Preferences にパーソナルデータを保存する
     * @param context
     * @param money Edyの残額
     */
    private void saveEdy(Context context, int money){
        // アプリ標準の Preferences を取得する
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        // Preferences に書き込むための Editor クラスを取得する
        SharedPreferences.Editor editor = sp.edit();

        // putXxxx("キー",データ) にて書き込むデータを登録する
        editor.putInt("edy", money);

        // 書き込みを確定する
        editor.commit();
    }

    /**
     * Preferences から年齢を読み出す
     * @param context
     * @return Edyの残額(なければ 0 を返す)
     */
    private int loadEdy(Context context){
        // アプリ標準の Preferences を取得する
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt("edy", 0);
    }

    @Override
    public void onPause() {
        super.onPause();

        EditText editTextEdy = (EditText) this.findViewById(R.id.editTextEdy);
        int edy = Integer.parseInt(editTextEdy.getText().toString());
        this.saveEdy(this, edy);
    }
}
