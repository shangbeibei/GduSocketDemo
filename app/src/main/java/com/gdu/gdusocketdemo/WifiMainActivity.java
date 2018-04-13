package com.gdu.gdusocketdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by zhangzhilai on 2018/1/5.
 */

public class WifiMainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void match(View view) {
        Intent intent = new Intent(this, APConnectActivity.class);
        startActivity(intent);
    }

    public void modify(View view) {
        Intent intent = new Intent(this, APTestActivity.class);
        startActivity(intent);
    }
}
