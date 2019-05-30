package com.kanishka.flock;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class AddLockActivity extends AppCompatActivity {

  private static final String TAG = "AddLockActivity";

  private TextView id;
  private EditText lockName;
  private Button addLock;
  private int i;
  private String lockId, adminName;
  private ListView pairedLocks;
  private BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();
  ArrayList list;

  private final static int REQUEST_ENABLE_BT = 1;
  ListAdapter adapter;
  ArrayList<String> locks;
  public static String deviceName, macAddress, name, mac;

  private FirebaseAuth mAuth;
  private DatabaseReference mDatabaseReference, aDatabaseReference, mReference, lDatabaseReference;
  private FirebaseUser user;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_add_lock);

    id = findViewById(R.id.new_lock_id);
    lockName = findViewById(R.id.new_lock_name);
    addLock = findViewById(R.id.new_lock_add);
    pairedLocks = findViewById(R.id.list_paired_locks);

    mAuth = FirebaseAuth.getInstance();
    user = mAuth.getCurrentUser();

    mReference = FirebaseDatabase.getInstance().getReference().child(user.getUid()).child("firstName");
    mReference.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        adminName = dataSnapshot.getValue().toString();
      }

      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {

      }
    });

    if (!bAdapter.isEnabled()) {
      Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    if (bAdapter == null) {
      Toast.makeText(getApplicationContext(), "Bluetooth Not Supported", Toast.LENGTH_SHORT).show();
    } else {
      Set<BluetoothDevice> pairedDevices = bAdapter.getBondedDevices();
      list = new ArrayList();
      ArrayList<AvailableLocksActivity.bluetoothList> arrayOfDevices = new ArrayList<AvailableLocksActivity.bluetoothList>();
      adapter = new ListAdapter(this, arrayOfDevices);
      if (pairedDevices.size() > 0) {
        for (BluetoothDevice device : pairedDevices) {
          deviceName = device.getName();
          macAddress = device.getAddress();
          if (macAddress.contains("24:0A:C4")) {
            AvailableLocksActivity.bluetoothList newDevice = new AvailableLocksActivity.bluetoothList(deviceName, macAddress);
            adapter.add(newDevice);
          }
          Log.d(TAG, "onCreate: Name:  " + deviceName + "   Mac: " + macAddress);
        }
//        aAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.two_line_list_item, list);

        pairedLocks.setAdapter(adapter);
      }
    }

    pairedLocks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AvailableLocksActivity.bluetoothList selectedItem = adapter.getItem(position);
        mac = selectedItem.macAddress;
        name = selectedItem.deviceName;
        lockId = mac.replace(":", "");
      }
    });


//    i =  ((int) (Math.random() * 99999999));

    id.setText("Choose your lock.");

    addLock.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Log.d(TAG, "onClick: admin -" + adminName);

        lDatabaseReference = FirebaseDatabase.getInstance().getReference().child("All Locks");
        lDatabaseReference.addValueEventListener(new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            locks = new ArrayList<>();
            for (DataSnapshot child : dataSnapshot.getChildren()) {
              locks.add(child.getKey());
            }
            Log.d(TAG, "onDataChange: locks" + locks);
            if (locks.contains(lockId)) {
              Toast.makeText(AddLockActivity.this, "Sorry, Lock is already set up...!", Toast.LENGTH_LONG).show();
            } else {
              lDatabaseReference.child(lockId).setValue(user.getUid());

              mDatabaseReference = FirebaseDatabase.getInstance().getReference().child(user.getUid()).child("My Locks");
              mDatabaseReference.child(lockId).setValue(adminName + "'s " + lockName.getText().toString());

              aDatabaseReference = FirebaseDatabase.getInstance().getReference().child(user.getUid()).child("My Accesses");
              aDatabaseReference.child(lockId).setValue(adminName + "'s " + lockName.getText().toString());
            }
          }

          @Override
          public void onCancelled(@NonNull DatabaseError databaseError) {

          }
        });
      }
    });

  }
}
