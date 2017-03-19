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

    String mValue;
    String mHumidity;
    String mTemperature;
    TextView mTextViewInformation;
    TextView mTextViewHumidity;
    TextView mTextViewTemperature;
    Button mButton;
    Handler mHandler;
    Physicaloid mPhysicaloid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextViewInformation = (TextView) findViewById(R.id.text_view_information);
        mTextViewHumidity = (TextView) findViewById(R.id.text_view_humidity);
        mTextViewTemperature = (TextView) findViewById(R.id.text_view_temperature);
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
                mTextViewInformation.setVisibility(View.GONE);
                mButton.setText("Stop");

                mPhysicaloid.addReadListener(new ReadLisener() {
                    @Override
                    public void onRead(int size) {
                        byte[] buf = new byte[size];

                        mPhysicaloid.read(buf, size);
                        try {
                            mValue = new String(buf, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            return;
                        }

                        Integer num = decodePacket(buf);
                        mValue = String.valueOf(num);
                        separateString(mValue);

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mTextViewHumidity.setText(mHumidity);
                                mTextViewTemperature.setText(mTemperature);
                            }
                        });
                    }
                });

            } else {
                mTextViewInformation.setVisibility(View.VISIBLE);
                mTextViewInformation.setText("waiting open..");
            }
        } else {
            mPhysicaloid.close();
            mButton.setText("Start");
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

    private void separateString(String string) {
        // TODO
    }
}
