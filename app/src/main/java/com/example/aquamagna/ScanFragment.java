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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
@SuppressLint("SetTextI18n")
public class ScanFragment extends Fragment {
    private final boolean isShowBluetoothDialogCalled = false;
    private Button startScan;
    private TextView mainText;
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

        mainText.setText("");
        startScan.setVisibility(View.GONE);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        super.onCreate(savedInstanceState);
        if (!isShowBluetoothDialogCalled){
            showBluetoothDialog();
        }

        startScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BLEReceiveManager bleReceiveManager = new BLEReceiveManager(getContext(), getActivity());
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
}