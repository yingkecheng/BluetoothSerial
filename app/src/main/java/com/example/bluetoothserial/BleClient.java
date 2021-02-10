package com.example.bluetoothserial;

import android.bluetooth.BluetoothProfile;
import android.content.Context;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.model.BleGattProfile;

public class BleClient extends BluetoothClient {

    private BleGattProfile bleGattProfile;

    private String connectMac;

    public BleClient(Context context) {
        super(context);
    }

    public BleGattProfile getBleGattProfile() {
        return bleGattProfile;
    }

    public void setBleGattProfile(BleGattProfile bleGattProfile) {
        this.bleGattProfile = bleGattProfile;
    }

    public boolean isConnect() {
        return !(connectMac == null);
    }

    public String getConnectMac() {
        return connectMac;
    }

    public void setConnectMac(String connectMac) {
        this.connectMac = connectMac;
    }

    @Override
    public void disconnect(String mac) {
        super.disconnect(mac);
        connectMac = null;
    }

}
