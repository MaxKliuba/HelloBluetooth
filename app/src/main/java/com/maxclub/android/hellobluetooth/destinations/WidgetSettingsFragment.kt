package com.maxclub.android.hellobluetooth.destinations

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
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

private const val LOG_TAG = "WidgetSettingsFragment"

class WidgetSettingsFragment : Fragment() {
    private val widgetSettingsViewModel: WidgetSettingsViewModel by lazy {
        ViewModelProvider(this)[WidgetSettingsViewModel::class.java]
    }
    private val args: WidgetSettingsFragmentArgs by navArgs()
    private lateinit var navController: NavController

    private lateinit var nameInputField: TextInputLayout
    private lateinit var typeInputField: TextInputLayout
    private lateinit var sizeInputField: TextInputLayout
    private lateinit var applyChangesFloatingActionButton: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_widget_settings, container, false)
        navController = findNavController()

        navController = findNavController()

        nameInputField = view.findViewById<TextInputLayout>(R.id.nameInputField).apply {
            editText?.apply {
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


        typeInputField = view.findViewById<TextInputLayout>(R.id.typeDropdownLayout).apply {
            val items = Widget.Type.values().map { getString(it.titleResId) }
            val adapter = ArrayAdapter(requireContext(), R.layout.list_item_dropdown, items)
            (editText as? AutoCompleteTextView)?.apply {
                setAdapter(adapter)
                setOnItemClickListener { _, _, position, _ ->
                    widgetSettingsViewModel.selectedTypeId = position
                    validateTypeValue()
                }
                setOnDismissListener {
                    validateTypeValue()
                }
            }
        }

        sizeInputField = view.findViewById<TextInputLayout>(R.id.sizeDropdownLayout).apply {
            val items = Widget.Size.values().map { getString(it.titleResId) }
            val adapter = ArrayAdapter(requireContext(), R.layout.list_item_dropdown, items)
            (editText as? AutoCompleteTextView)?.apply {
                setAdapter(adapter)
                setOnItemClickListener { _, _, position, _ ->
                    widgetSettingsViewModel.selectedSizeId = position
                    validateSizeValue()
                }
                setOnDismissListener {
                    validateSizeValue()
                }
            }
        }

        applyChangesFloatingActionButton =
            view.findViewById<FloatingActionButton>(R.id.applyChangesFloatingActionButton).apply {
                setOnClickListener {
                    if (validateValues()) {
                        val name = nameInputField.editText?.text.toString().trim()
                        val type = Widget.Type.values()[widgetSettingsViewModel.selectedTypeId]
                        val size = Widget.Size.values()[widgetSettingsViewModel.selectedSizeId]
                        val widget = args.widget
                        if (widget != null) {
                            widget.apply {
                                this.name = name
                                this.type = type
                                this.size = size
                            }
                            widgetSettingsViewModel.updateWidget(widget)
                        } else {
                            val newWidget = Widget(
                                name = name,
                                controllerId = args.controller.id,
                                type = type,
                                size = size
                            )
                            widgetSettingsViewModel.addWidget(newWidget)
                            Log.d(LOG_TAG, newWidget.toString())
                        }
                        navController.navigateUp()
                    }
                }
            }

        args.widget?.let {
            nameInputField.editText?.text?.append(it.name)
            (typeInputField.editText as? AutoCompleteTextView)?.apply {
                setText(adapter.getItem(it.type.ordinal).toString(), false)
            }
            (sizeInputField.editText as? AutoCompleteTextView)?.apply {
                setText(adapter.getItem(it.size.ordinal).toString(), false)
            }
        }

        return view
    }

    private fun validateValues(): Boolean =
        validateNameValue() and validateTypeValue() and validateSizeValue()

    private fun validateNameValue(): Boolean =
        if (nameInputField.editText?.text.toString().isNotEmpty()) {
            nameInputField.error = null
            true
        } else {
            nameInputField.error = getString(R.string.invalid_value_message)
            false
        }

    private fun validateTypeValue(): Boolean =
        if (Widget.Type.values().any { it.ordinal == widgetSettingsViewModel.selectedTypeId }) {
            typeInputField.error = null
            true
        } else {
            typeInputField.error = getString(R.string.invalid_value_message)
            false
        }

    private fun validateSizeValue(): Boolean =
        if (Widget.Size.values().any { it.ordinal == widgetSettingsViewModel.selectedSizeId }) {
            sizeInputField.error = null
            true
        } else {
            sizeInputField.error = getString(R.string.invalid_value_message)
            false
        }
}