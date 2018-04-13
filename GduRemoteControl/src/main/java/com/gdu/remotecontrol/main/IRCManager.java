package com.gdu.remotecontrol.main;

/**
 * Created by zhangzhilai on 2018/1/24.
 */

public interface IRCManager {

    interface OnRCManagerListener{
        void onAOACheck(String model);
        void onUsbOpen();
        void onUsbClose();
        class RCListenerAdapter implements OnRCManagerListener{

            @Override
            public void onAOACheck(String model) {

            }

            @Override
            public void onUsbOpen() {

            }

            @Override
            public void onUsbClose() {

            }

        }
    }
}
