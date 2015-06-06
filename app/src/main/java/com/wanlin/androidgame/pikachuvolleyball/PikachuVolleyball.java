package com.wanlin.androidgame.pikachuvolleyball;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.ListView;

import com.kilobolt.framework.Screen;
import com.kilobolt.framework.implementation.AndroidGame;
import com.rick.androidgame.bluetooth.BluetoothModule;
import com.rick.androidgame.bluetooth.HandlerMessageCallback;

import java.util.ArrayList;

/**
 * Created by wanlin on 15/6/4.
 */

/**
 * when we start the game, the SampleGame class will be instantiated,
 * and the methods from the Activity Lifecycle will be called
 * (starting with the onCreate). These methods are all implemented
 * in the AndroidGame superclass that SampleGame extends.
 */

public class PikachuVolleyball extends AndroidGame implements HandlerMessageCallback {

    private static final String LOG_TAG = "PikachuVolleyball";
    public static final int TYPE_SCREEN_GAME = 0;
    public static final int TYPE_SCREEN_MENU = 1;
    private int currentSreentType;
    private boolean isHost = true;

    private BluetoothModule btModule;
    private BluetoothModule.BtListAdapter btDevicesListAdapter;

    public PikachuVolleyball() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set current screen type
        setCurScreenType(TYPE_SCREEN_MENU);

        // Init Bluetooth
        Log.d(LOG_TAG, "Trying to init Bluetooth");
        btModule = new BluetoothModule(this, this);
        ListView btDevicesListView = new ListView(getApplicationContext());
        ListView btMsgListView = new ListView(getApplicationContext());
        btDevicesListAdapter =  btModule.bindBtDevicesAdapter(btDevicesListView);
        btModule.bindMsgAdapter(btMsgListView, android.R.layout.simple_list_item_1);
    }

    /* ========== Customized Members ========== */

    @Override
    public void setCurScreenType(int type) {
        currentSreentType = type;
    }
    @Override
    public int getCurScreenType() {
        return currentSreentType;
    }

    /**
     * Get the BluetoothModule object
     * @return BluetoothModule object
     */
    public BluetoothModule getBtModule() {
        return btModule;
    }

    /**
     * Get discovered Bluetooth devices
     * @return an ArrayList of BluetoothDevices
     */
    public ArrayList<BluetoothDevice> getFoundDevices() {
        ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
        int size = btDevicesListAdapter.getCount();

        for (int i = 0; i < size; i++) {
            BluetoothDevice btd = (BluetoothDevice)btDevicesListAdapter.getItem(i);
            devices.add(btd);
        }
        return devices;
    }

    /**
     * Check if this device is the host of Bluetooth connection
     * @return
     */
    public boolean isHost() {
        return this.isHost;
    }

    /**
     * The callback function of Bluetooth messages receiver thread
     * @param msg Message passed by receiver thread
     */
    @Override
    public void msgCallback(Message msg) {
        String strMsg;
        switch (msg.what) {
            case BluetoothModule.SERVERSOCK_THREAD_WHAT:
                strMsg = msg.getData().getString(BluetoothModule.SERVERSOCK_MSG_KEY);
                Log.d(LOG_TAG, "Got message: " + strMsg);
                if (strMsg == BluetoothModule.RESUlT_CONN_OK) {
                    if (getCurScreenType() == TYPE_SCREEN_MENU) {
                        isHost = true;
                        ((MainMenuScreen) getCurrentScreen()).startGame();
                    }
                }
                break;
            case BluetoothModule.CLIENTSOCK_THREAD_WHAT:
                strMsg = msg.getData().getString(BluetoothModule.CLIENTSOCK_MSG_KEY);
                Log.d(LOG_TAG, "Got message: " + strMsg);
                if (strMsg == BluetoothModule.RESUlT_CONN_OK) {
                    if (getCurScreenType() == TYPE_SCREEN_MENU) {
                        isHost = false;
                        ((MainMenuScreen) getCurrentScreen()).startGame();
                    }
                }
                break;
            case BluetoothModule.RECEIVER_THREAD_WHAT:
                strMsg = msg.getData().getString(BluetoothModule.RECEIVER_MSG_KEY);
                Log.d(LOG_TAG, "Got message: " + strMsg);
                if (getCurScreenType() == TYPE_SCREEN_GAME) {
                    int controlCmd = Integer.parseInt(strMsg);
                    if (controlCmd == GameScreen.STOP_MOVING) {
                        getCurrentScreen().pause();
                    }
                    else if (controlCmd == GameScreen.YOU_GOOD_TO_GO) {
                        getCurrentScreen().resume();
                    }
                    else if (controlCmd == GameScreen.START_THAT_FUKING_GAMEEEE) {
                        ((GameScreen) getCurrentScreen()).stargGame();
                    }
                    else {
                        ((GameScreen) getCurrentScreen()).getEnemy().handleAction(controlCmd);
                    }
                }
                break;
            case BluetoothModule.SYS_MSG_WHAT:
                strMsg = msg.getData().getString(BluetoothModule.SYS_MSG_KEY);
                Log.d(LOG_TAG, "Got message: " + strMsg);
                break;
            default:
                Log.d(LOG_TAG, "Message error");
                strMsg = "Message error";
                break;
        }
    }

    @Override
    public Screen getInitScreen() {
        return new LoadingScreen(this);
    }

    @Override
    public void onBackPressed() {
        getCurrentScreen().backButton();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case BluetoothModule.REQUEST_ENABLE_BIT:
                Log.d(LOG_TAG, "[onActivityResult] REQUEST_ENABLE_BIT");
                if (resultCode == RESULT_OK) {
                    btModule.btOKCallback();
                }
                else if (resultCode == RESULT_CANCELED) {
                    btModule.btErrorCallback();
                }
                break;
        }
    }
}
