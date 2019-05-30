package com.kanishka.flock;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class ScanBluetoothActivity extends AppCompatActivity {

  private static final String TAG = "ScanBluetoothActivity";
  private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
  private Context context = this;
  private final int MY_PERMISSIONS_REQUEST = 200;
  private ListView scanList;
  private ScanListAdapter mAdapter;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_scan_bluetooth);

    scanList = findViewById(R.id.list_scanBT);

    int hasReadLocationPermission = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION);
    Log.d(TAG, "onCreate: checkSelfPermission = " + hasReadLocationPermission);

    if (hasReadLocationPermission != PackageManager.PERMISSION_GRANTED) {
      Log.d(TAG, "onCreate: requesting permission");
      ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST);
    }

    ArrayList<bluetoothList> arrayOfDevices = new ArrayList<bluetoothList>();
    mAdapter = new ScanListAdapter(ScanBluetoothActivity.this, arrayOfDevices);

    bluetoothScanning();

    scanList.setAdapter(mAdapter);

    scanList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        bluetoothList selectedItem = mAdapter.getItem(position);
        String mac = selectedItem.macAddress;
        String name = selectedItem.deviceName;

        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mac);
        device.createBond();
        mBluetoothAdapter.cancelDiscovery();
        finish();
        Intent i = new Intent(getBaseContext(), AvailableLocksActivity.class);
        startActivity(i);

      }
    });

  }

  void bluetoothScanning() {
    mBluetoothAdapter.startDiscovery();

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
      public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
          // Discovery has found a device. Get the BluetoothDevice
          // object and its info from the Intent.
          BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
          String deviceName = device.getName();
          String deviceHardwareAddress = device.getAddress(); // MAC address

          ScanBluetoothActivity.bluetoothList newDevice = new bluetoothList(deviceName, deviceHardwareAddress);
          mAdapter.add(newDevice);
          mAdapter.notifyDataSetChanged();
          Log.d(TAG, "Device : " + deviceName + "  MAC : " + deviceHardwareAddress);
        }
      }
    };

    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    context.registerReceiver(mReceiver, filter);
    Log.d(TAG, "onReceive: bluetooth list");
  }

  public class bluetoothList {
    String deviceName, macAddress;

    public bluetoothList(String deviceName, String macAddress) {
      this.deviceName = deviceName;
      this.macAddress = macAddress;
    }
  }
}
