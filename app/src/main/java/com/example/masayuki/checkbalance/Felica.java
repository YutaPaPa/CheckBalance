package com.example.masayuki.checkbalance;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Masayuki on 2017/08/28.
 */

public class Felica {

    public Felica() {
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
    public byte[] readWithoutEncryption(byte[] idm, int size) throws IOException {
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
    public int parse(byte[] res) throws Exception {
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

//    public String toHex(byte[] id) {
//        StringBuilder sbuf = new StringBuilder();
//        for (int i = 0; i < id.length; i++) {
//            String hex = "0" + Integer.toString((int) id[i] & 0x0ff, 16);
//            if (hex.length() > 2)
//                hex = hex.substring(1, 3);
//            sbuf.append(" " + i + ":" + hex);
//        }
//        return sbuf.toString();
//    }

    //    public void dispEdyHistory(byte[] EdyHistory) {
//
//        // 日時
//        ByteBuffer timebf = ByteBuffer.allocate(4);
//        timebf.put(EdyHistory[4]);
//        timebf.put(EdyHistory[5]);
//        timebf.put(EdyHistory[6]);
//        timebf.put(EdyHistory[7]);
//        int timebfDiff = timebf.getInt(0);
//        int dateDiff = timebfDiff >>> 17;
//        int timeDiff = (timebfDiff << 15) >>> 15;
//
//        Calendar historyDate = Calendar.getInstance();
//        historyDate.set(2000, 0, 1, 0, 0, 0);
//        historyDate.add(Calendar.DATE, dateDiff);
//        sb1.append(String.format("%04d",historyDate.get(Calendar.YEAR)));
//        sb1.append("/");
//        sb1.append(String.format("%02d",historyDate.get(Calendar.MONTH) + 1));
//        sb1.append("/");
//        sb1.append(String.format("%02d",historyDate.get(Calendar.DATE)));
//        sb1.append(", ");
//
//        Calendar historyTime = Calendar.getInstance();
//        historyTime.set(2000, 0, 1, 0, 0, 0);
//        historyTime.add(Calendar.SECOND, timeDiff);
//        sb1.append(String.format("%02d",historyTime.get(Calendar.HOUR_OF_DAY)));
//        sb1.append(":");
//        sb1.append(String.format("%02d",historyTime.get(Calendar.MINUTE)));
//        sb1.append(":");
//        sb1.append(String.format("%02d",historyTime.get(Calendar.SECOND)));
//        sb1.append(", ");
//
//        // 処理通番
//        //for(int i = 1; i <= 3; i++) {
//        //	sb1.append(String.format("%02X",EdyHistory[i]));
//        //}
//        //sb1.append(", ");
//
//        // 処理種別
//        if(EdyHistory[0] == (byte)0x20) {
//            sb1.append("支払  ");
//        } else if (EdyHistory[0] == (byte)0x02) {
//            sb1.append("ﾁｬｰｼﾞ");
//        } else if (EdyHistory[0] == (byte)0x04) {
//            sb1.append("ｷﾞﾌﾄ  ");
//        } else {
//            sb1.append("不明  ");
//        }
//        sb1.append(", ");
//
//        // 金額
//        ByteBuffer bf1 = ByteBuffer.allocate(4);
//        bf1.put(EdyHistory[8]);
//        bf1.put(EdyHistory[9]);
//        bf1.put(EdyHistory[10]);
//        bf1.put(EdyHistory[11]);
//        sb1.append(String.format("%05d",bf1.getInt(0)));
//        sb1.append("円");
//        sb1.append(", ");
//
//        // 残高
//        ByteBuffer bf2 = ByteBuffer.allocate(4);
//        bf2.put(EdyHistory[12]);
//        bf2.put(EdyHistory[13]);
//        bf2.put(EdyHistory[14]);
//        bf2.put(EdyHistory[15]);
//        sb1.append(String.format("%05d",bf2.getInt(0)));
//        sb1.append("円");
//    }
}
