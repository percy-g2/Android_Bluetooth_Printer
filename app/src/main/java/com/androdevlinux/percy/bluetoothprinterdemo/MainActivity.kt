package com.androdevlinux.percy.bluetoothprinterdemo

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.nio.charset.Charset
import java.util.*


/**
 * Created by percy on 05/07/2018.
 */

class MainActivity : AppCompatActivity(), Runnable {
    private val applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothDevice: BluetoothDevice? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private val handler = Handler(Handler.Callback { msg ->
        if (progressDialog != null) {
            progressDialog!!.dismiss()
        }
        Toast.makeText(this@MainActivity, msg.obj.toString(), Toast.LENGTH_SHORT).show()
        return@Callback true
    })

    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        disconnect_device!!.setOnClickListener {
            try {
                resetConnection()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }


        find_device!!.setOnClickListener {
            try {
                resetConnection()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            when {
                bluetoothAdapter == null -> Toast.makeText(this, "Bluetooth adapter is needed!", Toast.LENGTH_SHORT).show()
                bluetoothAdapter!!.isEnabled -> startActivityForResult(Intent(this, BluetoothDeviceListActivity::class.java), REQUEST_CONNECT_DEVICE)
                else -> startActivityForResult(Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), REQUEST_ENABLE_BT)
            }
        }

        btnPrintText!!.setOnClickListener {
            if (!edtPrintContent!!.text.toString().isEmpty()) {
                object : Thread() {
                    override fun run() {
                        try {
                            if (btnBoldPrint.isChecked) {
                                bluetoothSocket!!.outputStream.write(ESCPOSCommands.FS_FONT_SIZE_BOLD + edtPrintContent!!.text.toString().toByteArray(Charset.forName("UTF-8")))
                            } else {
                                bluetoothSocket!!.outputStream.write(ESCPOSCommands.FS_FONT_SIZE_NON_BOLD + edtPrintContent!!.text.toString().toByteArray(Charset.forName("UTF-8")))
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                            val alertMessage = Message()
                            alertMessage.obj = "Check your connection with printer!"
                            handler.sendMessage(alertMessage)
                        }

                    }
                }.start()
            } else {
                Toast.makeText(this, "Print text can not be empty!", Toast.LENGTH_SHORT).show()
            }
        }

        btnLineFeed!!.setOnClickListener {
            object : Thread() {
                override fun run() {
                    try {
                        bluetoothSocket!!.outputStream.write(ESCPOSCommands.LF)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        val alertMessage = Message()
                        alertMessage.obj = "Check your connection with printer!"
                        handler.sendMessage(alertMessage)
                    }

                }
            }.start()
        }
        btnSamplePrint!!.setOnClickListener {
            object : Thread() {
                override fun run() {
                    try {
                        if (btnBoldPrint.isChecked) {
                            bluetoothSocket!!.outputStream.write(ESCPOSCommands.FS_FONT_SIZE_BOLD + " \n\n  Test Print \n\n  Hello From MainActivity".toByteArray(Charset.forName("UTF-8")))
                        } else {
                            bluetoothSocket!!.outputStream.write(ESCPOSCommands.FS_FONT_SIZE_NON_BOLD + " \n\n  Test Print \n\n  Hello From MainActivity".toByteArray(Charset.forName("UTF-8")))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        val alertMessage = Message()
                        alertMessage.obj = "Check your connection with printer!"
                        handler.sendMessage(alertMessage)
                    }

                }
            }.start()
        }
    }

    public override fun onActivityResult(mRequestCode: Int, mResultCode: Int, mDataIntent: Intent?) {
        super.onActivityResult(mRequestCode, mResultCode, mDataIntent)
        when (mRequestCode) {
            REQUEST_CONNECT_DEVICE -> if (mResultCode == -1) {
                val mDeviceAddress = mDataIntent!!.extras.getString("DeviceAddress")
                val mDeviceName = mDataIntent.extras.getString("DeviceName")
                device_address!!.text = mDeviceAddress
                device_name!!.text = mDeviceName
                Log.i(TAG, "device address " + mDeviceAddress!!)
                bluetoothDevice = bluetoothAdapter!!.getRemoteDevice(mDeviceAddress)
                progressDialog = ProgressDialog.show(this, "Connecting...", bluetoothDevice!!.name + " : " + bluetoothDevice!!.address, true, true)
                Thread(this).start()

            }
            REQUEST_ENABLE_BT -> if (mResultCode == -1) {
                startActivityForResult(Intent(this, BluetoothDeviceListActivity::class.java), REQUEST_CONNECT_DEVICE)
            } else {
                Toast.makeText(this, "Bluetooth needs to be enabled!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Throws(IOException::class)
    private fun resetConnection() {
        if (bluetoothSocket != null) {
            if (bluetoothSocket!!.inputStream != null) {
                try {
                    bluetoothSocket!!.inputStream.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            if (bluetoothSocket!!.outputStream != null) {
                try {
                    bluetoothSocket!!.outputStream.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }


            try {
                bluetoothSocket!!.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            bluetoothSocket = null
            device_address!!.text = resources.getString(R.string.none)
            device_name!!.text = resources.getString(R.string.none)
            Toast.makeText(this, "Device disconnected", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No device connected", Toast.LENGTH_SHORT).show()
        }

    }

    override fun run() {
        try {
            bluetoothSocket = bluetoothDevice!!.createInsecureRfcommSocketToServiceRecord(applicationUUID)
            bluetoothAdapter!!.cancelDiscovery()
            bluetoothSocket!!.connect()
            val alertMessage = Message()
            alertMessage.obj = "DeviceConnected "
            handler.sendMessage(alertMessage)
        } catch (e: IOException) {
            e.printStackTrace()
            closeSocket(bluetoothSocket!!)
        }

    }

    private fun closeSocket(nOpenSocket: BluetoothSocket) {
        try {
            nOpenSocket.close()
            val alertMessage = Message()
            alertMessage.obj = "Connection Failed"
            device_address!!.text = resources.getString(R.string.none)
            device_name!!.text = resources.getString(R.string.none)
            handler.sendMessage(alertMessage)
            Log.i(TAG, "SocketClosed")
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    companion object {
        private const val REQUEST_CONNECT_DEVICE = 1
        private const val REQUEST_ENABLE_BT = 2
        private const val TAG = "MainActivity"
    }
}
