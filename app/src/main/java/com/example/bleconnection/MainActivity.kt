package com.example.bleconnection

import android.os.Bundle
import android.webkit.PermissionRequest
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.bleconnection.ui.theme.BLEConnectionTheme
import com.example.bleconnection.view.BleDeviceScan
import com.example.bleconnection.view.BlePermissionHandler
import com.example.bleconnection.viewModel.BleViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BLEConnectionTheme {
                var permissionGranted by remember {
                    mutableStateOf(false)
                }

                BlePermissionHandler(onPermissionGranted = {
                    permissionGranted = true
                })
                if (permissionGranted) {
                    BleDeviceScan()
                } else {
                   // PermissionRequest()
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BLEConnectionTheme {
        Greeting("Android")
    }
}