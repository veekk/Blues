package com.tuule.blues

import android.bluetooth.BluetoothDevice
import io.reactivex.Observable
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import android.bluetooth.BluetoothSocket
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject


/**
 * Created by tuule on 19.06.17.
 */
class SPPConnection<out T : BluesConnectionType>(val device: BluetoothDevice, val type: T) {

    companion object {
        private val UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    private var deviceStateSubject = BehaviorSubject.create<Blues.DeviceState>()
    var deviceStateObservable: Observable<Blues.DeviceState> = deviceStateSubject

    val currentState
        get() = deviceStateSubject.value

    private var socket: BluetoothSocket? = null

    fun connectAndStartRead(): Observable<String> = connect()
            .flatMap { startRead() }
            .doOnError {
                socket?.close()
                deviceStateSubject.onNext(Blues.DeviceState.ERROR)
            }
            .doOnComplete {
                deviceStateSubject.onNext(Blues.DeviceState.DISCONNECTED)
            }
            .subscribeOn(Schedulers.newThread())


    fun connect() = Observable.create <Unit> {
        deviceStateSubject.onNext(Blues.DeviceState.CONNECTING)
        socket = device.createInsecureRfcommSocketToServiceRecord(UUID_SPP)?.apply {
            try {
                connect()
                type.renewHandler(inputStream, outputStream)
                it.onNext(Unit)
                it.onComplete()
            } catch (e: IOException) {
                it.onError(e)
            }
        }
    }

    fun disconnect() {
        socket?.close()
        interrupted = true
    }

    @Volatile private var interrupted = false

    fun startRead() = Observable.create<String> {
        deviceStateSubject.onNext(Blues.DeviceState.CONNECTED)
        while (!interrupted) {
            try {
                it.onNext(type.handler.parseData())
            } catch (e: IOException) {
                if (socket?.isConnected ?: true)
                    it.onError(e)
                else
                    it.onComplete()
                interrupted = true
            }
        }
    }

}