package app.onem.kioskandroid.monitor

import kotlinx.coroutines.flow.Flow

interface LocationMonitor {

    val locationStatus: Flow<Boolean>
}