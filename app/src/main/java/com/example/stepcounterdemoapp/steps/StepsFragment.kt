package com.example.stepcounterdemoapp.steps
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.stepcounterdemoapp.R
import com.example.stepcounterdemoapp.databinding.FragmentStepsBinding
import java.lang.Math.toDegrees

class StepsFragment : Fragment() {

    private lateinit var viewModel: StepsViewModel

    private var arrayGravity = FloatArray(3)
    private var arrayMagnetic = FloatArray(3)
    private var orientation = FloatArray(3)
    private var rotationMatrix = FloatArray(9)

    @RequiresApi(Build.VERSION_CODES.Q)
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

        requestPermissions()


        var sensorManager: SensorManager =
            requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val listener: SensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor?.type == Sensor.TYPE_GYROSCOPE) {
                    val gyroString = context?.resources?.getString(R.string.tv_gyro_data)
                    binding.gyroTxtView.text = "$gyroString \nx: ${event.values[0]}\n"
                    binding.gyroTxtView.append("y: ${event.values[1]}\n")
                    binding.gyroTxtView.append("z: ${event.values[2]}")
                }

                if (event.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                    val accString = context?.resources?.getString(R.string.tv_accelerometer_data)
                    binding.accTxtView.text = "$accString \nx: ${event.values[0]}\n"
                    binding.accTxtView.append("y: ${event.values[1]}\n")
                    binding.accTxtView.append("z: ${event.values[2]}")
                    arrayGravity = event.values
                }

                if (event.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    val compsString = context?.resources?.getString(R.string.tv_compass_data)
                    arrayMagnetic = event.values
                    SensorManager.getRotationMatrix(
                        rotationMatrix,
                        null,
                        arrayGravity,
                        arrayMagnetic
                    )
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    val degrees = (toDegrees(orientation[0].toDouble()) + 360).toFloat() % 360
                    val compassOrient = when (degrees) {
                        in 0.0..23.0 -> "North"
                        in 23.0..67.0 -> "North East"
                        in 67.0..112.00 -> "East"
                        in 112.0..157.0 -> "South East"
                        in 157.0..202.0 -> "South"
                        in 202.0..247.0 -> "South West"
                        in 247.0..292.0 -> "West"
                        in 292.0..337.0 -> "North West"
                        in 337.0..360.0 -> "North"
                        else -> "Compass not active"
                    }
                    binding.compassTxtView.text = "$compsString\n$compassOrient\n$degrees"
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
                            binding.gyroTxtView.text =
                                "It does not support magnetic field sensor sensors."
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

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun hasActivityRecognitionPermission() =
        context?.let {
            ActivityCompat.checkSelfPermission(
                it,
                Manifest.permission.ACTIVITY_RECOGNITION)
        } == PackageManager.PERMISSION_GRANTED

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestPermissions(){
        var permissionsToRequest = mutableListOf<String>()
        if(!hasActivityRecognitionPermission()){
            permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        if(permissionsToRequest.isNotEmpty()){
            ActivityCompat.requestPermissions(context as Activity,permissionsToRequest.toTypedArray(),0)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 && grantResults.isNotEmpty()){
            for (i in grantResults.indices){
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    Log.d("PermissionsRequest", "${permissions[i]} granted.")
                }
            }
        }
    }
}







