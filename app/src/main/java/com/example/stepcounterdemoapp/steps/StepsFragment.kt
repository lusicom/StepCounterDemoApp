package com.example.stepcounterdemoapp.steps

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.stepcounterdemoapp.R
import com.example.stepcounterdemoapp.databinding.FragmentStepsBinding
import android.widget.Toast

class StepsFragment : Fragment() {

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

        viewModel.isStarted.observe(
            viewLifecycleOwner,
            {
                when (it) {
                    true -> {
                        binding.button.text = "Stop"
                    }
                    false -> {
                        binding.button.text = "Start"
                    }
                }
            })

        binding.button.setOnClickListener {
            viewModel.changeButtonState()
        }

        val fps = resources.getStringArray(R.array.fps_array)

        val adapter = context?.let { ArrayAdapter(it, R.layout.dropdown_item, fps) }

        binding.autoCompleteTextView.setAdapter(adapter)
        binding.autoCompleteTextView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                val item = parent?.getItemAtPosition(position).toString()
                Toast.makeText(context, item, Toast.LENGTH_SHORT).show()
            }

        return binding.root
    }
}