package com.kanishka.flock;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class AvailableLocksActivity extends AppCompatActivity {

  private static final String TAG = "AvailableLocksActivity";

  ListView mListView;
  private ArrayAdapter<String> aAdapter;
  private BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();
  public static BluetoothDevice device;
  ArrayList list;
  ArrayList<String> accesses;
  private final static int REQUEST_ENABLE_BT = 1;
  public static BluetoothSocket mBtSocket;
  public static BluetoothDevice mBTDevice;
  ListAdapter adapter;
  public static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
  public static String deviceName, macAddress, name, mac;
  public static OutputStream mmOutStream;

  private FirebaseAuth mAuth;
  private DatabaseReference mDatabaseReference;
  private FirebaseUser user;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_available_locks);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    FloatingActionButton fab = findViewById(R.id.fab);
    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_bluetooth_connected_black_24dp));
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent i = new Intent(getBaseContext(), ScanBluetoothActivity.class);
        startActivity(i);
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
          .setAction("Action", null).show();
      }
    });

    mAuth = FirebaseAuth.getInstance();
    user = mAuth.getCurrentUser();

    if (!bAdapter.isEnabled()) {
      Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    if (bAdapter == null) {
      Toast.makeText(getApplicationContext(), "Bluetooth Not Supported", Toast.LENGTH_SHORT).show();
    } else {
      Set<BluetoothDevice> pairedDevices = bAdapter.getBondedDevices();
      list = new ArrayList();
      ArrayList<bluetoothList> arrayOfDevices = new ArrayList<bluetoothList>();
      adapter = new ListAdapter(this, arrayOfDevices);
      if (pairedDevices.size() > 0) {
        for (BluetoothDevice device : pairedDevices) {
          deviceName = device.getName();
          macAddress = device.getAddress();
          if (macAddress.contains("24:0A:C4")) {
            bluetoothList newDevice = new bluetoothList(deviceName, macAddress);
            adapter.add(newDevice);
          }
          //Log.d(TAG, "onCreate: Name:  " + deviceName + "   Mac: " + macAddress);
        }
        mListView = (ListView) findViewById(R.id.listView);
//        aAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.two_line_list_item, list);

        mListView.setAdapter(adapter);
      }
    }

    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        bluetoothList selectedItem = adapter.getItem(position);
        mac = selectedItem.macAddress;
        name = selectedItem.deviceName;
        if (connectToDevice(mac)) {
          Log.d(TAG, "onItemClick: SuccessFul");
          //sendDataToPairedDevice( '1', mac);

          mDatabaseReference = FirebaseDatabase.getInstance().getReference().child(user.getUid()).child("My Accesses");
          mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
              accesses = new ArrayList<>();
              for (DataSnapshot child : dataSnapshot.getChildren()) {
                accesses.add(child.getKey());
              }
              Log.d(TAG, "onDataChange: locks" + accesses);
              if (accesses.contains(mac.replace(":", ""))) {
                Intent i = new Intent(getBaseContext(), FingerPrintAuth.class);
                startActivity(i);
                Toast.makeText(AvailableLocksActivity.this, "Authenticating " + name, Toast.LENGTH_SHORT).show();
              } else {
                Toast.makeText(AvailableLocksActivity.this, "You don't have access to this lock", Toast.LENGTH_LONG).show();
              }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
          });
        } else {
          Toast.makeText(AvailableLocksActivity.this, "Can not connect to " + name + ". Check if device is on or off.", Toast.LENGTH_LONG).show();
        }
      }
    });
  }

  public static class bluetoothList {
    String deviceName, macAddress;

    public bluetoothList(String deviceName, String macAddress) {
      this.deviceName = deviceName;
      this.macAddress = macAddress;
    }
  }

  private boolean connectToDevice(String iAdress) {
    try {
      //if(bAdapter == null) bAdapter = BluetoothAdapter.getDefaultAdapter();
      mBTDevice = bAdapter.getRemoteDevice(iAdress);
      mBtSocket = mBTDevice.createRfcommSocketToServiceRecord(SPP_UUID);
      mBtSocket.connect();
      Toast.makeText(AvailableLocksActivity.this, "Connected to " + iAdress, Toast.LENGTH_SHORT).show();
      return true;
    } catch (IOException e) {
      e.printStackTrace();

    } catch (Exception e) {
      e.printStackTrace();

    }
    return false;
  }

  public void sendDataToPairedDevice(Character message, String macAdd) {
    if (message != null) {
      //byte[] toSend = message.getBytes();
      try {
        device = bAdapter.getRemoteDevice(macAdd);
        //mBtSocket = device.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
        mmOutStream = mBtSocket.getOutputStream();
        if (mmOutStream != null) {
          mmOutStream.write(message);
        }
        // Your Data is sent to  BT connected paired device ENJOY.
      } catch (IOException e) {
        Log.e(TAG, "Exception during write", e);
      } catch (NullPointerException e) {
        Log.e(TAG, "NullPointerException", e);
      }
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    switch (id) {
      case R.id.menu_profile:
//        Toast.makeText(MainActivity.this, "Profile", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(getBaseContext(), UserDetailActivity.class);
        startActivity(i);
        break;
      default:
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

}
