package com.kanishka.flock;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class FriendProfileActivity extends AppCompatActivity {

  private static final String TAG = "FriendProfileActivity";

  TextView fName, fId;
  FirebaseUser user;
  FirebaseAuth mAuth;
  DatabaseReference friendDatabsaeReference, mDatabaseReference, aDatabaseReference;
  String firstName, lastName;
  ImageView fImage;
  Bitmap output;
  ArrayList<String> mlockName, mlockId, flockName, flockId;
  ListView commonAcceses;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_friend_profile);

    fName = findViewById(R.id.friend_name);
    fId = findViewById(R.id.friend_id);
    fImage = findViewById(R.id.friend_image);
    commonAcceses = findViewById(R.id.friend_common_access_list);

    mAuth = FirebaseAuth.getInstance();
    user = mAuth.getCurrentUser();


    fId.setText(ContactsActivitty.UID);

    friendDatabsaeReference = FirebaseDatabase.getInstance().getReference().child(ContactsActivitty.UID);
    friendDatabsaeReference.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        for (DataSnapshot child : dataSnapshot.getChildren()) {
          Log.d(TAG, child.getKey());
          if (child.getKey().equals("firstName")) {
            firstName = child.getValue().toString();
            Log.d(TAG, "onDataChange: " + child.getValue());
          }
          if (child.getKey().equals("lastName")) {
            lastName = child.getValue().toString();
            Log.d(TAG, "onDataChange: " + child.getValue());
          }
        }
        fName.setText(firstName + " " + lastName);
      }

      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {

      }
    });

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference mStorageReference = storage.getReference(ContactsActivitty.UID);

    try {
      final File localFile1 = File.createTempFile("images", "jpg");
      Log.d(TAG, "onCreate: " + localFile1);
      mStorageReference.
        getFile(localFile1).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
        @Override
        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
          Bitmap bitmap = BitmapFactory.decodeFile(localFile1.getAbsolutePath());
          Log.d(TAG, "onSuccess: image - " + bitmap);
          getRoundedCroppedBitmap(bitmap);
          fImage.setImageBitmap(output);
          fImage.setMaxHeight(160);
          fImage.setMaxWidth(160);
//            profileImage.setBackground(getResources().getDrawable(R.drawable.rounded_corner));
//            profileImage.setCropToPadding(true);


        }
      }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception exception) {
        }
      });
    } catch (IOException e) {

    } catch (NullPointerException e) {
      Log.e(TAG, "onCreate: ", e);
    }

    mDatabaseReference = FirebaseDatabase.getInstance().getReference().child(user.getUid()).child("My Accesses");
    mDatabaseReference.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        mlockName = new ArrayList<>();
        mlockId = new ArrayList<>();
        for (DataSnapshot child : dataSnapshot.getChildren()) {
          mlockName.add(child.getValue().toString());
          mlockId.add(child.getKey());
          Log.d(TAG, "onDataChange: " + mlockName);
        }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {

      }
    });

    aDatabaseReference = FirebaseDatabase.getInstance().getReference().child(ContactsActivitty.UID).child("My Accesses");
    aDatabaseReference.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        flockName = new ArrayList<>();
        flockId = new ArrayList<>();
        for (DataSnapshot child : dataSnapshot.getChildren()) {
          if (mlockId.contains(child.getKey())) {
            flockName.add(child.getValue().toString());
            flockId.add(child.getKey());
          }
        }
        if (flockName == null) {
          flockName.add("No common accesses yet.");
        }
        ArrayAdapter adapter = new ArrayAdapter<>(FriendProfileActivity.this, android.R.layout.simple_list_item_1, flockName);
        commonAcceses.setAdapter(adapter);
      }

      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {

      }
    });

  }

  private Bitmap getRoundedCroppedBitmap(Bitmap bitmap) {
    int widthLight = bitmap.getWidth();
    int heightLight = bitmap.getHeight();

    output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

    Canvas canvas = new Canvas(output);
    Paint paintColor = new Paint();
    paintColor.setFlags(Paint.ANTI_ALIAS_FLAG);

    RectF rectF = new RectF(new Rect(0, 0, widthLight, heightLight));

    canvas.drawRoundRect(rectF, widthLight, widthLight, paintColor);

    Paint paintImage = new Paint();
    paintImage.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
    canvas.drawBitmap(bitmap, 0, 0, paintImage);

    return output;
  }
}
