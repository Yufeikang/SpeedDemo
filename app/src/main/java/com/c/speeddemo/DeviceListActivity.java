package com.c.speeddemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DeviceListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    private static final String TAG = DeviceListActivity.class.getSimpleName();
    private static final String SPEED_TAG = "010D";
    private ProgressBar mLoading;
    private BluetoothAdapter mBluetoothAdapter;
    private List<BluetoothDevice> mDeviceList;
    private DeviceAdapter mAdapter;
    private ListView mListView;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("BT onReceive:",action);
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                Log.i("Discover BT :",device.getName() + "\n" + device.getAddress());
                mDeviceList.add(device);
                mAdapter.refresh(mDeviceList);
            }else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mLoading.setVisibility(View.VISIBLE);
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                mLoading.setVisibility(View.GONE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        mLoading= (ProgressBar) findViewById(R.id.loading);
        mListView= (ListView) findViewById(R.id.list);
        mDeviceList=new ArrayList<>();
        mAdapter=new DeviceAdapter(mDeviceList,this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

        Button bt_scanBT = (Button) findViewById(R.id.btn_pair_more_devices);
        assert bt_scanBT != null;
        bt_scanBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBluetoothAdapter.isDiscovering()){
                    return;
                }
                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

                if (pairedDevices.size() > 0) {
                    // Loop through paired devices
                    for (BluetoothDevice device : pairedDevices) {
                        // Add the name and address to an array adapter to show in a ListView
                        Log.i("pairedDevices BT :",device.getName() + "\n" + device.getAddress());
                        mDeviceList.add(device);
                    }
                    mAdapter.refresh(mDeviceList);
                }
                boolean is = mBluetoothAdapter.startDiscovery();

                Log.i("Start Scan:",is+":"+ mBluetoothAdapter.getScanMode());
            }
        });
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BluetoothDevice device = mDeviceList.get(position);
        Log.i("Bound Blue :",device.getName());
        Intent intent=new Intent(DeviceListActivity.this,DashBoardActivity.class);
        intent.putExtra("device",device);
        startActivity(intent);
    }
}
