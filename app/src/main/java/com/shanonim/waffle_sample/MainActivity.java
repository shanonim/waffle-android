package com.shanonim.waffle_sample;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.physicaloid.lib.Physicaloid;
import com.physicaloid.lib.usb.driver.uart.ReadLisener;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    String mValue;
    RelativeLayout mLayout;
    TextView mTextViewSerialValue;
    TextView mTextViewMessage;
    Button mButton;
    Handler mHandler;
    Physicaloid mPhysicaloid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLayout = (RelativeLayout) findViewById(R.id.layout_main_activity);
        mTextViewSerialValue = (TextView) findViewById(R.id.text_view_serial_data);
        mTextViewMessage = (TextView) findViewById(R.id.text_view_message);
        mButton = (Button) findViewById(R.id.button_main);

        if (mButton != null) {
            mButton.setOnClickListener(clicked);
        }

        mHandler = new Handler();
        mPhysicaloid = new Physicaloid(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPhysicaloid.close();
    }

    private View.OnClickListener clicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            readValue();
        }
    };

    public void readValue() {
        if (!mPhysicaloid.isOpened()) {
            if (mPhysicaloid.open()) {
                mTextViewSerialValue.setText("open.");
                mButton.setText("Stop");

                mPhysicaloid.addReadListener(new ReadLisener() { // リスナー登録

                    @Override
                    public void onRead(int size) { // Androidでシリアル文字を受信したらコールバックが発生
                        byte[] buf = new byte[size];

                        mPhysicaloid.read(buf, size);
                        try {
                            mValue = new String(buf, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            return;
                        }

                        Integer num = decodePacket(buf);
                        mValue = String.valueOf(num); // 数字をString型へ変換
                        mHandler.post(new Runnable() { // UIスレッドへ書き込む場合ハンドラを使う
                            @Override
                            public void run() {
                                mTextViewSerialValue.setText(mValue);
                                changeMessageAndBackgroundColor(mValue);
                            }
                        });
                    }
                });

            } else {
                mTextViewSerialValue.setText("waiting open..");
            }
        } else {
            mPhysicaloid.close();
            mButton.setText("Start");
        }
    }

    private void changeMessageAndBackgroundColor(String value) {
        int intValue = Integer.valueOf(value);
        if (intValue == 0) {
            // on the air.
            mTextViewMessage.setText("待機中です");
            mLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
        } else if (0 < intValue && intValue < 400) {
            // dry.
            mTextViewMessage.setText("土が乾いてきました");
            mLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorDry));
        } else if (400 <= intValue && intValue < 600) {
            // it seems good.
            mTextViewMessage.setText("ちょうどいい湿度です");
            mLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorGood));
        } else if (600 <= intValue && intValue < 1000) {
            // too much moisture.
            mTextViewMessage.setText("水が多すぎます");
            mLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorTooMuchMoisture));
        }
    }

    private int decodePacket(byte[] buf) {
        boolean existStx = false;
        int result = 0;

        for (int i = 0; i < buf.length; i++) {
            if (!existStx) {
                if (buf[i] == 's') { // 最初のsを検索
                    existStx = true;
                }
            } else {
                if (buf[i] == '\r') { // 最後の ¥r までresultに取り込む
                    return result;
                } else {
                    if ('0' <= buf[i] && buf[i] <= '9') { // 数値情報をシフトさせながらresultに保存する
                        result = result * 10 + (buf[i] - '0'); // 文字 '0' 分を引くことでASCIIコードから数値に変換
                    } else {
                        return -1;
                    }
                }
            }
        }
        return -1;
    }
}
