package com.kanishka.flock;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ContactsActivitty extends AppCompatActivity {

  private static final String TAG = "ContactsActivitty";

  ProgressDialog dialog;
  DatabaseReference mDatabaseReference, userDatabaseReference, accessDatabaseReference;
  FirebaseAuth mAuth;
  FirebaseUser user;
  UserInformation userInformation;
  ListView friendsList;
  ArrayList<String> users, contact, friendUID, nums;
  public static String UID;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_contacts_activitty);

    friendsList = findViewById(R.id.friends_list);
    mAuth = FirebaseAuth.getInstance();
    user = mAuth.getCurrentUser();
    mDatabaseReference = FirebaseDatabase.getInstance().getReference().child(user.getUid()).child("Contacts");

    dialog = new ProgressDialog(this);
    dialog.setMessage("Uploading contacts...");

    Cursor contacts = getContentResolver().query(
      ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
      new String[]{
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.NUMBER
      },
      null,
      null,
      ContactsContract.Contacts.DISPLAY_NAME
    );

    final HashMap<String, String> map = new HashMap<>();
    final ArrayList<String> contactNum = new ArrayList<>();
    final ArrayList<String> Name = new ArrayList<>();
    if (contacts != null) {
      while (contacts.moveToNext()) {
        map.put(
          contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            .replace("+91", "")
            .replace("-", "")
            .replace(" ", "")
            .replace("#", ""),
          contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            .replaceAll("[-\\[\\]^/,'*:.!><~@#$%+=?|\"\\\\()]", "")
        );

        contactNum.add(contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
          .replace("+91", "")
          .replace("-", "")
          .replace(" ", "")
          .replace("#", ""));
        //Log.d(TAG, "onCreate: new contact" + map.size());
        Name.add(contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
          .replaceAll("[-\\[\\]^/,'*:.!><~@#$%+=?|\"\\\\()]", ""));
      }
      //sortByValue(map);
      userInformation = new UserInformation(map);
      userInformation.setMap(map);
      contacts.close();
    }

    mDatabaseReference.setValue(userInformation.getMap())
      .addOnSuccessListener(new OnSuccessListener<Void>() {
        @Override
        public void onSuccess(Void aVoid) {
//          Toast.makeText(ContactsActivitty.this, "Contacts uploaded successfully!", Toast.LENGTH_SHORT).show();
        }
      })
//this onFailureListener is also optional.
      .addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
          Log.e(TAG, "Error: " + e.getMessage());
          Toast.makeText(ContactsActivitty.this, "Contacts upload failed.", Toast.LENGTH_SHORT).show();
        }
      });

    userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users Mobile");
    userDatabaseReference.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        users = new ArrayList<>();
        friendUID = new ArrayList<>();
        for (DataSnapshot child : dataSnapshot.getChildren()) {
          users.add(child.getKey());
          //friendUID.add(child.getValue().toString());
//          Log.d(TAG, "onDataChange: " + child.getValue().toString());
        }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {

      }
    });

    mDatabaseReference.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        contact = new ArrayList<>();
        nums = new ArrayList<>();
        for (DataSnapshot child : dataSnapshot.getChildren()) {
          if (users.contains(child.getKey())) {
            contact.add(child.getValue().toString());
            nums.add(child.getKey());
          }
          Log.d(TAG, "onDataChange: " + friendUID);
//          Log.d(TAG, "onDataChange: " + child.getValue().toString());
        }

        ArrayAdapter adapter = new ArrayAdapter<>(ContactsActivitty.this, android.R.layout.simple_list_item_1, contact);
        friendsList.setAdapter(adapter);
      }

      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {

      }
    });

    userDatabaseReference.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        for (DataSnapshot child : dataSnapshot.getChildren()) {
          if (nums.contains(child.getKey())) {
            friendUID.add(child.getValue().toString());
          }
//          Log.d(TAG, "onDataChange: " + child.getValue().toString());
        }
        //friendUID.sort();
        Log.d(TAG, "onDataChange: " + friendUID);

      }

      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {

      }
    });

    friendsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        UID = friendUID.get(position);
        Toast.makeText(ContactsActivitty.this, UID, Toast.LENGTH_LONG).show();
        if (!UserDetailActivity.forAccess) {
          startActivity(new Intent(ContactsActivitty.this, FriendProfileActivity.class));
        }

        if (UserDetailActivity.forAccess) {
          Log.d(TAG, "onItemClick: Giving Access");
          accessDatabaseReference = FirebaseDatabase.getInstance().getReference().child(UID).child("My Accesses");
          accessDatabaseReference.child(LocksActivity.tempId).setValue(LocksActivity.tempName);
          UserDetailActivity.forAccess = false;
          finish();
          startActivity(new Intent(ContactsActivitty.this, LocksActivity.class));
        }
      }
    });

    friendsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
      @Override
      public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        UID = friendUID.get(position);
        if (UserDetailActivity.forAccess) {
          Log.d(TAG, "onItemLongClick: Removing Access");
          accessDatabaseReference = FirebaseDatabase.getInstance().getReference().child(UID).child("My Accesses");
          accessDatabaseReference.child(LocksActivity.tempId).removeValue();
          Toast.makeText(ContactsActivitty.this, "Access Removed", Toast.LENGTH_LONG).show();
          UserDetailActivity.forAccess = false;
          finish();
          startActivity(new Intent(ContactsActivitty.this, LocksActivity.class));
        }

        return false;
      }
    });

  }

}

