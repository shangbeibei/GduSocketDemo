package com.gdu.gdusocketdemo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gdu.gduclient.util.ActionUtil;
import com.gdu.gduclient.wifi.GetWifiChannelAction;
import com.gdu.gduclient.handler.ActionHandler;
import com.gdu.gduclient.wifi.GetWifiInfoAction;
import com.gdu.gduclient.wifi.GetWifiPasswordAction;
import com.gdu.gduclient.wifi.GetWifiPowerAction;
import com.gdu.gduclient.wifi.GetWifiStaSignalAction;
import com.gdu.gduclient.wifi.ReStartWifiAction;
import com.gdu.gduclient.wifi.SetWifiChannelAction;
import com.gdu.gduclient.wifi.SetWifiCountryAction;
import com.gdu.gduclient.wifi.SetWifiNameAction;
import com.gdu.gduclient.wifi.SetWifiPasswordAction;
import com.gdu.gduclient.wifi.SetWifiPowerAction;
import com.gdu.gduclient.wifi.GetWifiNameAction;
import com.gdu.remotecontrol.model.GDUConstants;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zhangzhilai on 2017/12/18.
 */

public class APTestActivity extends Activity {

    private Context mContext;
    private TextView mResultTextView;
    private EditText mWifiNameEdit;
    private EditText mWifiChannelEdit;
    private EditText mWifiPowerEdit;
    private EditText mWifiPassEdit;
    private Button mAPAndStaChangeButton;

    private boolean isAp = true;
    private boolean isSetup = false;

    private final int SET_CHANNEL = 1;
    private final int SET_NAME = 2;
    private final int SET_POWER = 3;
    private final int SET_PASSWORD = 4;
    private final int RESTART_WIFI = 5;
    private int mCurrentType;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ap_test);
        mContext = this;
//        testTimer();
        initView();
    }

    private void testTimer() {
        Log.d("TEST", "TEST 1 " + android.os.Process.myTid());
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d("TEST", "TEST 2 " + android.os.Process.myTid());
            }
        }, 0, 1000);
    }

    private void initView() {
        mAPAndStaChangeButton = (Button)findViewById(R.id.ap_sta_change_button);
        mResultTextView = (TextView) findViewById(R.id.result_textview);
        mWifiNameEdit     = (EditText) findViewById(R.id.setWifiName_edit);
        mWifiChannelEdit  = (EditText) findViewById(R.id.setWifiChannel_edit);
        mWifiPowerEdit    = (EditText) findViewById(R.id.setWifiPower_edit) ;
        mWifiPassEdit     = (EditText) findViewById(R.id.setWifiPassword_edit) ;
    }


    public void getWifiChannel(View view) {
        mCurrentType = -1;
        new GetWifiChannelAction().execute(true, mActionHandler);
    }

    public void getWifiName(View view) {
        mCurrentType = -1;
        new GetWifiNameAction().execute(true, mActionHandler);
    }

    public void getWifiInfo(View view) {
        mCurrentType = -1;
        new GetWifiInfoAction().execute(true, mActionHandler);
    }

    private ActionHandler mActionHandler = new ActionHandler() {
        @Override
        public void doActionStart() {

        }

        @Override
        public void doActionEnd() {

        }

        @Override
        public void doActionResponse(int status, Serializable message) {
            Log.d("test", "test status: " + status + " message:" + message);
            String result = "";
            String me = message != null ? message.toString() : null;
            switch (mCurrentType){
                case SET_CHANNEL:
                    result += "设置channel: ";
                    restartWifi(null);
                    break;
                case SET_NAME:
                    result += "设置Name: ";
                    restartWifi(null);
                    break;
                case SET_PASSWORD:
                    result += "设置password: ";
                    restartWifi(null);
                    break;
                case SET_POWER:
                    result += "设置power: ";
                    restartWifi(null);
                    break;
                case RESTART_WIFI:
                    Toast.makeText(mContext, "重启wifi成功", Toast.LENGTH_SHORT).show();
                    break;
            }
//            if (mCurrentType != RESTART_WIFI) {
                mResultTextView.setText(me);
//            }
        }

        @Override
        public void doActionRawData(Serializable data) {

        }
    };

    public void getWifiPower(View view) {
        mCurrentType = -1;
        new GetWifiPowerAction().execute(true, mActionHandler);
    }

    public void getWifiPassword(View view) {
        mCurrentType = -1;
        new GetWifiPasswordAction().execute(true, mActionHandler);
    }

    public void setWifiChannel(View view) {
        String channelS = mWifiChannelEdit.getText().toString();
        if (TextUtils.isEmpty(channelS)) {
            Toast.makeText(mContext, "输入为空", Toast.LENGTH_SHORT).show();
                  return;
        }
        int channel = Integer.parseInt(channelS);
        mCurrentType = SET_CHANNEL;
        new SetWifiChannelAction(channel).execute(true, mActionHandler);
    }

    public void setWifiName(View view) {
        String wifiName = mWifiNameEdit.getText().toString();
        if (TextUtils.isEmpty(wifiName)) {
            Toast.makeText(mContext, "输入为空", Toast.LENGTH_SHORT).show();
            return;
        }
        mCurrentType = SET_NAME;
        new SetWifiNameAction(wifiName).execute(true, mActionHandler);
    }

    public void setWifiPower(View view) {
        String wifiPowerS = mWifiPowerEdit.getText().toString();
        if (TextUtils.isEmpty(wifiPowerS)) {
            Toast.makeText(mContext, "输入为空", Toast.LENGTH_SHORT).show();
            return;
        }
        mCurrentType = SET_POWER;
        new SetWifiPowerAction(Integer.parseInt(wifiPowerS)).execute(true, mActionHandler);
    }

    public void setWifiPassword(View view) {
        String wifiPassword = mWifiPassEdit.getText().toString();
        if (TextUtils.isEmpty(wifiPassword)) {
            Toast.makeText(mContext, "输入为空", Toast.LENGTH_SHORT).show();
            return;
        }
        mCurrentType = SET_PASSWORD;
        new SetWifiPasswordAction(wifiPassword).execute(true, mActionHandler);
    }

    public void restartWifi(View view) {
        mCurrentType = RESTART_WIFI;
        new ReStartWifiAction().execute(true, mActionHandler);
    }

    public void getWifiStaSignal(View view) {
        mCurrentType = -1;
        new GetWifiStaSignalAction().execute(true, mActionHandler);
    }

    public void apStaChange(View view) {
        if (isAp) {
            isAp = false;
            mAPAndStaChangeButton.setText("遥控器wifi操作");
        } else {
            isAp = true;
            mAPAndStaChangeButton.setText("飞机wifi操作");
        }
        ActionUtil.changeApOrStaURL(isAp);
    }

    public void setWifiCountry(View view) {
        String locale = getResources().getConfiguration().locale.getCountry();
//        TelephonyManager.getSimCountryIso()
        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        System.out.println("test: " + manager.getSimCountryIso());
        ActionUtil.setUrlAndPort("192.168.11.123", 8801);
        String country = "US";
        new SetWifiCountryAction(country).execute(true, mActionHandler);
    }

    public void connectRC(View view) {
        ActionUtil.setUrlAndPort(GDUConstants.LOCAL_IP, GDUConstants.LOCAL_PORT_RC_USB);
    }
}
