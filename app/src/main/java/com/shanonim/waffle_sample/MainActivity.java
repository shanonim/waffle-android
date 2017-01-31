package com.shanonim.waffle_sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.physicaloid.lib.Physicaloid;

public class MainActivity extends AppCompatActivity {

    Button mButton;
    Physicaloid mPhysicaloid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton = (Button) findViewById(R.id.button_main);
        if (mButton != null) {
            mButton.setOnClickListener(clicked);
        }
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
            if (mPhysicaloid.open()) {
                Toast.makeText(MainActivity.this, "opened!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "not opened...", Toast.LENGTH_SHORT).show();
            }
        }
    };
}
