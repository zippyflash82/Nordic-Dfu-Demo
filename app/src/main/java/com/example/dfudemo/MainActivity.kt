package com.example.dfudemo

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import no.nordicsemi.android.dfu.DfuBaseService.MIME_TYPE_ZIP
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter
import no.nordicsemi.android.dfu.DfuServiceInitiator
import no.nordicsemi.android.dfu.DfuServiceListenerHelper
import no.nordicsemi.android.support.v18.scanner.*
import java.util.*
import kotlin.random.Random

var filePath = "storage/emulated/0/ANNE_CHEST_3.0.12-a.zip"
var fileUri:Uri?= null

private val REQUEST_CODE = Random(10).nextBits(16)

private const val SELECT_FILE_REQ = 1

class MainActivity : AppCompatActivity(), DevicesAdapter.ItemClickListener {

    var btnUploadFirmware: Button? = null
    var btnListDevices: Button? = null
    var btnSelctFile: Button? = null

    var scanning = false
    private lateinit var adapter: DevicesAdapter
    private lateinit var rvDevices: RecyclerView
    private lateinit var itemClickListener: DevicesAdapter.ItemClickListener
    private val devices: ArrayList<Device> = arrayListOf()
    private lateinit var tvSelectedDevice: TextView
    private var selectedDeviceName = "C3388"
    private val scanner: BluetoothLeScannerCompat = BluetoothLeScannerCompat.getScanner()


    private val progressListenerAdapter = object : DfuProgressListenerAdapter() {
        override fun onDeviceConnected(deviceAddress: String) {
            super.onDeviceConnected(deviceAddress)
        }

        override fun onDeviceConnecting(deviceAddress: String) {
            super.onDeviceConnecting(deviceAddress)
        }

        override fun onDeviceDisconnected(deviceAddress: String) {
            super.onDeviceDisconnected(deviceAddress)
        }

        override fun onDeviceDisconnecting(deviceAddress: String?) {
            super.onDeviceDisconnecting(deviceAddress)
        }

        override fun onDfuAborted(deviceAddress: String) {
            super.onDfuAborted(deviceAddress)
        }

        override fun onDfuCompleted(deviceAddress: String) {
            super.onDfuCompleted(deviceAddress)
        }

        override fun onDfuProcessStarted(deviceAddress: String) {
            super.onDfuProcessStarted(deviceAddress)
        }

        override fun onDfuProcessStarting(deviceAddress: String) {
            super.onDfuProcessStarting(deviceAddress)
        }

        override fun onEnablingDfuMode(deviceAddress: String) {
            super.onEnablingDfuMode(deviceAddress)
        }

        override fun onError(deviceAddress: String, error: Int, errorType: Int, message: String?) {
            super.onError(deviceAddress, error, errorType, message)
        }

        override fun onFirmwareValidating(deviceAddress: String) {
            super.onFirmwareValidating(deviceAddress)
        }

        override fun onProgressChanged(
            deviceAddress: String,
            percent: Int,
            speed: Float,
            avgSpeed: Float,
            currentPart: Int,
            partsTotal: Int
        ) {
            super.onProgressChanged(
                deviceAddress,
                percent,
                speed,
                avgSpeed,
                currentPart,
                partsTotal
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Enable bluetooth.
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (!adapter.isEnabled) {
            Log.i("TAG", "Bluetooth is off. Turning on bluetooth.")
            adapter.enable()
        }

        btnListDevices = findViewById(R.id.btn_list_devices)
        btnSelctFile = findViewById(R.id.btn_select_firm_ware)

        btnSelctFile?.setOnClickListener {
            Intent(Intent.ACTION_GET_CONTENT).apply {
                type = MIME_TYPE_ZIP
                startActivityForResult(this, SELECT_FILE_REQ)
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DfuServiceInitiator.createDfuNotificationChannel(this)
        }

        btnUploadFirmware = findViewById(R.id.btn_upload_firmware)
        btnUploadFirmware?.setOnClickListener {
            DfuServiceInitiator(tvSelectedDevice.text.toString())
                .setDeviceName(selectedDeviceName)
                .setKeepBond(true)
                .setForceDfu(false)
                .setPrepareDataObjectDelay(400)
                .setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true)
                .setZip(fileUri!!)
                .start(this, DfuService::class.java)
        }
        btnListDevices!!.setOnClickListener {
            startScan()
        }
    }

    /**
     * Activity's lifecycle onStart Method
     */
    override fun onStart() {
        super.onStart()
        requestPermission()

        tvSelectedDevice = findViewById(R.id.tvDeviceAddress)
        itemClickListener = this
        rvDevices = findViewById(R.id.rvDevices)
        adapter = DevicesAdapter(devices, itemClickListener)
        rvDevices.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)
            adapter = this@MainActivity.adapter
        }
    }

    // Request permission
    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val requestedPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                arrayOf(
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.BLUETOOTH_ADMIN,
                    android.Manifest.permission.BLUETOOTH
                )
            } else {
                arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.BLUETOOTH_ADMIN,
                    android.Manifest.permission.BLUETOOTH
                )
            }
            ActivityCompat.requestPermissions(this, requestedPermissions, REQUEST_CODE)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_FILE_REQ && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            val uri: Uri = data.data!!
            val filePathColumn = arrayOf(MediaStore.Downloads._ID)
            val cursor = contentResolver?.query(uri, filePathColumn, null, null, null)
            cursor?.let {
                while (cursor.moveToNext()) {
                    try {
                        val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                        columnIndex.let {
                            filePath = cursor.getString(columnIndex)

                        }
                    } catch (e: Exception) {

                    }
                }
                cursor.close()
                fileUri = uri
             //   viewModel.updateDfuZipFilePath(uri)
            }

        }
    }



    private fun startScan() {
        devices.clear()
        val settings: ScanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        scanner.startScan(null, settings, scanCallback)
        scanning = true
        btnListDevices!!.isEnabled = false
        Handler(Looper.getMainLooper()).postDelayed({
            if (scanning) {
                stopScan()
                btnListDevices!!.isEnabled = true
            }
        }, 15000)
    }

    val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Log.d("TAG", "onScanResult: ${result.device.address}")
            devices.add(
                Device(
                    result.device.address!!,
                    result.device.name ?: "N/A"
                )
            )
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            Log.d("TAG", "onBatchScanResults: ")
        }

        override fun onScanFailed(errorCode: Int) {
            Log.d("TAG", "onScanFailed: ")
        }
    }

    private fun stopScan() {
        Log.d("TAG", "stopScan: ${devices.size}")
        adapter = DevicesAdapter(devices, itemClickListener)
        rvDevices.adapter = adapter
        scanner.stopScan(scanCallback)
        scanning = false
    }


    override fun onResume() {
        super.onResume()
        DfuServiceListenerHelper.registerProgressListener(this, progressListenerAdapter)
    }

    override fun onPause() {
        super.onPause()
        DfuServiceListenerHelper.unregisterProgressListener(this, progressListenerAdapter)
    }

    override fun onItemClickListener(device: Device) {
        tvSelectedDevice.text = device.macAddress
        selectedDeviceName = device.name
    }
}