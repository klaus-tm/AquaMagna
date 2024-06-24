package com.example.aquamagna;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.aquamagna.dataClasses.DeviceSensors;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@SuppressLint("MissingPermission")
public class BLEReceiveManager {
    //Lolin32 info (name, UUIDS)
    private static final String DEVICE_NAME = "ESP32";
    private static final UUID SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b");
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");
    private static final UUID CCCD_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");

    //delay handler set to 10s
    private Handler handler = new Handler(Looper.getMainLooper());

    //information received from the fragment(adapter, context, activity-this is initialised with BluetoothManager to allow running on a single thread)
    private final BluetoothAdapter bluetoothAdapter;
    private final Context context;

    //BLECallbacks interface object used for handling UI changes in the fragment
    private BLECallbacks bleCallbacks;
    private final Activity activity;

    //responsable for handling the connection process with the Lolin32 board
    private BluetoothGatt bluetoothGatt;

    //visible progress dialog for the connection procedure
    private ProgressDialog progressDialog;

    /**
     * disconnect handler when the connection ends after 10s
     */
    private Runnable disconnectRunnable = new Runnable() {
        @Override
        public void run() {
            if (bluetoothGatt != null){
                bluetoothGatt.disconnect();
                bluetoothGatt.close();
                bluetoothGatt = null;
                bleCallbacks.showSnackbar();
                stopScanning();
            }
        }
    };

    /**
     * callback used when the scan process ends and checks if the device found is the same with DEVICE_NAME
     */
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if (device != null && device.getName() != null && device.getName().equals(DEVICE_NAME)) {
                connectToDevice(device);
            }
        }
    };

    /**
     * callbacks of the gatt while creating the connection with the found device
     */
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        /**
         * callback used when the device gets connected and searches for the services
         * @param gatt
         * @param status
         * @param newState
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                updateProgress("Discovering services...");
                gatt.discoverServices();
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                handler.removeCallbacksAndMessages(null);
            }
        }

        /**
         * callback used when the services get discovered and searches for characteristic and descriptor with the specified UUIDs
         * @param gatt
         * @param status
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                updateProgress("Enabling notifications...");
                BluetoothGattService service = gatt.getService(SERVICE_UUID);
                if (service != null) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHARACTERISTIC_UUID);
                    if (characteristic != null) {
                        gatt.setCharacteristicNotification(characteristic, true);

                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CCCD_DESCRIPTOR_UUID);
                        if (descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptor);
                        }
                    }
                }
            }
        }

        /**
         * callback used when the characteristic gets discovered.
         * It also gets the data from the server and creates the DeviceSensors object to send them to the fragment using bleCallbacks.onDataFlow
         * @param gatt
         * @param characteristic
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            // Handle characteristic changes here
            String message = characteristic.getStringValue(0);
            if (message != null && !message.isEmpty()) {
                String[] parts = message.split(" ");
                List<Float> parameters = new ArrayList<>();
                for (String part : parts) {
                    parameters.add(Float.parseFloat(part));
                }
                if (parameters.size() >= 3) {
                    DeviceSensors deviceSensors = new DeviceSensors(parameters.get(0), parameters.get(1), parameters.get(2));
                    bleCallbacks.onDataFlow(deviceSensors, activity);
                }
            }

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.hide();
                }
            });
        }
    };

    /**
     * Class constructor.
     * It requires fragment context, activity and callbacks.
     * It initialises the adapter and the progress dialog
     * @param context
     * @param activity
     * @param callbacks
     */
    public BLEReceiveManager(Context context, Activity activity, BLECallbacks callbacks) {
        this.context = context;
        this.activity = activity;

        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

        if (bluetoothManager != null) {
            this.bluetoothAdapter = bluetoothManager.getAdapter();
        } else {
            this.bluetoothAdapter = null;
        }

        this.progressDialog = new ProgressDialog(activity);
        this.progressDialog.setMessage("Connecting to device...");
        this.progressDialog.setCancelable(false);
        this.bleCallbacks = callbacks;
    }

    /**
     * method to start the scan procedure followed by the scanCallback
     */
    public void startScanning() {
        if (bluetoothAdapter != null) {
            progressDialog.show();
            bleCallbacks.onScanStarted(activity);
            bluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback);
        }
    }

    /**
     * method to stop the scan procedure after the device has been found
     */
    public void stopScanning() {
        if (bluetoothAdapter != null) {
            progressDialog.dismiss();
            bleCallbacks.onScanStopped(activity);
            bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
        }
    }

    /**
     * method which starts the connection procedure followed by data receive for 10 seconds
     * @param device
     */
    private void connectToDevice(final BluetoothDevice device) {
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
        }
        bluetoothGatt = device.connectGatt(context, false, gattCallback);
        handler.postDelayed(disconnectRunnable, 10000);
    }

    /**
     * method used to change the message of the progress dialog during the gatt callbacks
     * @param message
     */
    private void updateProgress(String message) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.setMessage(message);
            }
        });
    }

    /**
     * interface used to modify the UI elements in the fragment
     */
    public interface BLECallbacks{
        void onScanStarted(Activity activity);
        void onScanStopped(Activity activity);
        void onDataFlow(DeviceSensors deviceSensors, Activity activity);
        void showSnackbar();
    }
}
