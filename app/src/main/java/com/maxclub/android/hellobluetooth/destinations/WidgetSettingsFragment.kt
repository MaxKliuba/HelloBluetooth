package com.maxclub.android.hellobluetooth.destinations

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import com.maxclub.android.hellobluetooth.R
import com.maxclub.android.hellobluetooth.data.Widget
import com.maxclub.android.hellobluetooth.viewmodel.WidgetSettingsViewModel

class WidgetSettingsFragment : Fragment() {
    private val widgetSettingsViewModel: WidgetSettingsViewModel by lazy {
        ViewModelProvider(this)[WidgetSettingsViewModel::class.java]
    }
    private val args: WidgetSettingsFragmentArgs by navArgs()
    private lateinit var navController: NavController

    private lateinit var nameInputField: TextInputLayout
    private lateinit var applyChangesFloatingActionButton: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_widget_settings, container, false)
        navController = findNavController()

        navController = findNavController()

        nameInputField = view.findViewById<TextInputLayout?>(R.id.nameInputField).apply {
            editText?.doOnTextChanged { text, _, _, _ ->
                isValuesValid(text.toString())
            }
        }

        applyChangesFloatingActionButton =
            view.findViewById<FloatingActionButton>(R.id.applyChangesFloatingActionButton).apply {
                setOnClickListener {
                    val name = nameInputField.editText?.text.toString().trim()
                    if (isValuesValid(name)) {
                        val widget = args.widget
                        if (widget != null) {
                            widget.name = name
                            widgetSettingsViewModel.updateWidget(widget)
                        } else {
                            val newWidget = Widget(
                                controllerId = args.controller.id,
                                type = Widget.BUTTON_TYPE,
                                size = Widget.SMALL_SIZE
                            )
                            widgetSettingsViewModel.addWidget(newWidget)
                        }
                        navController.navigateUp()
                    }
                }
            }

        args.widget?.let {
            nameInputField.editText?.text?.append(it.name)
        }

        return view
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