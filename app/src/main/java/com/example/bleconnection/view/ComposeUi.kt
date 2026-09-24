package com.example.bleconnection.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.R
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bleconnection.model.DeviceModel
import com.example.bleconnection.viewModel.BleViewModel
import java.nio.file.WatchEvent
import kotlin.io.encoding.Base64

@Composable
fun BleDeviceScan(viewModel: BleViewModel = viewModel()){
    val devices by viewModel.device.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val connectedDevice by viewModel.connectedDevice.collectAsState()

    var context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Text(text = "BLE device", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        connectedDevice?.let { device ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Connected",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = device.name
                    )

                    Text(
                        text = device.address
                    )

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    Button(onClick = {viewModel.disconnect()}) {
                        Text("Disconnected")
                    }
                }
            }
            Spacer(
                modifier = Modifier.height(16.dp)
            )
        }

        Button(
            onClick = {

                if (isScanning.equals(true)) {
                    viewModel.stopScan()
                } else {
                    viewModel.startScan()
                }

            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                if (isScanning.equals(true))
                    "Stop Scan"
                else
                    "Scan BLE Devices"
            )
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        if (isScanning.equals(true)) {

            CircularProgressIndicator(
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {

            items(
                items = devices,
                key = {
                    it.address
                }
            ) { device ->

                BleDeviceItem(
                    device = device,
                    onClick = {
                        viewModel.connect(context = context, device)
                    }
                )
            }
        }

    }
}

@Composable
fun BleDeviceItem(
    device: DeviceModel,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable {
                onClick()
            }
    ) {

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

           /* Icon(
                imageVector = ,
                contentDescription = "Bluetooth"
            )*/

            Spacer(
                modifier = Modifier.width(16.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = device.name,
                    style =
                        MaterialTheme.typography.titleMedium
                )

                Text(
                    text = device.address,
                    style =
                        MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "RSSI: ${device.rssi} dBm",
                    style =
                        MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = "Connect",
                style =
                    MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun BlePermissionHandler(
    onPermissionGranted: () -> Unit
) {

    val context = LocalContext.current
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )

        } else {

            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            val granted = result.values.all {
                    it
            }

            if (granted) {
                onPermissionGranted()
            }
        }

    LaunchedEffect(Unit) {
        val allGranted = permissions.all {
                ContextCompat.checkSelfPermission(
                    context,
                    it
                ) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            onPermissionGranted()
        } else {
            launcher.launch(permissions)
        }
    }
}