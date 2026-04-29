package com.example.labo5

import android.content.res.ColorStateList
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager

    private var pressureSensor: Sensor? = null
    private var humiditySensor: Sensor? = null
    private var temperatureSensor: Sensor? = null

    private lateinit var pressure: TextView
    private lateinit var altitudee: TextView
    private lateinit var humidity: TextView
    private lateinit var temperature: TextView


    private lateinit var progressTemperature: ProgressBar
    private lateinit var buttonReset: Button

    private var currentPressure = 0f
    private var currentHumidity = 0f
    private var currentTemperature = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pressure = findViewById(R.id.textPressure)
        altitudee = findViewById(R.id.textAltitude)
        humidity = findViewById(R.id.textHumidity)
        temperature = findViewById(R.id.textTemperature)


        progressTemperature = findViewById(R.id.progressTemperature)
        buttonReset = findViewById(R.id.buttonReset)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        humiditySensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY)
        temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)

        environmentalText()
        updateUi()

        buttonReset.setOnClickListener {
            currentPressure = 0f
            currentHumidity = 0f
            currentTemperature = 0f

            environmentalText()
            updateUi()
        }
    }

    override fun onResume() {
        super.onResume()

        pressureSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        humiditySensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        temperatureSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_PRESSURE -> {
                currentPressure = event.values[0]
                updateUi()
            }

            Sensor.TYPE_RELATIVE_HUMIDITY -> {
                currentHumidity = event.values[0]
                environmentalText()
            }

            Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                val newTemperature = event.values[0]

                if (newTemperature in -273f..100f) {
                    currentTemperature = newTemperature
                    environmentalText()
                    updateUi()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun updateUi() {
        pressure.text = Sensors.formatPressure(currentPressure)

        val pressureForAltitude = if (currentPressure > 0f) {
            currentPressure
        } else {
            SensorManager.PRESSURE_STANDARD_ATMOSPHERE
        }

        val altitude = SensorManager.getAltitude(
            SensorManager.PRESSURE_STANDARD_ATMOSPHERE,
            pressureForAltitude
        )
        altitudee.text = Sensors.formatAltitude(altitude)

        temperature.text = Sensors.formatTemperature(currentTemperature)

        val tempMin = -273f
        val tempMax = 100f
        val normalized = ((currentTemperature - tempMin) / (tempMax - tempMin) * 100f)
            .coerceIn(0f, 100f)

        progressTemperature.max = 100
        progressTemperature.progress = normalized.roundToInt()
        progressTemperature.progressTintList =
            ColorStateList.valueOf(temperatureColor(currentTemperature))

    }

    private fun environmentalText() {
        humidity.text = Sensors.formatHumidity(currentHumidity)
        temperature.text = Sensors.formatTemperature(currentTemperature)
    }

    private fun temperatureColor(temp: Float): Int {
        val deepCold = ContextCompat.getColor(this, R.color.smalt)
        val cold = ContextCompat.getColor(this, R.color.havelock)
        val mild = ContextCompat.getColor(this, R.color.celestial_blue)
        val neutral = ContextCompat.getColor(this, R.color.pear)
        val warm = ContextCompat.getColor(this, R.color.warm_orange)
        val hot = ContextCompat.getColor(this, R.color.hot_red)

        return when {
            temp <= -100f -> {
                val ratio = ((temp + 273f) / 173f).coerceIn(0f, 1f)
                blendColors(deepCold, cold, ratio)
            }

            temp <= 0f -> {
                val ratio = ((temp + 100f) / 100f).coerceIn(0f, 1f)
                blendColors(cold, mild, ratio)
            }

            temp <= 30f -> {
                val ratio = (temp / 30f).coerceIn(0f, 1f)
                blendColors(mild, neutral, ratio)
            }

            temp <= 60f -> {
                val ratio = ((temp - 30f) / 30f).coerceIn(0f, 1f)
                blendColors(neutral, warm, ratio)
            }

            else -> {
                val ratio = ((temp - 60f) / 40f).coerceIn(0f, 1f)
                blendColors(warm, hot, ratio)
            }
        }
    }

    private fun blendColors(startColor: Int, endColor: Int, ratio: Float): Int {
        val r = (Color.red(startColor) + ratio * (Color.red(endColor) - Color.red(startColor))).roundToInt()
        val g = (Color.green(startColor) + ratio * (Color.green(endColor) - Color.green(startColor))).roundToInt()
        val b = (Color.blue(startColor) + ratio * (Color.blue(endColor) - Color.blue(startColor))).roundToInt()
        return Color.rgb(r, g, b)
    }
}