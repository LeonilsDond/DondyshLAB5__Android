package com.example.labo5

import java.util.Locale

object Sensors {

    fun formatPressure(value: Float): String {
        return String.format(Locale.getDefault(), "%.1f гПа", value)
    }

    fun formatAltitude(value: Float): String {
        return String.format(Locale.getDefault(), "%.1f м", value)
    }

    fun formatHumidity(value: Float): String {
        return String.format(Locale.getDefault(), "%.1f %%", value)
    }

    fun formatTemperature(value: Float): String {
        return String.format(Locale.getDefault(), "%.1f °C", value)
    }
}