package com.gdu.gdusocketdemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.gdu.gduclient.handler.ActionHandler;
import com.gdu.gduclient.util.ActionUtil;
import com.gdu.gduclient.wifi.ConnectAPAction;
import com.gdu.gduclient.wifi.DisConnectAPAction;
import com.gdu.gduclient.wifi.GetWifiInfoAction;
import com.gdu.gduclient.wifi.ReStartWifiAction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangzhilai on 2017/12/27.
 */

public class APConnectActivity extends Activity {

    private TextView mWifiInfoTextView;
    private TextView mWifiMatchTextView;
    private ListView mWifiListView;
    private List<String> mWifiInfoList;
    private String mCurrentWifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ap_connect);
        initView();
        initData();
    }

    private void initData() {
        mWifiInfoList = new ArrayList<>();
    }

    private void initView() {
        mWifiInfoTextView = (TextView) findViewById(R.id.wifi_info_textview);
        mWifiMatchTextView = (TextView)findViewById(R.id.wifi_match_textview);
        mWifiListView = (ListView) findViewById(R.id.wifi_listview);
        mWifiListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCurrentWifi = mWifiInfoList.get(position);
                mWifiInfoTextView.setText(mCurrentWifi);
            }
        });
    }

    public void scan(View view) {
        ActionUtil.changeApOrStaURL(false);
        mWifiInfoList.clear();
        new GetWifiInfoAction().execute(true, new ActionHandler() {
            @Override
            public void doActionStart() {

            }

            @Override
            public void doActionEnd() {

            }

            @Override
            public void doActionResponse(int status, Serializable message) {
                Log.d("test", "test status:" + status + " " + message);
                if (message != null) {
                    try {
                        JSONObject obj = new JSONObject(message.toString());
                        JSONObject result = (JSONObject) obj.get("result");
                        JSONArray wifiA = (JSONArray) result.get("wifi");

                        for (int i = 0; i < wifiA.length(); i++) {
                            mWifiInfoList.add(wifiA.getString(i));
                        }
                        setData(mWifiInfoList);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
//                mWifiInfoTextView.setText(message.toString());
            }

            @Override
            public void doActionRawData(Serializable data) {

            }
        });
    }

    public void setData(List<String> strings){
        mWifiListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1,strings));
    }

    public void match(View view) {
        new ConnectAPAction(mCurrentWifi, "12345678").execute(true, new ActionHandler() {
            @Override
            public void doActionStart() {

            }

            @Override
            public void doActionEnd() {

            }

            @Override
            public void doActionResponse(int status, Serializable message) {
                Log.d("test", "test status match:" + status + " " + message);
                if (status == 0) {
                    restartWifi(0);
                }else {
                    mWifiMatchTextView.setText("解配失败");
                }

//                mWifiMatchTextView.setText("配对" + message.toString());
            }

            @Override
            public void doActionRawData(Serializable data) {

            }
        });
    }

    public void disMatch(View view) {
        new DisConnectAPAction(1).execute(true, new ActionHandler() {
            @Override
            public void doActionStart() {

            }

            @Override
            public void doActionEnd() {

            }

            @Override
            public void doActionResponse(int status, Serializable message) {
                Log.d("test", "test status disMatch:" + status + " " + message);
                if(status == 0){
                    restartWifi(1);
                } else {
                    mWifiMatchTextView.setText("解配失败");
                }
            }

            @Override
            public void doActionRawData(Serializable data) {

            }
        });
    }

    public void restartWifi(final int type){
        new ReStartWifiAction().execute(true, new ActionHandler() {
            @Override
            public void doActionStart() {

            }

            @Override
            public void doActionEnd() {

            }

            @Override
            public void doActionResponse(int status, Serializable message) {
                Log.d("test", "test status restartWifi:" + status + " " + message);

                if (type == 0) {
                    mWifiMatchTextView.setText("配对成功");
                } else {
                    mWifiMatchTextView.setText("解配成功");
                }
            }

            @Override
            public void doActionRawData(Serializable data) {

            }
        });
    }
}
