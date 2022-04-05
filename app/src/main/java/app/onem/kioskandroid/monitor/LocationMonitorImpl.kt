package app.onem.kioskandroid.monitor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart

class LocationMonitorImpl(
    context: Context
) : LocationMonitor {

    override val locationStatus: Flow<Boolean>

    init {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationStatus = callbackFlow {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    trySend(locationManager.isLocationEnabled)
                }
            }
            val intentFilter = IntentFilter(LocationManager.MODE_CHANGED_ACTION)

            context.registerReceiver(receiver, intentFilter)
            awaitClose {
                context.unregisterReceiver(receiver)
            }
        }
            .onStart {
                emit(locationManager.isLocationEnabled)
            }
    }
}