package com.kanishka.flock;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

import static android.Manifest.permission.READ_CONTACTS;

public class UserDetailActivity extends AppCompatActivity {

  private static final int REQUEST_CODE_READ_CONTACTS = 1;
  private static final String TAG = "UserDetailActivity";

  private ImageView profileImage;
  private TextView profileFname, mobile, profileId;
  private FirebaseAuth mAuth;
  private FirebaseUser user;
  private Button logOut, editProfile, friends, locks;
  private Bitmap output;
  public static boolean forAccess = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_user_detail);

    int hasReadContactPermission = ContextCompat.checkSelfPermission(this, READ_CONTACTS);
    Log.d(TAG, "onCreate: checkSelfPermission = " + hasReadContactPermission);

    if (hasReadContactPermission != PackageManager.PERMISSION_GRANTED) {
      Log.d(TAG, "onCreate: requesting permission");
      ActivityCompat.requestPermissions(this, new String[]{READ_CONTACTS}, REQUEST_CODE_READ_CONTACTS);
    }

    profileImage = findViewById(R.id.friend_image);
    profileFname = findViewById(R.id.profile_fname);
    profileId = findViewById(R.id.profile_id);
    logOut = findViewById(R.id.profile_logout);
    editProfile = findViewById(R.id.profile_edit);
    mobile = findViewById(R.id.profile_mobile);
    friends = findViewById(R.id.profile_friends);
    locks = findViewById(R.id.profile_locks);

    mAuth = FirebaseAuth.getInstance();
    user = mAuth.getCurrentUser();

    //storageReference.child(user.getUid()).getFile();
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mDatabaseReference = database.getReference();

    if (user != null) {
      profileId.setText("ID - " + user.getUid());
      //mDatabaseReference.child(user.getUid());
      mDatabaseReference.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
          showData(dataSnapshot);

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
      });

      FirebaseStorage storage = FirebaseStorage.getInstance();
      StorageReference storageReference = storage.getReference().child(user.getUid());

      try {
        final File localFile = File.createTempFile("images", "jpg");
        storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
          @Override
          public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
            getRoundedCroppedBitmap(bitmap);
            profileImage.setImageBitmap(output);
            profileImage.setMaxHeight(160);
            profileImage.setMaxWidth(160);
//            profileImage.setBackground(getResources().getDrawable(R.drawable.rounded_corner));
//            profileImage.setCropToPadding(true);


          }
        }).addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception exception) {
          }
        });
      } catch (IOException e) {
      }
    }

    logOut.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mAuth.signOut();
        finish();
        startActivity(new Intent(UserDetailActivity.this, LoginActivity.class));

      }
    });

    editProfile.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(UserDetailActivity.this, ProfileActivity.class));
      }
    });

    friends.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        forAccess = false;
        startActivity(new Intent(UserDetailActivity.this, ContactsActivitty.class));
      }
    });

    locks.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(UserDetailActivity.this, LocksActivity.class));
      }
    });

  }

  private void showData(DataSnapshot dataSnapshot) {

    UserInformation userInformation = new UserInformation();

    userInformation.setFirstName(dataSnapshot.child(user.getUid()).getValue(UserInformation.class).getFirstName());
    userInformation.setLastName(dataSnapshot.child(user.getUid()).getValue(UserInformation.class).getLastName());
    userInformation.setMobile(dataSnapshot.child(user.getUid()).getValue(UserInformation.class).getMobile());
    profileFname.setText(userInformation.firstName + " " + userInformation.lastName);
    mobile.setText("Mobile - " + userInformation.mobile);

    //profileLname.setText(userInformation.lastName);


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
