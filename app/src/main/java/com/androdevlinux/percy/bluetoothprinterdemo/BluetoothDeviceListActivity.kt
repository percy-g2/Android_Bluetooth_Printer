package com.androdevlinux.percy.bluetoothprinterdemo

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.device_list.*

class BluetoothDeviceListActivity : Activity() {
    private var bluetoothAdapter: BluetoothAdapter? = null

    override fun onCreate(mSavedInstanceState: Bundle?) {
        super.onCreate(mSavedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.device_list)
        setResult(0)
        val pairedDevicesAdapter = ArrayAdapter<String>(this, R.layout.device_name)
        paired_devices.adapter = pairedDevicesAdapter
        paired_devices.onItemClickListener = OnItemClickListener { _, view, _, _ ->
            try {
                bluetoothAdapter!!.cancelDiscovery()
                val mDeviceInfo = (view as TextView).text.toString()
                Log.i(TAG, "Device_Address " + mDeviceInfo.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1])
                val bundle = Bundle()
                bundle.putString("DeviceAddress", mDeviceInfo.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1])
                bundle.putString("DeviceName", mDeviceInfo.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])
                val mBackIntent = Intent()
                mBackIntent.putExtras(bundle)
                setResult(-1, mBackIntent)
                finish()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val mPairedDevices = bluetoothAdapter!!.bondedDevices
        if (mPairedDevices.size > 0) {
            title_paired_devices.visibility = View.VISIBLE
            for (mDevice in mPairedDevices) {
                pairedDevicesAdapter.add(mDevice.name + "\n" + mDevice.address)
            }
            return
        }
        pairedDevicesAdapter.add("None Paired Devices")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bluetoothAdapter != null) {
            bluetoothAdapter!!.cancelDiscovery()
        }
    }

    companion object {
        private const val TAG = "BluetoothDeviceList"
    }
}
