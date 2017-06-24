package com.tuule.blues

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.bluetooth.BluetoothManager
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject


/**
 * Created by tuule on 19.06.17.
 */
class Blues<T : BluesConnectionType>(val context: Context) {

    //<editor-fold desc="adapter">

    val adapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter!!

    fun enableAdapter() {
        if (!adapter.isEnabled)
            adapter.enable()
    }

    val bondedDevices: Set<BluetoothDevice>
        get() = adapter.bondedDevices

    //</editor-fold>

    //<editor-fold desc="emmiters">

    private val dataSubject = BehaviorSubject.create<Pair<String, T>>()
    val dataObservable: Observable<Pair<String, T>> = dataSubject

    private val adapterStateSubject = BehaviorSubject.create<AdapterState>()
    val adapterStateObservable: Observable<AdapterState> = adapterStateSubject

    private val deviceConnectionSubject = BehaviorSubject.create<BluesDeviceModel<T>>()
    val deviceConnectionListener: Observable<BluesDeviceModel<T>> = deviceConnectionSubject

    //</editor-fold>

    //<editor-fold desc="main">
    private val connectionList = ArrayList<SPPConnection<T>>()

    val connectedDeviceList: List<BluesDeviceModel<T>>
        get() = connectionList
                .filter { it.currentState == DeviceState.CONNECTED }
                .map { BluesDeviceModel(it.device, it.type, it.currentState) }

    fun startDiscovery(): Observable<List<BluesDeviceModel<T>>> {
//        if (adapter.isDiscovering)
//            adapter.cancelDiscovery()
//
//        val scanReceiver = BluetoothScanReceiver()
//        val scanIntentFilter = IntentFilter().apply {
//            addAction(BluetoothDevice.ACTION_FOUND)
//            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
//            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
//        }
//
//        context.registerReceiver(scanReceiver, scanIntentFilter)
//
//        adapter.startDiscovery()

//        return scanReceiver.deviceListener
//                .scan(ArrayList<BluetoothDevice>(bondedDevices), { set, device -> set.apply { if (!contains(device)) add(device) } })
//                .doOnComplete { context.unregisterReceiver(scanReceiver) }
//                .map { set ->
//                    set.map { device ->
//                        connectedDeviceList.find { it.device == device }
//                                ?: BluesDeviceModel(device, null, DeviceState.DISCONNECTED)
//                    }
//                }

        return Observable.just(bondedDevices).map {
            it.map { device ->
                connectedDeviceList.find { it.device == device }
                        ?: BluesDeviceModel(device, null, DeviceState.DISCONNECTED)
            }
        }
    }

    fun connect(address: String, type: T) {
        SPPConnection(adapter.getRemoteDevice(address),
                type)
                .let { sppConnection ->

                    connectionList.add(sppConnection)

                    sppConnection.deviceStateObservable.subscribe { state ->
                        deviceConnectionSubject.onNext(BluesDeviceModel(sppConnection.device, type, state))
                        when (state) {
                            Blues.DeviceState.CONNECTED -> {
                                adapterStateSubject.onNext(AdapterState.CONNECTED)
                            }
                            Blues.DeviceState.DISCONNECTED, Blues.DeviceState.ERROR -> {
                                connectionList.remove(sppConnection)
                                if (connectionList.isEmpty())
                                    adapterStateSubject.onNext(AdapterState.DISCONNECTED)
                            }
                        }
                    }
                    sppConnection.connectAndStartRead().subscribe({ dataSubject.onNext(it to type) }, {})
                }
    }

    //</editor-fold>

    //<editor-fold desc="disconnect">

    fun disconnect(address: String) = disconnect(connectionList.find { it.device.address == address })

    private fun disconnect(conn: SPPConnection<T>?) = conn?.disconnect()

    fun disconnectAll() = connectionList.forEach { it.disconnect() }

    //</editor-fold>

    //<editor-fold desc="states">

    enum class DeviceState {
        CONNECTED,
        CONNECTING,
        DISCONNECTED,
        ERROR
    }

    enum class AdapterState {
        CONNECTED,
        DISCONNECTED,
        DISABLED
    }

    //</editor-fold>

    //<editor-fold desc="auto-connect">

    var autoconnectArray = emptyList<Pair<String, T>>()

    private var autoconnectionThread: AutoconnectionThread? = null

    fun startAutoconnect() {
        autoconnectionThread?.interrupt = true
        autoconnectionThread = AutoconnectionThread()
        autoconnectionThread!!.start()
    }

    fun stopAutoconnect() {
        autoconnectionThread?.interrupt = true
        autoconnectionThread = null
    }

    private inner class AutoconnectionThread : Thread() {
        var interrupt = false

        override fun run() {
            while (!interrupt) {
                autoconnectArray.filterNot { (device, _) ->
                    connectedDeviceList
                            .map { it.device.address }.contains(device)
                }.forEach { (addr, type) ->
                    connect(addr, type)
                }
                Thread.sleep(AUTOCONNECT_INTERVAL)
            }
        }
    }

    companion object {
        const val AUTOCONNECT_INTERVAL = 10000L
    }

    //</editor-fold>

}

