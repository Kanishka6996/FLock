package com.kanishka.flock;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapter extends ArrayAdapter<AvailableLocksActivity.bluetoothList> {

  public ListAdapter(Context context, ArrayList<AvailableLocksActivity.bluetoothList> devices) {
    super(context, 0, devices);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    // Get the data item for this position
    AvailableLocksActivity.bluetoothList device = getItem(position);
    // Check if an existing view is being reused, otherwise inflate the view
    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.custom_row, parent, false);
    }
    // Lookup view for data population
    TextView btName = (TextView) convertView.findViewById(R.id.bt_name);
    TextView btAddress = (TextView) convertView.findViewById(R.id.bt_address);
    // Populate the data into the template view using the data object
    btName.setText(device.deviceName);
    btAddress.setText(device.macAddress);
    // Return the completed view to render on screen
    return convertView;
  }


}
