package com.maxclub.android.hellobluetooth.destinations

import android.os.Bundle
import android.view.*
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.TextInputLayout
import com.maxclub.android.hellobluetooth.R
import com.maxclub.android.hellobluetooth.data.Controller
import com.maxclub.android.hellobluetooth.viewmodel.ControllerSettingsViewModel

class ControllerSettingsFragment : Fragment() {
    private val controllerSettingsViewModel: ControllerSettingsViewModel by lazy {
        ViewModelProvider(this)[ControllerSettingsViewModel::class.java]
    }
    private val args: ControllerSettingsFragmentArgs by navArgs()

    private lateinit var nameInputField: TextInputLayout

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_controller_settings, container, false)
        navController = findNavController()

        nameInputField = view.findViewById<TextInputLayout?>(R.id.nameInputField).apply {
            editText?.doOnTextChanged { text, _, _, _ ->
                isValuesValid(text.toString())
            }
        }

        args.controller?.let {
            nameInputField.editText?.text?.append(it.name)
        }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_controller_settings, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.apply -> {
                val name = nameInputField.editText?.text.toString().trim()
                if (isValuesValid(name)) {
                    val controller = args.controller
                    if (controller != null) {
                        controller.name = name
                        controllerSettingsViewModel.updateController(controller)
                    } else {
                        val newController = Controller(name = name)
                        controllerSettingsViewModel.addController(newController)
                    }
                    navController.navigateUp()
                    true
                } else {
                    false
                }
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }

    private fun isValuesValid(name: String): Boolean =
        if (name.isNotEmpty()) {
            nameInputField.error = null
            true
        } else {
            nameInputField.error = getString(R.string.name_error_message)
            false
        }
}