package app.onem.kioskandroid.monitor

import kotlinx.coroutines.flow.Flow

interface BluetoothMonitor {

    val bluetoothStatus: Flow<Boolean>
}
