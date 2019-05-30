package com.kanishka.flock;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class LocksActivity extends AppCompatActivity {

  private static final String TAG = "LocksActivity";

  private FloatingActionButton newLock;
  private ListView locksList;
  private ArrayList<String> lockName, lockId;
  public static String tempId, tempName;

  private FirebaseAuth mAuth;
  private DatabaseReference mDatabaseReference, aDatabaseReference, tDatabaseReference;
  private FirebaseUser user;

  private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
    = new BottomNavigationView.OnNavigationItemSelectedListener() {

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
      switch (item.getItemId()) {
        case R.id.navigation_home:
          Log.d(TAG, "onNavigationItemSelected: my locks");
          newLock.show();
          tDatabaseReference = FirebaseDatabase.getInstance().getReference().child(user.getUid()).child("My Accesses");
          mDatabaseReference = FirebaseDatabase.getInstance().getReference().child(user.getUid()).child("My Locks");
          mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
              lockName = new ArrayList<>();
              lockId = new ArrayList<>();
              for (DataSnapshot child : dataSnapshot.getChildren()) {
                lockName.add(child.getValue().toString());
                lockId.add(child.getKey());
                Log.d(TAG, "onDataChange: " + lockName);
              }
              ArrayAdapter adapter = new ArrayAdapter<>(LocksActivity.this, android.R.layout.simple_list_item_1, lockName);
              locksList.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
          });

          locksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
              UserDetailActivity.forAccess = true;
              tempId = lockId.get(position);
              tempName = lockName.get(position);
              Log.d(TAG, "onItemClick: " + tempId);
              startActivity(new Intent(LocksActivity.this, ContactsActivitty.class));

            }
          });

          locksList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
              tempId = lockId.get(position);
              tDatabaseReference.child(tempId).removeValue();
              mDatabaseReference.child(tempId).removeValue();
              return false;
            }
          });
          return true;

        case R.id.navigation_dashboard:
          newLock.hide();

          aDatabaseReference = FirebaseDatabase.getInstance().getReference().child(user.getUid()).child("My Accesses");
          aDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
              lockName = new ArrayList<>();
              lockId = new ArrayList<>();
              for (DataSnapshot child : dataSnapshot.getChildren()) {
                lockName.add(child.getValue().toString());
                lockId.add(child.getKey());
              }
              Log.d(TAG, "onDataChange: " + lockName);

              if (lockName != null) {
                ArrayAdapter adapter = new ArrayAdapter<>(LocksActivity.this, android.R.layout.simple_list_item_1, lockName);
                locksList.setAdapter(adapter);
              }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
          });

          locksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
          });

          locksList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
              return false;
            }
          });
          return true;
      }
      return false;
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_locks);

    newLock = findViewById(R.id.lock_new);
    BottomNavigationView navigation = findViewById(R.id.navigation);
    navigation.setSelectedItemId(R.id.navigation_home);
    Menu menu = navigation.getMenu();
    menu.findItem(R.id.navigation_home).setChecked(true);
    navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    mAuth = FirebaseAuth.getInstance();
    user = mAuth.getCurrentUser();
    locksList = findViewById(R.id.lock_list);

    newLock.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(LocksActivity.this, AddLockActivity.class));

      }
    });
  }

}
