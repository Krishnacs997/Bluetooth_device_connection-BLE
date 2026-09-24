package com.example.bleconnection.viewModel

import android.Manifest
import android.app.Application
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.example.bleconnection.BleManager
import com.example.bleconnection.model.DeviceModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BleViewModel(application: Application): AndroidViewModel(application = application) {
    private val bleManager = BleManager(application)
    private val _device = MutableStateFlow<List<DeviceModel>>(emptyList())
    val device = _device.asStateFlow()
    private val _isScanning = MutableStateFlow(Boolean)
    val isScanning = _isScanning.asStateFlow()

    private val _connectedDevice = MutableStateFlow<DeviceModel?>(null)
    val connectedDevice = _connectedDevice.asStateFlow()

    fun startScan(){
        _device.value = emptyList()
        _isScanning.value.equals(true)
        bleManager.startScan { device ->
            val currentList = _device.value.toMutableList()
            val existingIndex = currentList.indexOfFirst { it.address == device.address }
            if(existingIndex >= 0){
                currentList[existingIndex] = device
            } else {
                currentList.add(device)
            }
            _device.value = currentList.sortedByDescending{it.rssi}
        }
    }

    fun stopScan() {
        bleManager.stopScan()
        _isScanning.value.equals(true)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connect(context: Context,device: DeviceModel) {
        stopScan()
        bleManager.connect(context = context,
            address = device.address
        ) { connected ->

            if (connected) {
                _connectedDevice.value = device
            } else {
                _connectedDevice.value = null
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun disconnect() {
        bleManager.disconnect()
        _connectedDevice.value = null
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCleared() {
        bleManager.stopScan()
        bleManager.disconnect()
        super.onCleared()
    }

}