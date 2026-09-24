package com.example.bleconnection

import android.Manifest
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import com.example.bleconnection.model.DeviceModel

class BleManager(context: Context) {
    private val bleManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bleAdapter = bleManager.adapter

    private val scanner: BluetoothLeScanner?
        get() = bleAdapter?.bluetoothLeScanner
    private var bleGatt: BluetoothGatt? = null
    private var scanCallback: ScanCallback? = null

    fun startScan(onDeviceFound: (DeviceModel) -> Unit) {
        val callback = object : ScanCallback() {
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun onScanResult(callbackType: Int, result: ScanResult?) {

                    val device = result!!.device
                    val name = result.scanRecord?.deviceName ?: device.name ?: "Unknown device"
                    val bleDevice = DeviceModel(name= name, address = device.address, rssi = result.rssi)
                    onDeviceFound(bleDevice)

            }

            override fun onScanFailed(errorCode: Int) {
                    Log.e("BLE", "Scan failed :$errorCode")
            }
        }

        scanCallback = callback
        scanner?.startScan(callback)
    }

    fun stopScan() {
        scanCallback?.let {
            scanner?.stopScan(it)
        }

        scanCallback = null
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connect(
        context: Context,
        address: String,
        onConnectionStateChanged: (Boolean) -> Unit
    ) {

        //var context = LocalContext
        val device = bleAdapter.getRemoteDevice(address)
        bleGatt?.close()
        bleGatt = device.connectGatt(context,
                false,
                object : BluetoothGattCallback() {

                    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
                    override fun onConnectionStateChange(
                        gatt: BluetoothGatt,
                        status: Int,
                        newState: Int
                    ) {

                        when (newState) {

                            BluetoothProfile.STATE_CONNECTED -> {

                                Log.d(
                                    "BLE",
                                    "Connected: $address"
                                )

                                onConnectionStateChanged(true)

                                // Discover services
                                gatt.discoverServices()
                            }

                            BluetoothProfile.STATE_DISCONNECTED -> {

                                Log.d(
                                    "BLE",
                                    "Disconnected"
                                )

                                onConnectionStateChanged(false)
                            }
                        }
                    }

                    override fun onServicesDiscovered(
                        gatt: BluetoothGatt,
                        status: Int
                    ) {

                        if (status ==
                            BluetoothGatt.GATT_SUCCESS
                        ) {

                            Log.d(
                                "BLE",
                                "Services discovered"
                            )

                            for (service in gatt.services) {

                                Log.d(
                                    "BLE",
                                    "Service: ${service.uuid}"
                                )

                                for (characteristic in
                                service.characteristics
                                ) {

                                    Log.d(
                                        "BLE",
                                        "Characteristic: ${characteristic.uuid}"
                                    )
                                }
                            }
                        }
                    }
                }
            )
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun disconnect() {

        bleGatt?.disconnect()

        bleGatt?.close()

        bleGatt = null
    }

}