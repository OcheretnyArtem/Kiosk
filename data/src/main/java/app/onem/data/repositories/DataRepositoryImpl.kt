package app.onem.data.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.onem.domain.repositories.DataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class DataRepositoryImpl(val context: Context) : DataRepository {
    companion object {
        private const val DATA_STORE_NAME = "dataStore"
        private val READER_ID_KEY = stringPreferencesKey("readerId")
        private val SHOP_ID_KEY = stringPreferencesKey("shopId")
        private val LOCATION_ID_KEY = stringPreferencesKey("locationId")
    }

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATA_STORE_NAME)

    override fun getSelectedReaderSerialNumber(): Flow<String?> {
        return getValue(READER_ID_KEY)
    }

    override suspend fun saveSelectedReaderSerialNumber(id: String?) {
        saveValue(READER_ID_KEY, id)
    }

    override fun getSelectedShopId(): Flow<String?> {
        return getValue(SHOP_ID_KEY)
    }

    override suspend fun saveSelectedShopId(id: String?) {
        saveValue(SHOP_ID_KEY, id)
    }

    override suspend fun saveLocationId(id: String?) {
        saveValue(LOCATION_ID_KEY, id)
    }

    override suspend fun getSavedLocationId(): Flow<String?> {
        return getValue(LOCATION_ID_KEY)
    }

    private fun <T> getValue(key: Preferences.Key<T>): Flow<T?> {
        return context.dataStore.data.map { it[key] }
    }

    private suspend fun <T> saveValue(key: Preferences.Key<T>, value: T?) {
        context.dataStore.edit { preferences ->
            value?.let {
                preferences[key] = it
            } ?: preferences.remove(key)
        }
    }
}