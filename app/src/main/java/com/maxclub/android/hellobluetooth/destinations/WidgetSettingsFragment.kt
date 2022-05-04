package com.maxclub.android.hellobluetooth.destinations

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import com.maxclub.android.hellobluetooth.R
import com.maxclub.android.hellobluetooth.data.Widget
import com.maxclub.android.hellobluetooth.utils.CommandHelper
import com.maxclub.android.hellobluetooth.model.WidgetIcon
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
    private lateinit var tagInputField: TextInputLayout
    private lateinit var iconInputField: TextInputLayout
    private lateinit var readonlyCheckBox: CheckBox
    private lateinit var applyChangesFloatingActionButton: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_widget_settings, container, false)
        navController = findNavController()

        nameInputField = view.findViewById(R.id.name_input_field)
        typeInputField = view.findViewById(R.id.type_dropdown_layout)
        sizeInputField = view.findViewById(R.id.size_dropdown_layout)
        tagInputField = view.findViewById<TextInputLayout>(R.id.tag_input_field).apply {
            suffixText = CommandHelper.TAG_TERMINATOR
        }
        iconInputField = view.findViewById(R.id.icon_dropdown_layout)
        readonlyCheckBox = view.findViewById(R.id.readonly_check_box)

        applyChangesFloatingActionButton =
            view.findViewById<FloatingActionButton>(R.id.apply_changes_floating_action_button)
                .apply {
                    setOnClickListener {
                        if (validateValues()) {
                            val name = nameInputField.editText?.text.toString().trim()
                            val type = Widget.Type.values()[widgetSettingsViewModel.selectedTypeId]
                            val size = Widget.Size.values()[widgetSettingsViewModel.selectedSizeId]
                            val tag = tagInputField.editText?.text.toString().trim()
                            val iconResId = widgetSettingsViewModel.selectedIconResId
                            val isReadOnly = readonlyCheckBox.isChecked
                            val widget = args.widget
                            if (widget != null) {
                                widget.apply {
                                    this.name = name
                                    this.type = type
                                    this.size = size
                                    this.tag = tag
                                    this.iconResId = iconResId
                                    this.isReadOnly = isReadOnly
                                }
                                widgetSettingsViewModel.updateWidget(widget)
                            } else {
                                val newWidget = Widget(
                                    name = name,
                                    controllerId = args.controller.id,
                                    type = type,
                                    size = size,
                                    tag = tag,
                                    iconResId = iconResId,
                                    isReadOnly = isReadOnly,
                                )
                                widgetSettingsViewModel.addWidget(newWidget)
                                Log.d(LOG_TAG, newWidget.toString())
                            }
                            navController.navigateUp()
                        }
                    }
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

        (typeInputField.editText as? AutoCompleteTextView)?.apply {
            val items = Widget.Type.values().map { getString(it.titleResId) }
            val adapter = ArrayAdapter(requireContext(), R.layout.list_item_dropdown, items)
            setAdapter(adapter)
            setOnItemClickListener { _, _, position, _ ->
                widgetSettingsViewModel.selectedTypeId = position
                validateTypeValue()
                updateRelatedValues()
            }
            setOnDismissListener {
                validateTypeValue()
            }
        }

        (sizeInputField.editText as? AutoCompleteTextView)?.apply {
            val items = Widget.Size.values().map { getString(it.titleResId) }
            val adapter = ArrayAdapter(requireContext(), R.layout.list_item_dropdown, items)
            setAdapter(adapter)
            setOnItemClickListener { _, _, position, _ ->
                widgetSettingsViewModel.selectedSizeId = position
                validateSizeValue()
            }
            setOnDismissListener {
                validateSizeValue()
            }
        }

        tagInputField.editText?.apply {
            doOnTextChanged { _, _, _, _ ->
                validateTagValue()
            }
            setOnFocusChangeListener { _, isFocused ->
                if (!isFocused) {
                    validateTagValue()
                }
            }
        }

        (iconInputField.editText as? AutoCompleteTextView)?.apply {
            val adapter =
                ArrayAdapterWithIcon(
                    requireContext(),
                    R.layout.list_item_dropdown,
                    widgetSettingsViewModel.widgetIcons
                )
            setAdapter(adapter)
            setText(adapter.getItem(0).toString(), false)
            setOnItemClickListener { _, _, position, _ ->
                val drawableResId = widgetSettingsViewModel.widgetIcons[position].drawableResId
                iconInputField.startIconDrawable = if (drawableResId != 0) {
                    ContextCompat.getDrawable(context, drawableResId)
                } else {
                    null
                }
                widgetSettingsViewModel.selectedIconResId = drawableResId
            }
        }

        args.widget?.let {
            nameInputField.editText?.text?.append(it.name)
            (typeInputField.editText as? AutoCompleteTextView)?.apply {
                val itemId = it.type.ordinal
                setText(adapter.getItem(itemId).toString(), false)
                widgetSettingsViewModel.selectedTypeId = itemId
            }
            (sizeInputField.editText as? AutoCompleteTextView)?.apply {
                val itemId = it.size.ordinal
                setText(adapter.getItem(itemId).toString(), false)
                widgetSettingsViewModel.selectedSizeId = itemId
            }
            tagInputField.editText?.text?.append(it.tag)
            (iconInputField.editText as? AutoCompleteTextView)?.apply {
                val drawableResId = it.iconResId
                val itemId = widgetSettingsViewModel.widgetIcons.indexOfFirst { widgetIcon ->
                    widgetIcon.drawableResId == drawableResId
                }
                if (itemId >= 0) {
                    iconInputField.startIconDrawable = if (drawableResId != 0) {
                        ContextCompat.getDrawable(context, drawableResId)
                    } else {
                        null
                    }
                    setText(adapter.getItem(itemId).toString(), false)
                    widgetSettingsViewModel.selectedIconResId = drawableResId
                }
            }
            readonlyCheckBox.isChecked = it.isReadOnly

            updateRelatedValues()
        }
    }

    private fun validateValues(): Boolean =
        validateNameValue() and validateTypeValue() and validateSizeValue() and validateTagValue()

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

    private fun validateTagValue(): Boolean =
        if (tagInputField.editText?.text.toString().isNotEmpty()) {
            tagInputField.error = null
            true
        } else {
            tagInputField.error = getString(R.string.invalid_value_message)
            false
        }

    private fun updateRelatedValues() {
        // TODO
        if (validateTypeValue()) {
            when (Widget.Type.values()[widgetSettingsViewModel.selectedTypeId]) {
//                Widget.Type.BUTTON -> {
//                    readonlyCheckBox.apply {
//                        isChecked = false
//                        isEnabled = false
//                    }
//                }
                else -> {
                    readonlyCheckBox.apply {
                        isEnabled = true
                    }
                }
            }
        }
    }

    class ArrayAdapterWithIcon(
        context: Context,
        private val resource: Int,
        private var items: List<WidgetIcon>
    ) : ArrayAdapter<String>(context, resource, items.map { context.getString(it.title) }) {
        private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
            (layoutInflater.inflate(
                resource,
                parent,
                false
            ) as TextView).apply {
                text = getItem(position)
                setCompoundDrawablesWithIntrinsicBounds(
                    items[position].drawableResId,
                    0,
                    0,
                    0
                )
            }
    }
}
