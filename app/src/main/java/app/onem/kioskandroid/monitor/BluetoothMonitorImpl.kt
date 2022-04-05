package app.onem.kioskandroid.monitor

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart

class BluetoothMonitorImpl(
    private val context: Context
) : BluetoothMonitor {

    override val bluetoothStatus: Flow<Boolean>

    init {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

        bluetoothStatus = if (bluetoothAdapter != null) {
            createStatusFlow(bluetoothAdapter)
        } else {
            // To be able to test on devices without bluetooth
            createStatusFlowStub()
        }
    }

    private fun createStatusFlow(bluetoothAdapter: BluetoothAdapter): Flow<Boolean> =
        callbackFlow {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    trySend(bluetoothAdapter.isEnabled)
                }
            }
            val intentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)

            context.registerReceiver(receiver, intentFilter)
            awaitClose {
                context.unregisterReceiver(receiver)
            }
        }
            .onStart {
                emit(bluetoothAdapter.isEnabled)
            }

    private fun createStatusFlowStub(): Flow<Boolean> =
        flow {
            emit(true)
        }
}
