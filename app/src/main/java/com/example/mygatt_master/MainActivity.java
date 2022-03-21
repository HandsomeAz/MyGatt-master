package com.example.mygatt_master;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "BLE_TEST";

    private String UUID_service = "0000FFE0-0000-1000-8000-00805F9B34FB";

    private String UUID_characteristic ="0000FFE1-0000-1000-8000-00805F9B34FB";

    private BluetoothGatt mBluetoothGatt;

    private BluetoothGattCharacteristic mCharacteristic;

    private BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {

        @Override

        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            if (newState == BluetoothGatt.STATE_CONNECTED) {

                gatt.discoverServices();//四.连接蓝牙成功之后，发现服务

            }

            super.onConnectionStateChange(gatt, status, newState);

        }

        @Override

        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            if (status == BluetoothGatt.GATT_SUCCESS){ //五.发现服务成功之后，去找需要的特征值

                UUID serviceUUID = UUID.fromString(UUID_service);

                UUID characteristicUUID = UUID.fromString(UUID_characteristic);

                BluetoothGattService service = mBluetoothGatt.getService(serviceUUID);

                mCharacteristic = service.getCharacteristic(characteristicUUID); //找到特征值之后进行收发操作，设置接收特征值通知

                mBluetoothGatt.setCharacteristicNotification(mCharacteristic, true);

                send(); //发个消息给蓝牙

            }

            super.onServicesDiscovered(gatt, status);

        }

        @Override

        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            super.onCharacteristicRead(gatt, characteristic, status);

//            Log.i(TAG,"onCharacteristicRead" + ByteHelper.BytesToHexString(characteristic.getValue()));

        }

        @Override

        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

//            super.onCharacteristicWrite(gatt, characteristic, status);      Log.i(TAG,"onCharacteristicWrite:"+ByteHelper.BytesToHexString(characteristic.getValue()));

        }

        @Override

        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {

            super.onCharacteristicChanged(gatt, characteristic);

//            Log.i(TAG,"onCharacteristicChanged"+ByteHelper.BytesToHexString(characteristic.getValue()));

        }

    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            int checkResult = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

            if (checkResult== PackageManager.PERMISSION_DENIED){

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            }

        }

        initBluetooth();

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initBluetooth() {

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        bluetoothAdapter.enable();//一.打开蓝牙

        final BluetoothLeScanner bluetoothLeScanner =bluetoothAdapter.getBluetoothLeScanner();

        bluetoothLeScanner.startScan(new ScanCallback() {//二.搜索蓝牙

            @Override

            public void onScanResult(int callbackType, ScanResult result) {

                super.onScanResult(callbackType, result);

                if ("我的蓝牙".equals(result.getDevice().getName())){//三.连接蓝牙

                    mBluetoothGatt = result.getDevice().connectGatt(getApplicationContext(), false, mBluetoothGattCallback);

                    bluetoothLeScanner.stopScan(this);

                }

            }

        });

    }

    private void send() {

        byte[] mes = new byte[4];

        mes[0] = (byte) 0xAB;

        mes[1] = (byte) 0xA8;

        mes[2] = (byte) 0x58;

        mes[3] = (byte) 0xFE;

        mCharacteristic.setValue(mes);

        mBluetoothGatt.writeCharacteristic(mCharacteristic);

    }

}