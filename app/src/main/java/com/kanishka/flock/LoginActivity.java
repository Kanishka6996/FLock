package com.kanishka.flock;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

  private Button buttonLogin;
  private EditText emailLogin;
  private EditText passLogin;
  private TextView signUpLogin;

  private ProgressDialog mProgressDialog;
  private FirebaseAuth mAuth;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    mAuth = FirebaseAuth.getInstance();

    if (mAuth.getCurrentUser() != null) {
      finish();
      Intent i = new Intent(getApplicationContext(), AvailableLocksActivity.class);
      startActivity(i);
    }

    buttonLogin = findViewById(R.id.login_button);
    emailLogin = findViewById(R.id.login_email);
    passLogin = findViewById(R.id.login_password);
    signUpLogin = findViewById(R.id.login_signUP);

    mProgressDialog = new ProgressDialog(this);

    buttonLogin.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        userLogin();
      }
    });

    signUpLogin.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        finish();
        Intent i = new Intent(getBaseContext(), MainActivity.class);
        startActivity(i);
      }
    });
  }

  private void userLogin() {

    String email = emailLogin.getText().toString().trim();
    String password = passLogin.getText().toString().trim();

    if (TextUtils.isEmpty(email)) {
      Toast.makeText(this, "You must enter email to login", Toast.LENGTH_SHORT).show();
      return;
    }

    if (TextUtils.isEmpty(password)) {
      Toast.makeText(this, "You must enter password to login", Toast.LENGTH_SHORT).show();
      return;
    }

    //mProgressDialog.setMessage("Signing In...");
    //mProgressDialog.show();

    mAuth.signInWithEmailAndPassword(email, password)
      .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
          if (task.isSuccessful()) {
            Toast.makeText(LoginActivity.this, "Sign In Successfully.", Toast.LENGTH_LONG).show();
            finish();
            Intent i = new Intent(getApplicationContext(), AvailableLocksActivity.class);
            startActivity(i);
          } else {
            Toast.makeText(LoginActivity.this, "Sign In Failed, Check details again.", Toast.LENGTH_LONG).show();
          }
        }
      });
  }
}
