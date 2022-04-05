package app.onem.domain.repositories

import kotlinx.coroutines.flow.Flow

interface DataRepository {

    fun getSelectedReaderSerialNumber(): Flow<String?>

    suspend fun saveSelectedReaderSerialNumber(id: String?)

    fun getSelectedShopId(): Flow<String?>

    suspend fun saveSelectedShopId(id: String?)

    suspend fun saveLocationId(Id: String?)

    suspend fun getSavedLocationId(): Flow<String?>
}
