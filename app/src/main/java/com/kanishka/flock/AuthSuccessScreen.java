package com.kanishka.flock;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class AuthSuccessScreen extends AppCompatActivity {
  private static final String TAG = "AuthSuccessScreen";

  ImageView lock;
  AvailableLocksActivity main = new AvailableLocksActivity();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_auth_success_screen);

    lock = findViewById(R.id.lock_icon);

    main.sendDataToPairedDevice('1', main.mac);
    Log.d(TAG, "onCreate: MAC :" + main.mac);

    lock.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        main.sendDataToPairedDevice('0', main.mac);
        finish();
        Intent i = new Intent(getBaseContext(), AvailableLocksActivity.class);
        startActivity(i);
      }
    });
  }
}

