package com.kanishka.flock;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";

  private Button buttonRegister, buttonMobile;
  private EditText emailRegister;
  private EditText passRegister;
  private TextView signInRegister;

  private ProgressDialog mProgressDialog;
  private FirebaseAuth mAuth;
  public static boolean firstLogin = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mAuth = FirebaseAuth.getInstance();

    if (mAuth.getCurrentUser() != null) {
      finish();
      Intent i = new Intent(getApplicationContext(), AvailableLocksActivity.class);
      startActivity(i);
    }

    buttonRegister = findViewById(R.id.auth_register);
    emailRegister = findViewById(R.id.auth_email);
    passRegister = findViewById(R.id.auth_password);
    signInRegister = findViewById(R.id.auth_signIn);
    buttonMobile = findViewById(R.id.auth_mobile);

    mProgressDialog = new ProgressDialog(this);

    buttonRegister.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        registerUser();
      }
    });

    signInRegister.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        finish();
        Intent i = new Intent(getBaseContext(), LoginActivity.class);
        startActivity(i);
      }
    });

    buttonMobile.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(getBaseContext(), MobileLoginActivity.class));
      }
    });

  }

  private void registerUser() {

    String email = emailRegister.getText().toString().trim();
    String password = passRegister.getText().toString().trim();

    if (TextUtils.isEmpty(email)) {
      Toast.makeText(this, "You must enter email to login", Toast.LENGTH_SHORT).show();
      return;
    }

    if (TextUtils.isEmpty(password)) {
      Toast.makeText(this, "You must enter password to login", Toast.LENGTH_SHORT).show();
      return;
    }

    mProgressDialog.setMessage("Registering User...");
    mProgressDialog.show();

    mAuth.createUserWithEmailAndPassword(email, password)
      .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
          if (task.isSuccessful()) {
            Toast.makeText(MainActivity.this, "You are registered successfully.", Toast.LENGTH_LONG).show();
            firstLogin = true;
            finish();
            Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
            startActivity(i);
          } else {
            Log.d(TAG, "onComplete: " + task.getException().getMessage());
            Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
          }
        }
      });
    mProgressDialog.hide();
  }
}

