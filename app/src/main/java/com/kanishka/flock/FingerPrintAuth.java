package com.kanishka.flock;

import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.multidots.fingerprintauth.AuthErrorCodes;
import com.multidots.fingerprintauth.FingerPrintAuthCallback;
import com.multidots.fingerprintauth.FingerPrintAuthHelper;
import com.multidots.fingerprintauth.FingerPrintUtils;

public class FingerPrintAuth extends AppCompatActivity implements FingerPrintAuthCallback {

  private TextView mAuthMsgTv;
  private ViewSwitcher mSwitcher;
  private Button mGoToSettingsBtn;
  private FingerPrintAuthHelper mFingerPrintAuthHelper;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_finger_print_auth);

    mGoToSettingsBtn = (Button) findViewById(R.id.go_to_settings_btn);
    mGoToSettingsBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        FingerPrintUtils.openSecuritySettings(FingerPrintAuth.this);
      }
    });

    mSwitcher = (ViewSwitcher) findViewById(R.id.main_switcher);
    mAuthMsgTv = (TextView) findViewById(R.id.auth_message_tv);

    EditText pinEt = (EditText) findViewById(R.id.pin_et);
    pinEt.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.toString().equals("1234")) {
          Toast.makeText(FingerPrintAuth.this, "Authentication succeeded.", Toast.LENGTH_SHORT).show();
          startActivity(new Intent(FingerPrintAuth.this, AuthSuccessScreen.class));
        }
      }

      @Override
      public void afterTextChanged(Editable s) {

      }
    });

    mFingerPrintAuthHelper = FingerPrintAuthHelper.getHelper(this, this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    mGoToSettingsBtn.setVisibility(View.GONE);

    mAuthMsgTv.setText("Scan your finger");

    //start finger print authentication
    mFingerPrintAuthHelper.startAuth();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mFingerPrintAuthHelper.stopAuth();
  }

  @Override
  public void onNoFingerPrintHardwareFound() {
    mAuthMsgTv.setText("Your device does not have finger print scanner. Please type 1234 to authenticate.");
    mSwitcher.showNext();
  }

  @Override
  public void onNoFingerPrintRegistered() {
    mAuthMsgTv.setText("There are no finger prints registered on this device. Please register your finger from settings.");
    mGoToSettingsBtn.setVisibility(View.VISIBLE);
  }

  @Override
  public void onBelowMarshmallow() {
    mAuthMsgTv.setText("You are running older version of android that does not support finger print authentication. Please type 1234 to authenticate.");
    mSwitcher.showNext();
  }

  @Override
  public void onAuthSuccess(FingerprintManager.CryptoObject cryptoObject) {
    Toast.makeText(FingerPrintAuth.this, "Authentication succeeded.", Toast.LENGTH_SHORT).show();
    startActivity(new Intent(FingerPrintAuth.this, AuthSuccessScreen.class));
  }

  @Override
  public void onAuthFailed(int errorCode, String errorMessage) {
    switch (errorCode) {
      case AuthErrorCodes.CANNOT_RECOGNIZE_ERROR:
        mAuthMsgTv.setText("Cannot recognize your finger print. Please try again.");
        Vibrator v = (Vibrator) getSystemService(this.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
          //deprecated in API 26
          v.vibrate(500);
        }
        break;
      case AuthErrorCodes.NON_RECOVERABLE_ERROR:
        mAuthMsgTv.setText("Cannot initialize finger print authentication. Please type 1234 to authenticate.");
        mSwitcher.showNext();
        break;
      case AuthErrorCodes.RECOVERABLE_ERROR:
        mAuthMsgTv.setText(errorMessage);
        break;
    }
  }
}

