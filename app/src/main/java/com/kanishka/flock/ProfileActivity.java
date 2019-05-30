package com.kanishka.flock;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

  private static final String TAG = "ProfileActivity";
  private static final int PICK_IMAGE_REQUEST = 123;
  private FirebaseAuth mAuth;
  private TextView userId;
  private ImageView userImage;

  private FirebaseUser user;
  private DatabaseReference mDatabaseReference, usersReference;
  private EditText userFirstName, userLastName, userMobile;
  private Button saveInfo;
  private Uri filePath;
  private Bitmap output;
  private byte[] data;
  private ArrayList<String> users;
  boolean temp = true;


  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_profile);

    mAuth = FirebaseAuth.getInstance();
    user = mAuth.getCurrentUser();
    if (user == null) {
      finish();
      startActivity(new Intent(this, LoginActivity.class));
    }

    mDatabaseReference = FirebaseDatabase.getInstance().getReference();

    userFirstName = findViewById(R.id.user_first_name);
    userLastName = findViewById(R.id.user_last_name);
    saveInfo = findViewById(R.id.user_save_info);
    userImage = findViewById(R.id.user_image);
    userMobile = findViewById(R.id.user_mobile);

    FirebaseUser user = mAuth.getCurrentUser();

    userId = findViewById(R.id.user_email);
    if (user.getEmail() != null) {
      userId.setText("Welcome " + user.getEmail());
    }
    if (user.getPhoneNumber() != null) {
      userId.setText("Welcome " + user.getPhoneNumber());
      userMobile.setText(user.getPhoneNumber());
    }

    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mDatabaseReference = database.getReference();
    if (!MainActivity.firstLogin) {
      Log.d(TAG, "onCreate: Already Name added");
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
            //Bitmap.createScaledBitmap(bitmap,180,180,true);
            getRoundedCroppedBitmap(bitmap);
            userImage.setImageBitmap(output);
            userImage.setMaxHeight(160);
            userImage.setMaxWidth(160);
          }
        }).addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception exception) {
          }
        });
      } catch (IOException e) {
      }
    }


    saveInfo.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        MainActivity.firstLogin = false;
        saveUserInformation();

      }
    });

    userImage.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        showFileChooser();

      }
    });

  }

  private void saveUserInformation() {
    String fName = userFirstName.getText().toString().trim();
    String lName = userLastName.getText().toString().trim();
    final String mobile = userMobile.getText().toString().trim();

    if (TextUtils.isEmpty(fName)) {
      //Toast.makeText(this, "You must enter First Name", Toast.LENGTH_SHORT).show();
      userFirstName.setError("You must enter First Name");
      return;
    }

    if (TextUtils.isEmpty(lName)) {
      //Toast.makeText(this, "You must enter Last Name", Toast.LENGTH_SHORT).show();
      userLastName.setError("You must enter Last Name");
      return;
    }

    if (TextUtils.isEmpty(mobile) || (mobile.length() < 10)) {
      //Toast.makeText(this, "You must enter Mobile", Toast.LENGTH_SHORT).show();
      userMobile.setError("You must enter valid Mobile Number");
      return;
    }

    usersReference = FirebaseDatabase.getInstance().getReference().child("Users Mobile");
    usersReference.child(mobile.replace("+91", "")).setValue(user.getUid());

    UserInformation userInformation = new UserInformation(fName, lName, mobile);
    user = mAuth.getCurrentUser();
    mDatabaseReference.child(user.getUid()).setValue(userInformation);

    if (filePath != null) {
      //displaying a progress dialog while upload is going on
      final ProgressDialog progressDialog = new ProgressDialog(this);
      progressDialog.setTitle("Uploading");
      progressDialog.show();

      FirebaseStorage storage = FirebaseStorage.getInstance();
      StorageReference storageReference = storage.getReference();
      StorageReference riversRef = storageReference.child(user.getUid());
      riversRef.putFile(filePath)
        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
          @Override
          public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            //if the upload is successfull
            //hiding the progress dialog
            //progressDialog.dismiss();
//            finish();
//            startActivity(new Intent(ProfileActivity.this, UserDetailActivity.class));
            //and displaying a success toast
            Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();
          }
        })
        .addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception exception) {
            //if the upload is not successfull
            //hiding the progress dialog
            progressDialog.dismiss();

            //and displaying error message
            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
          }
        })
        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
          @Override
          public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
            //calculating progress percentage
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

            //displaying percentage in progress dialog
            progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
            if (progress == 100) {
              finish();
              startActivity(new Intent(ProfileActivity.this, UserDetailActivity.class));
            }
          }
        });
    }
    //if there is not any file
    else {
      //you can display an error toast
    }

    Toast.makeText(this, "Information Saved.", Toast.LENGTH_LONG).show();
    //temp = true;

  }

  private void showFileChooser() {
    Intent intent = new Intent();
    intent.setType("image/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);
    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == UCrop.RESULT_ERROR) {
      Toast.makeText(this, "uCrop error", Toast.LENGTH_SHORT).show();
      return;
    }

    if (requestCode == UCrop.REQUEST_CROP) {
      filePath = UCrop.getOutput(data);
      //Toast.makeText(this, imgUri.getPath(), Toast.LENGTH_SHORT).show();
      return;
    }

    if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

      filePath = data.getData();
      File tempCropped = new File(getCacheDir(), "tempImgCropped.png");
      Uri destinationUri = Uri.fromFile(tempCropped);
      UCrop.of(filePath, destinationUri)
        .withAspectRatio(1, 1)
        .withMaxResultSize(500, 500)
        .start(this);

      try {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
        getRoundedCroppedBitmap(bitmap);
        userImage.setImageBitmap(output);
        userImage.setMaxWidth(160);
        userImage.setMaxHeight(160);

      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void showData(DataSnapshot dataSnapshot) {

    Log.d(TAG, "showData: data is not null");
    UserInformation userInformation = new UserInformation();
    userInformation.setFirstName(dataSnapshot.child(user.getUid()).getValue(UserInformation.class).getFirstName());
    userInformation.setLastName(dataSnapshot.child(user.getUid()).getValue(UserInformation.class).getLastName());
    userInformation.setMobile(dataSnapshot.child(user.getUid()).getValue(UserInformation.class).getMobile());

    userMobile.setText(userInformation.mobile);
    userFirstName.setText(userInformation.firstName);
    userLastName.setText(userInformation.lastName);

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

