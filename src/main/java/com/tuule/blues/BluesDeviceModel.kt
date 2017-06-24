package com.tuule.blues

import android.bluetooth.BluetoothDevice

/**
 * Created by tuule on 21.06.17.
 */
data class BluesDeviceModel<out T : BluesConnectionType>(val device: BluetoothDevice,
                                                         val typeModel: T? = null,
                                                         val deviceState: Blues.DeviceState =
                                                    Blues.DeviceState.DISCONNECTED)
