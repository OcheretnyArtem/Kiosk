package app.onem.kioskandroid.monitor

import kotlinx.coroutines.flow.Flow

interface NetworkMonitor {

    val networkStatus: Flow<Boolean>
}