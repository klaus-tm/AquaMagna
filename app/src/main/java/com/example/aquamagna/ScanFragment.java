package com.example.aquamagna;

import static android.content.res.Configuration.UI_MODE_NIGHT_YES;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.VolumeShaper;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;

@SuppressLint("SetTextI18n")
public class ScanFragment extends Fragment implements BLEReceiveManager.BLECallbacks{
    private final boolean isShowBluetoothDialogCalled = false;
    private boolean scanStarted = false;
    private Button startScan;
    private TextView mainText;
    private TextView phText;
    private TextView turbidityText;
    private TextView conductivityText;
    private TextView messageText;
    private Button newScan;
    private Button saveScan;
    private LinearLayout buttons;
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
    private boolean isDark(Context context){
        int currentMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentMode == UI_MODE_NIGHT_YES;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);
        startScan = view.findViewById(R.id.startScan);
        mainText = view.findViewById(R.id.mainText);
        phText = view.findViewById(R.id.PhText);
        turbidityText = view.findViewById(R.id.TurbidityText);
        conductivityText = view.findViewById(R.id.ConductivityText);
        messageText = view.findViewById(R.id.typeText);
        buttons = view.findViewById(R.id.buttons);
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
                scanStarted = true;
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
        if(!scanStarted){
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
                buttons.setVisibility(View.GONE);
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
                buttons.setVisibility(View.VISIBLE);
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
                if (Math.abs(phThreshold - deviceSensors.getPh()) > 1.5f || Math.abs(deviceSensors.getTurbidity() - turbidityThreshold) > 10.0f || Math.abs(deviceSensors.getConductivity() - conductivityThreshold) > 100.0f) {
                    // Values are far from the standards, set background color to dark red
                    if (isDark(getContext())){
                        setButtons(R.color.md_theme_dark_error, R.color.md_theme_dark_onError);
                        setText("Don't drink it! :(", R.color.md_theme_dark_onErrorContainer, R.color.md_theme_dark_errorContainer);
                    } else {
                        setButtons(R.color.md_theme_light_error, R.color.md_theme_light_onError);
                        setText("Don't drink it! :(", R.color.md_theme_light_onErrorContainer, R.color.md_theme_light_errorContainer);
                    }
                } else if (Math.abs(deviceSensors.getPh() - phThreshold) <= 0.1f && Math.abs(deviceSensors.getTurbidity() - turbidityThreshold) <= 5.0f && Math.abs(deviceSensors.getConductivity() - conductivityThreshold) <= 50.0f) {
                    // Values are close to the standards, set background color to green
                    if (isDark(getContext())){
                        setButtons(R.color.md_theme_dark_primary, R.color.md_theme_dark_onPrimary);
                        setText("Everything looks good! :)", R.color.md_theme_dark_onSurface, R.color.md_theme_dark_surface);
                    } else {
                        setButtons(R.color.md_theme_light_primary, R.color.md_theme_light_onPrimary);
                        setText("Everything looks good! :)", R.color.md_theme_light_onSurface, R.color.md_theme_light_surface);
                    }
                } else {
                    // Values are not far from the standards, set the text color to orange
                    if (isDark(getContext())){
                        setButtons(R.color.md_theme_dark_tertiary, R.color.md_theme_dark_onTertiary);
                        setText("Try to filtrate it before consuming! :/", R.color.md_theme_dark_onTertiaryContainer, R.color.md_theme_dark_tertiaryContainer);
                    } else {
                        setButtons(R.color.md_theme_light_tertiary, R.color.md_theme_light_onTertiary);
                        setText("Try to filtrate it before consuming! :/", R.color.md_theme_light_onTertiaryContainer, R.color.md_theme_light_tertiaryContainer);
                    }
                }
            }
        });
    }

    @Override
    public void showSnackbar() {
        Snackbar.make((CoordinatorLayout)getActivity().findViewById(R.id.coordinator), "Connection closed!", Snackbar.LENGTH_SHORT).setAnchorView((BottomNavigationView)getActivity().findViewById(R.id.bottomNavView)).show();
    }

    private void setButtons(int colorButton, int colorText) {
        saveScan.setBackgroundColor(getResources().getColor(colorButton));
        saveScan.setTextColor(getResources().getColor(colorText));
        newScan.setTextColor(getResources().getColor(colorButton));
    }

    private void setText(String message, int colorText, int colorContainer) {
        messageText.setText(message);
        messageText.setTextColor(getResources().getColor(colorText));
        phText.setTextColor(getResources().getColor(colorText));
        ((TextView) getView().findViewById(R.id.PhStandard)).setTextColor(getResources().getColor(colorText));
        turbidityText.setTextColor(getResources().getColor(colorText));
        ((TextView)getView().findViewById(R.id.TurbidityStandard)).setTextColor(getResources().getColor(colorText));
        conductivityText.setTextColor(getResources().getColor(colorText));
        ((TextView)getView().findViewById(R.id.ConductivityStandard)).setTextColor(getResources().getColor(colorText));
        ((MaterialCardView)getView().findViewById(R.id.card)).setCardBackgroundColor(getResources().getColor(colorContainer));
    }
}