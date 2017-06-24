package com.tuule.blues

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.reactivex.Observable
import io.reactivex.subjects.ReplaySubject

/**
 * Created by tuule on 22.06.17.
 */
class BluetoothScanReceiver : BroadcastReceiver() {

    private val deviceSubject = ReplaySubject.create<BluetoothDevice>()
    val deviceListener: Observable<BluetoothDevice> = deviceSubject

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        when (action) {
//            BluetoothAdapter.ACTION_DISCOVERY_STARTED ->
            BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> deviceSubject.onComplete()
            BluetoothDevice.ACTION_FOUND -> deviceSubject.onNext(intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE))
        }

    }
}
