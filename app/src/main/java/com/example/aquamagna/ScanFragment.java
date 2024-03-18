package com.example.aquamagna;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
@SuppressLint("SetTextI18n")
public class ScanFragment extends Fragment implements BLEReceiveManager.BLECallbacks{
    private final boolean isShowBluetoothDialogCalled = false;
    private Button startScan;
    private TextView mainText;
    private TextView phText;
    private TextView turbidityText;
    private TextView conductivityText;
    private TextView messageText;
    private Button newScan;
    private Button saveScan;
    private BluetoothAdapter bluetoothAdapter;
    private boolean isBluetoothSupported = false;
    private boolean arePermissionsGranted = false;
    private boolean isBluetoothEnabled = false;

    private final ActivityResultLauncher<Intent> startBluetoothIntentForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != Activity.RESULT_OK){
            isBluetoothEnabled = false;
            mainText.setText("Please enable bluetooth and location from the notification panel!");
        }
        else
            isBluetoothEnabled = true;
        updateStartScanButtonVisibility();
    });

    private final ActivityResultLauncher<String[]> bluetoothPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
        // Check if all permissions are granted
        if (permissions.containsValue(false)) {
            // Permission denied, handle accordingly (e.g., show a message to the user)
            mainText.setText("Grant location and nearby devices permissions in settings!");
            arePermissionsGranted = false;
        } else {
            // All permissions granted, proceed with the logic
            arePermissionsGranted = true;
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startBluetoothIntentForResult.launch(enableBTIntent);
            } else {
                isBluetoothEnabled = true;
            }
        }
        updateStartScanButtonVisibility();
    });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);
        startScan = view.findViewById(R.id.startScan);
        mainText = view.findViewById(R.id.mainText);
        phText = view.findViewById(R.id.PhText);
        turbidityText = view.findViewById(R.id.TurbidityText);
        conductivityText = view.findViewById(R.id.ConductivityText);
        messageText = view.findViewById(R.id.typeText);
        saveScan = view.findViewById(R.id.saveScan);
        newScan = view.findViewById(R.id.newScan);

        mainText.setText("");
        startScan.setVisibility(View.GONE);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        super.onCreate(savedInstanceState);
        if (!isShowBluetoothDialogCalled){
            showBluetoothDialog();
        }
        BLEReceiveManager bleReceiveManager = new BLEReceiveManager(getContext(), getActivity(), this);

        startScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bleReceiveManager.startScanning();
            }
        });

        newScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bleReceiveManager.startScanning();
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check for permissions update here
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)
                arePermissionsGranted = true;
        }
        else {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED)
                arePermissionsGranted = true;
        }
        updateStartScanButtonVisibility();
    }

    public void showBluetoothDialog() {
        if (bluetoothAdapter == null){
            mainText.setText("Device doesn't support Bluetooth :(");
            isBluetoothSupported = false;
        } else {
            isBluetoothSupported = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                bluetoothPermissionLauncher.launch(new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.BLUETOOTH_SCAN});
            } else {
                bluetoothPermissionLauncher.launch(new String[]{Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
            }
        }
    }
    private void updateStartScanButtonVisibility() {
        if (isBluetoothSupported && arePermissionsGranted && isBluetoothEnabled) {
            startScan.setVisibility(View.VISIBLE);
            mainText.setText("Please make sure to start the AquaMagna scanning device");
        } else {
            startScan.setVisibility(View.GONE);
        }
    }

    @Override
    public void onScanStarted(Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainText.setVisibility(View.GONE);
                mainText.setText("");
                messageText.setText("");
                messageText.setVisibility(View.GONE);
                newScan.setVisibility(View.GONE);
                saveScan.setVisibility(View.GONE);
                startScan.setVisibility(View.GONE);
                phText.setVisibility(View.VISIBLE);
                turbidityText.setVisibility(View.VISIBLE);
                conductivityText.setVisibility(View.VISIBLE);
                getView().findViewById(R.id.PhStandard).setVisibility(View.VISIBLE);
                getView().findViewById(R.id.TurbidityStandard).setVisibility(View.VISIBLE);
                getView().findViewById(R.id.ConductivityStandard).setVisibility(View.VISIBLE);
                phText.setText("pH: ");
                turbidityText.setText("Turbidity: ");
                conductivityText.setText("Conductivity: ");
            }
        });
    }

    @Override
    public void onScanStopped(Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                newScan.setVisibility(View.VISIBLE);
                saveScan.setVisibility(View.VISIBLE);
                messageText.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public void onDataFlow(DeviceSensors deviceSensors, Activity activity) {
        if (!isAdded())
            return;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                phText.setText("pH: " + deviceSensors.getPh().toString());
                turbidityText.setText("Turbidity: " + deviceSensors.getTurbidity().toString());
                conductivityText.setText("Conductivity: " + deviceSensors.getConductivity().toString());
                float phThreshold = 7.0f;
                float turbidityThreshold = 1.0f;
                float conductivityThreshold = 800.0f;

                // Check if the values are far from the standards
                if (Math.abs(deviceSensors.getPh() - phThreshold) > 1.0f || Math.abs(deviceSensors.getTurbidity() - turbidityThreshold) > 10.0f || Math.abs(deviceSensors.getConductivity() - conductivityThreshold) > 100.0f) {
                    // Values are far from the standards, set background color to dark red
                    messageText.setText("Don't drink it! :(");
                    messageText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    phText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    ((TextView) getView().findViewById(R.id.PhStandard)).setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    turbidityText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    ((TextView)getView().findViewById(R.id.TurbidityStandard)).setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    conductivityText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    ((TextView)getView().findViewById(R.id.ConductivityStandard)).setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    saveScan.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                    newScan.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));

                } else if (Math.abs(deviceSensors.getPh() - phThreshold) <= 0.5f && Math.abs(deviceSensors.getTurbidity() - turbidityThreshold) <= 5.0f && Math.abs(deviceSensors.getConductivity() - conductivityThreshold) <= 50.0f) {
                    // Values are close to the standards, set background color to green
                    messageText.setText("Everything looks good! :)");
                    messageText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    phText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    ((TextView) getView().findViewById(R.id.PhStandard)).setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    turbidityText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    ((TextView)getView().findViewById(R.id.TurbidityStandard)).setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    conductivityText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    ((TextView)getView().findViewById(R.id.ConductivityStandard)).setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    saveScan.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                    newScan.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));

                } else {
                    // Values are not far from the standards, set the text color to orange
                    messageText.setText("Try to filtrate it before consuming! :/");
                    messageText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                    phText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                    ((TextView) getView().findViewById(R.id.PhStandard)).setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                    turbidityText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                    ((TextView)getView().findViewById(R.id.TurbidityStandard)).setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                    conductivityText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                    ((TextView)getView().findViewById(R.id.ConductivityStandard)).setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                    saveScan.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
                    newScan.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
                }
            }
        });
    }
}