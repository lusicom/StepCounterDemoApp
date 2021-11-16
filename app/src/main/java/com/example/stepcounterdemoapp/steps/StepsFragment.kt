package com.example.stepcounterdemoapp.steps

import android.content.Context
import android.hardware.*
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.stepcounterdemoapp.R
import com.example.stepcounterdemoapp.databinding.FragmentStepsBinding


class StepsFragment : Fragment(){

    private lateinit var viewModel: StepsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentStepsBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_steps,
            container,
            false
        )

        viewModel = ViewModelProvider(this).get(StepsViewModel::class.java)

        var sensorManager: SensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val listener: SensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event?.sensor?.type == Sensor.TYPE_GYROSCOPE) {
                    val gyroString = context?.resources?.getString(R.string.tv_gyro_data)
                    binding.gyroTxtView.text="$gyroString \nx: ${event.values[0]}\n"
                    binding.gyroTxtView.append("y: ${event.values[1]}\n")
                    binding.gyroTxtView.append("z: ${event.values[2]}")
                }

                if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                    val accString = context?.resources?.getString(R.string.tv_accelerometer_data)
                    binding.accTxtView.text="$accString \nx: ${event.values[0]}\n"
                    binding.accTxtView.append("y: ${event.values[1]}\n")
                    binding.accTxtView.append("z: ${event.values[2]}")
                }

                if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    val magnString = context?.resources?.getString(R.string.tv_magnetometer_data)
                    binding.magnTxtView.text="$magnString \nx: ${event.values[0]}\n"
                    binding.magnTxtView.append("y: ${event.values[1]}\n")
                    binding.magnTxtView.append("z: ${event.values[2]}")
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                Log.d("MY Sensor Listener", "$sensor - $accuracy")
            }
        }

        val fps = resources.getStringArray(R.array.fps_array)
        val adapter = context?.let { ArrayAdapter(it, R.layout.dropdown_item, fps) }

        binding.autoCompleteTextView.setAdapter(adapter)
        binding.autoCompleteTextView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                val item = parent?.getItemAtPosition(position).toString()
                Toast.makeText(context, item, Toast.LENGTH_SHORT).show()
            }

        viewModel.isStarted.observe(
            viewLifecycleOwner,
            {
                when (it) {
                    true -> {
                        binding.button.text = "Stop"
                        val sensorGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
                        val sensorAcc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                        val sensorMagn = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)


                        val checkedGyro = sensorManager.registerListener(
                            listener,
                            sensorGyro,
                            SensorManager.SENSOR_DELAY_UI
                        )
                        if (!checkedGyro) {
                            binding.gyroTxtView.text = "It does not support gyroscope sensors."
                        }

                        val checkedAcc = sensorManager.registerListener(
                            listener,
                            sensorAcc,
                            SensorManager.SENSOR_DELAY_UI
                        )
                        if (!checkedAcc) {
                            binding.gyroTxtView.text = "It does not support accelerometer sensors."
                        }

                        val checkedMagn = sensorManager.registerListener(
                            listener,
                            sensorMagn,
                            SensorManager.SENSOR_DELAY_UI
                        )
                        if (!checkedMagn) {
                            binding.gyroTxtView.text = "It does not support magnetic field sensor sensors."
                        }
                    }

                    false -> {
                        binding.button.text = "Start"
                        sensorManager.unregisterListener(listener)
                    }
                }
            })

        binding.button.setOnClickListener {
            viewModel.changeButtonState()
        }

        return binding.root
    }
}