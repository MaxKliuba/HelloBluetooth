package com.maxclub.android.hellobluetooth.destinations

import android.os.Bundle
import android.view.*
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import com.maxclub.android.hellobluetooth.R
import com.maxclub.android.hellobluetooth.data.Controller
import com.maxclub.android.hellobluetooth.viewmodel.ControllerSettingsViewModel

class ControllerSettingsFragment : Fragment() {
    private val controllerSettingsViewModel: ControllerSettingsViewModel by lazy {
        ViewModelProvider(this)[ControllerSettingsViewModel::class.java]
    }

    private lateinit var navController: NavController
    private val args: ControllerSettingsFragmentArgs by navArgs()

    private lateinit var nameInputField: TextInputLayout
    private lateinit var applyChangesFloatingActionButton: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_controller_settings, container, false)
        navController = findNavController()

        nameInputField = view.findViewById(R.id.nameInputField)

        applyChangesFloatingActionButton =
            view.findViewById<FloatingActionButton>(R.id.applyChangesFloatingActionButton).apply {
                setOnClickListener {
                    val name = nameInputField.editText?.text.toString().trim()
                    if (validateValues()) {
                        val controller = args.controller
                        if (controller != null) {
                            controller.name = name
                            controllerSettingsViewModel.updateController(controller)
                        } else {
                            val newController = Controller(name = name)
                            controllerSettingsViewModel.addController(newController)
                        }
                        navController.navigateUp()
                    }
                }
            }

        args.controller?.let {
            nameInputField.editText?.text?.append(it.name)
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        nameInputField.editText?.apply {
            doOnTextChanged { _, _, _, _ ->
                validateNameValue()
            }
            setOnFocusChangeListener { _, isFocused ->
                if (!isFocused) {
                    validateNameValue()
                }
            }
        }
    }

    private fun validateValues(): Boolean = validateNameValue()

    private fun validateNameValue(): Boolean =
        if (nameInputField.editText?.text.toString().isNotEmpty()) {
            nameInputField.error = null
            true
        } else {
            nameInputField.error = getString(R.string.invalid_value_message)
            false
        }
}