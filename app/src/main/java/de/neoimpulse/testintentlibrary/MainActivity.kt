package de.neoimpulse.testintentlibrary

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nimmsta.intent_library.NIMMSTADevice
import com.nimmsta.intent_library.NIMMSTADeviceEventHandler
import com.nimmsta.intent_library.NIMMSTAIntentConnection
import com.nimmsta.intent_library.NIMMSTAManager
import com.nimmsta.intent_library.models.general.Barcode
import de.neoimpulse.testintentlibrary.databinding.ActivityMainBinding

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), NIMMSTADeviceEventHandler {

    private lateinit var nimmstaDevice: NIMMSTADevice
    private lateinit var nimmstaConnection: NIMMSTAIntentConnection
    private lateinit var nimmstaManager: NIMMSTAManager
    private lateinit var binding: ActivityMainBinding

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
        if (this::nimmstaManager.isInitialized && this::nimmstaConnection.isInitialized) {
            Log.d(TAG, "disconnect from NIMMSTA")
            nimmstaManager.cancelAllConnect()
            nimmstaConnection.close()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // connect to Nimmsta intent library
        NIMMSTAIntentConnection.bindServiceToActivity(this).onComplete { it ->
            // connection is ready
            Log.d(TAG, "Nimmsta connection is ready")
            nimmstaConnection = it.result
            nimmstaManager = nimmstaConnection.manager!!

            // get device
            getDevice()

        }.onError { error ->
            Log.e(TAG, "Error occurred: $error", error)
            // error occurred
        }.onSuccess { connection ->
            // connection is ready}
            Log.d(TAG, "Nimmsta connection is ready")
            nimmstaConnection = connection
            nimmstaManager = connection.manager!!
        }

        binding.btnResetCounter.setOnClickListener {
            binding.tvCounter.text = "0"
        }
    }

    private fun getDevice(): NIMMSTADevice? {
        Log.d(TAG, "get device")
        var device = nimmstaManager.devices.firstOrNull()
        if (device != null) {
            Log.d(TAG, "device not null")
            nimmstaDevice = device
            Log.d(TAG, "Device serial number: ${device.serialNumber}")
            runOnUiThread {
                binding.tvDeviceSerial.text = device.serialNumber
            }
            return nimmstaDevice
        } else {
            Log.d(TAG, "device is null")
            runOnUiThread {
                binding.tvDeviceSerial.text = "No device connected"
            }
            return null
        }
    }

    override fun didScanBarcode(device: NIMMSTADevice, barcode: Barcode) {
        Log.d(TAG, "Barcode scanned: ${barcode.barcode}")
        binding.tvLastScan.text = barcode.barcode
        Log.d(TAG, "Add 1 to counter")
        runOnUiThread {
            binding.tvCounter.text = (binding.tvCounter.text.toString().toInt() + 1).toString()
        }
    }
}