package com.example.lab07;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final int REQUEST_BLUETOOTH_PERMISSION = 2;

    private TextView state;
    private ListView listDevices;
    private BluetoothAdapter btAdapter;
    private ArrayAdapter<String> btArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        state = findViewById(R.id.state);
        listDevices = findViewById(R.id.listdevices);

        btArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listDevices.setAdapter(btArrayAdapter);

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        checkBluetoothPermission();
        checkBluetoothState();

        registerReceiver(BluetoothFoundReceiver,
                new IntentFilter(BluetoothDevice.ACTION_FOUND));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(BluetoothFoundReceiver);
    }

    public void btnScan_Click(View view) {
        btArrayAdapter.clear();
        btAdapter.startDiscovery();
        state.setText("正在掃描中...");
    }

    private void checkBluetoothState() {
        if (btAdapter == null) {
            state.setText("此裝置不支援藍牙");
        } else {
            if (btAdapter.isEnabled()) {
                state.setText("藍牙已啟用");
                Button btnScan = findViewById(R.id.btnScan);
                btnScan.setEnabled(true);
            } else {
                state.setText("藍牙尚未啟用，請允許啟動");
                Intent intent =
                        new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, REQUEST_ENABLE_BLUETOOTH);
            }
        }
    }

    private void checkBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            String[] permissions = {
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };


            boolean needPermission = false;
            for (String p : permissions) {
                if (ContextCompat.checkSelfPermission(this, p)
                        != PackageManager.PERMISSION_GRANTED) {
                    needPermission = true;
                }
            }

            if (needPermission) {
                ActivityCompat.requestPermissions(
                        this,
                        permissions,
                        REQUEST_BLUETOOTH_PERMISSION
                );
            }
        }
    }

    private final BroadcastReceiver BluetoothFoundReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                        BluetoothDevice device =
                                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                        if (device != null) {
                            btArrayAdapter.add(device.getName()
                                    + "\n" + device.getAddress());
                            btArrayAdapter.notifyDataSetChanged();
                        }
                    }
                }
            };
}
