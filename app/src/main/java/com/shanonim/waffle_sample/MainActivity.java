package com.shanonim.waffle_sample;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.physicaloid.lib.Physicaloid;
import com.physicaloid.lib.usb.driver.uart.ReadLisener;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    TextView mTextView;
    Button mButton;
    Physicaloid mPhysicaloid;
    String mValue;
    Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.text_view_serial_data);
        mButton = (Button) findViewById(R.id.button_main);

        if (mButton != null) {
            mButton.setOnClickListener(clicked);
        }
        mPhysicaloid = new Physicaloid(getApplicationContext());
        mHandler = new Handler();
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
        if(!mPhysicaloid.isOpened()) {
            if(mPhysicaloid.open()) {
                mTextView.setText("open.");
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
                                mTextView.setText(mValue);
                            }
                        });
                    }
                });

            } else {
                mTextView.setText("Cannot open.");
            }
        } else {
            mPhysicaloid.close();
            mButton.setText("Start");
        }
    }

    private int decodePacket(byte[] buf) {
        boolean existStx = false;
        int result = 0;

        for(int i=0; i<buf.length; i++) {
            if(!existStx) {
                if(buf[i] == 's') { // 最初のsを検索
                    existStx = true;
                }
            } else {
                if(buf[i] == '\r') { // 最後の ¥r までresultに取り込む
                    return result;
                } else {
                    if('0' <= buf[i] && buf[i] <= '9') { // 数値情報をシフトさせながらresultに保存する
                        result = result*10 + (buf[i]-'0'); // 文字 '0' 分を引くことでASCIIコードから数値に変換
                    } else {
                        return -1;
                    }
                }
            }
        }
        return -1;
    }
}
