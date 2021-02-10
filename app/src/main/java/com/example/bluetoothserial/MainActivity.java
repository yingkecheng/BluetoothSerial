package com.example.bluetoothserial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bluetoothserial.util.ToastUtil;
import com.github.clans.fab.FloatingActionButton;
import com.inuker.bluetooth.library.Constants;
import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.model.BleGattService;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;

import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements BluetoothDeviceAdapter.Listener, View.OnClickListener {

    private static final String TAG = "MainActivity";

    private BleClient mClient;
    private BluetoothDeviceAdapter mDeviceAdapter;

    private Switch mSwitch;
    private TextView tip;
    private RecyclerView mRecyclerView;
    private FloatingActionButton fabUnlock;
    private FloatingActionButton fabSearch;
    private FloatingActionButton fabCommunicate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bluetoothPermissions();

        mClient = new BleClient(this);
        mDeviceAdapter = new BluetoothDeviceAdapter(this);
        mSwitch = (Switch) findViewById(R.id.bluetooth_switch);
        tip = (TextView) findViewById(R.id.tip);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyler_view);
        fabUnlock = (FloatingActionButton) findViewById(R.id.unlock);
        fabSearch = (FloatingActionButton) findViewById(R.id.search);
        fabCommunicate = (FloatingActionButton) findViewById(R.id.communicate);

        mSwitch.setChecked(mClient.isBluetoothOpened());

        // 注册监听
        mClient.registerBluetoothStateListener(new BluetoothStateListener() {
            @Override
            public void onBluetoothStateChanged(boolean openOrClosed) {
                mSwitch.setChecked(openOrClosed);
                if (!openOrClosed) {
                    mDeviceAdapter.clearAll();
                }
            }
        });


        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mClient.openBluetooth();
                } else {
                    mClient.closeBluetooth();
                }
            }
        });
        fabUnlock.setOnClickListener(this);
        fabSearch.setOnClickListener(this);
        fabCommunicate.setOnClickListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mDeviceAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public void onItemClick(BluetoothDevice bluetoothDevice) {
        BleConnectOptions options = new BleConnectOptions.Builder()
                .setConnectRetry(3)   // 连接如果失败重试3次
                .setConnectTimeout(30000)   // 连接超时30s
                .setServiceDiscoverRetry(3)  // 发现服务如果失败重试3次
                .setServiceDiscoverTimeout(20000)  // 发现服务超时20s
                .build();
        mClient.connect(bluetoothDevice.getAddress(), options, new BleConnectResponse() {
            @Override
            public void onResponse(int code, BleGattProfile data) {
                if (code == Constants.REQUEST_SUCCESS) {
                    ToastUtil.showMessage("连接成功");
                    mClient.setBleGattProfile(data);
                    mClient.setConnectMac(bluetoothDevice.getAddress());
                    tip.setText("连接上" + bluetoothDevice.getAddress());
                } else {
                    ToastUtil.showMessage("连接失败");
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                AlertDialog textDialog = new AlertDialog.Builder(this)
                        .setTitle("说明")
                        .setMessage("软件用于ble串口通信， 目前仅支持mlt-05")
                        .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search:
               doSearch();
               break;
            case R.id.unlock:
                unlock();
                break;
            case R.id.communicate:
                doComumunicate();
                break;
        }
    }

    private void doSearch() {
        mDeviceAdapter.clearAll();
        SearchRequest request = new SearchRequest.Builder()
                .searchBluetoothLeDevice(3000, 3)   // 先扫BLE设备3次，每次3s
                .searchBluetoothClassicDevice(5000) // 再扫经典蓝牙5s
                .searchBluetoothLeDevice(2000)      // 再扫BLE设备2s
                .build();

        mClient.search(request, new SearchResponse() {
            @Override
            public void onSearchStarted() {

            }

            @Override
            public void onDeviceFounded(SearchResult device) {
                if (device.rssi != 0) {
                    mDeviceAdapter.add(device.device);
                }
            }

            @Override
            public void onSearchStopped() {

            }

            @Override
            public void onSearchCanceled() {

            }
        });
    }

    private void unlock() {
        if (mClient.isConnect()) {
            String mac = mClient.getConnectMac();
            List<BleGattService> services = mClient.getBleGattProfile().getServices();
            BleGattService service = services.get(services.size() - 1);
            UUID seviceUuid = service.getUUID();
            UUID cUuid = service.getCharacters().get(0).getUuid();

            mClient.write(mac, seviceUuid, cUuid, "unlock".getBytes(), new BleWriteResponse() {
                @Override
                public void onResponse(int code) {
                    if (code == Constants.REQUEST_SUCCESS) {
                        ToastUtil.showMessage("发送指令成功");
                    } else {
                        ToastUtil.showMessage("发送指令失败");
                    }
                }
            });
        } else {
            ToastUtil.showMessage("未连接蓝牙");
        }
    }

    private void doComumunicate() {
        if (mClient.isConnect()) {
            String mac = mClient.getConnectMac();
            List<BleGattService> services = mClient.getBleGattProfile().getServices();
            BleGattService service = services.get(services.size() - 1);
            UUID seviceUuid = service.getUUID();
            UUID cUuid = service.getCharacters().get(0).getUuid();

            final EditText editText = new EditText(this);
            AlertDialog inputDialog = new AlertDialog.Builder(this)
                    .setTitle("请输入要发送的消息")
                    .setView(editText)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mClient.write(mac, seviceUuid, cUuid, editText.getText().toString().getBytes(), new BleWriteResponse() {
                                @Override
                                public void onResponse(int code) {
                                    if (code == Constants.REQUEST_SUCCESS) {
                                        ToastUtil.showMessage("发送指令成功");
                                    } else {
                                        ToastUtil.showMessage("发送指令失败");
                                    }
                                }
                            });
                        }
                    }).show();
        } else {
            ToastUtil.showMessage("未连接蓝牙");
        }
    }

    private void bluetoothPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            Log.d(TAG, "request ACCESS_FINE_LOCATION for bluetooth");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "成功获取地理位置权限");
        } else {
            Toast.makeText(MainActivity.this, "用户拒绝了权限", Toast.LENGTH_SHORT).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}