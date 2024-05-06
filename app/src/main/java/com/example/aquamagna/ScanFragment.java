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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.aquamagna.dataClasses.DeviceSensors;
import com.example.aquamagna.dataClasses.ScanData;
import com.example.aquamagna.dataClasses.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
    private LinearProgressIndicator loadingSave;
    private BluetoothAdapter bluetoothAdapter;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private DatabaseReference userReference;
    private static final String DATABASE_URL = "https://aquamagna-77b9d-default-rtdb.europe-west1.firebasedatabase.app/";
    private String locationString = "";
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
        if (permissions.containsValue(false)) {
            mainText.setText("Grant location and nearby devices permissions in settings!");
            arePermissionsGranted = false;
        } else {
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
        loadingSave = view.findViewById(R.id.loadingSave);
        auth = FirebaseAuth.getInstance();

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

        saveScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttons.setVisibility(View.GONE);
                getLocationForScan(view);
            }
        });
        return view;
    }

    private void getLocationForScan(View view) {
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        loadingSave.setVisibility(View.VISIBLE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                locationString = "latitude: " + latitude + ", longitude: " + longitude;
                locationManager.removeUpdates(this);
                getCompanyFromUser(view);
            }
        };

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    private void getCompanyFromUser(View view) {
        String company = "";
        userReference = FirebaseDatabase.getInstance(DATABASE_URL).getReference("users").child(auth.getUid());
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    company.concat(user.getCompany());
                    saveScanToDatabase(view, company);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Snackbar.make(view, "Unable to get company!", Snackbar.LENGTH_SHORT).setAnchorView((BottomNavigationView)getActivity().findViewById(R.id.bottomNavView)).show();
            }
        });
    }

    private void saveScanToDatabase(View view, String company) {
        String[] ph = phText.getText().toString().split(":");
        String[] turbidity = turbidityText.getText().toString().split(":");
        String[] conductivity = conductivityText.getText().toString().split(":");
        databaseReference = FirebaseDatabase.getInstance(DATABASE_URL).getReference("scans");
        String saveId = databaseReference.push().getKey();
        if (saveId != null){
            Date today = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy, HH:mm:ss", Locale.getDefault());
            databaseReference.child(saveId)
                    .setValue(new ScanData(saveId, dateFormat.format(today), locationString, auth.getCurrentUser().getUid(), company, ph[1].trim(), turbidity[1].trim(), conductivity[1].trim()))
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                loadingSave.setVisibility(View.GONE);
                                buttons.setVisibility(View.VISIBLE);
                                saveScan.setVisibility(View.GONE);
                                Snackbar.make(view, "Scan saved successfully!", Snackbar.LENGTH_SHORT).setAnchorView((BottomNavigationView) getActivity().findViewById(R.id.bottomNavView)).show();
                            }
                            else {
                                loadingSave.setVisibility(View.GONE);
                                Snackbar.make(view, "A problem occured while saving the scan!", Snackbar.LENGTH_SHORT).setAnchorView((BottomNavigationView)getActivity().findViewById(R.id.bottomNavView)).show();
                            }
                        }
                    });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!scanStarted){
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
                float phThreshold = 7.5f;

                if (Math.abs(phThreshold - deviceSensors.getPh()) <= 1f && deviceSensors.getTurbidity() <= 1f && deviceSensors.getConductivity() <= 0.8f) {
                    if (isDark(getContext())){
                        setButtons(R.color.md_theme_dark_primary, R.color.md_theme_dark_onPrimary);
                        setText("Everything looks good! :)", R.color.md_theme_dark_onSurface, R.color.md_theme_dark_surface);
                    } else {
                        setButtons(R.color.md_theme_light_primary, R.color.md_theme_light_onPrimary);
                        setText("Everything looks good! :)", R.color.md_theme_light_onSurface, R.color.md_theme_light_surface);
                    }
                } else if (Math.abs(phThreshold - deviceSensors.getPh()) > 1.5f || deviceSensors.getTurbidity() > 5f || deviceSensors.getConductivity() > 2.5f) {
                    if (isDark(getContext())){
                        setButtons(R.color.md_theme_dark_error, R.color.md_theme_dark_onError);
                        setText("Don't drink it! :(", R.color.md_theme_dark_onErrorContainer, R.color.md_theme_dark_errorContainer);
                    } else {
                        setButtons(R.color.md_theme_light_error, R.color.md_theme_light_onError);
                        setText("Don't drink it! :(", R.color.md_theme_light_onErrorContainer, R.color.md_theme_light_errorContainer);
                    }
                } else {
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
        Snackbar.make((CoordinatorLayout)getActivity().findViewById(R.id.coordinator), "Scan finished successfully!", Snackbar.LENGTH_SHORT).setAnchorView((BottomNavigationView)getActivity().findViewById(R.id.bottomNavView)).show();
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