package com.speedo.gauge

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "speedometer_settings")

class UnitsStore(private val context: Context) {

    private val unitKey = stringPreferencesKey("speed_unit")

    val unitFlow: Flow<SpeedUnit> = context.settingsDataStore.data.map { prefs ->
        runCatching { SpeedUnit.valueOf(prefs[unitKey] ?: SpeedUnit.MPH.name) }
            .getOrDefault(SpeedUnit.MPH)
    }

    suspend fun setUnit(unit: SpeedUnit) {
        context.settingsDataStore.edit { it[unitKey] = unit.name }
    }
}
