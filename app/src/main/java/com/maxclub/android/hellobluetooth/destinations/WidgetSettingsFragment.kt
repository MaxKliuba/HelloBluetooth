package com.maxclub.android.hellobluetooth.destinations

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.DrawableRes
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

class WidgetSettingsFragment : Fragment() {
    private val widgetSettingsViewModel: WidgetSettingsViewModel by lazy {
        ViewModelProvider(this)[WidgetSettingsViewModel::class.java]
    }
    private val args: WidgetSettingsFragmentArgs by navArgs()
    private lateinit var navController: NavController

    private lateinit var nameInputField: TextInputLayout
    private lateinit var tagInputField: TextInputLayout
    private lateinit var typeInputField: TextInputLayout
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
        tagInputField = view.findViewById<TextInputLayout>(R.id.tag_input_field).apply {
            suffixText = CommandHelper.TAG_TERMINATOR
        }
        typeInputField = view.findViewById(R.id.type_dropdown_layout)
        iconInputField = view.findViewById(R.id.icon_dropdown_layout)
        readonlyCheckBox = view.findViewById(R.id.readonly_check_box)

        applyChangesFloatingActionButton =
            view.findViewById<FloatingActionButton>(R.id.apply_changes_floating_action_button)
                .apply {
                    setOnClickListener {
                        if (validateValues()) {
                            val newName = nameInputField.editText?.text.toString().trim()
                            val newType = widgetSettingsViewModel.selectedType!!
                            val newSize = Widget.Size.SMALL
                            val newTag = tagInputField.editText?.text.toString().trim()
                            val newIconResId =
                                widgetSettingsViewModel.selectedWidgetIcon?.let { widgetIcon ->
                                    if (widgetIcon.isValid) widgetIcon.drawableResId else 0
                                } ?: 0
                            val newIsReadOnly = readonlyCheckBox.isChecked
                            val widget = args.widget
                            if (widget != null) {
                                widget.apply {
                                    name = newName
                                    type = newType
                                    size = newSize
                                    tag = newTag
                                    iconResId = newIconResId
                                    isReadOnly = newIsReadOnly
                                }
                                widgetSettingsViewModel.updateWidget(widget)
                            } else {
                                val newWidget = Widget(
                                    name = newName,
                                    controllerId = args.controller.id,
                                    type = newType,
                                    size = newSize,
                                    tag = newTag,
                                    iconResId = newIconResId,
                                    isReadOnly = newIsReadOnly,
                                )
                                widgetSettingsViewModel.addWidget(newWidget)
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

        (typeInputField.editText as? AutoCompleteTextView)?.apply {
            val items = Widget.Type.values().map { getString(it.titleResId) }
            val adapter = ArrayAdapter(requireContext(), R.layout.list_item_dropdown, items)
            setAdapter(adapter)
            setOnItemClickListener { _, _, position, _ ->
                widgetSettingsViewModel.selectedType = Widget.Type.values().getOrNull(position)
                validateTypeValue()
                updateRelatedValues()
            }
            setOnDismissListener {
                validateTypeValue()
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
            val index = getIndexByDrawableResId(
                widgetSettingsViewModel.selectedWidgetIcon?.drawableResId ?: 0
            )
            setItemToIconInputField(index)
            setOnItemClickListener { _, _, position, _ ->
                setItemToIconInputField(position)
            }
        }

        if (!widgetSettingsViewModel.isValuesUpdated) {
            args.widget?.let { widget ->
                nameInputField.editText?.text?.append(widget.name)
                tagInputField.editText?.text?.append(widget.tag)
                (typeInputField.editText as? AutoCompleteTextView)?.apply {
                    widgetSettingsViewModel.selectedType = widget.type
                    setText(adapter.getItem(widget.type.ordinal).toString(), false)
                }

                iconInputField.apply {
                    val index = getIndexByDrawableResId(widget.iconResId)
                    setItemToIconInputField(index)
                }
                readonlyCheckBox.isChecked = widget.isReadOnly

                updateRelatedValues()
            }
            widgetSettingsViewModel.isValuesUpdated = true
        }
    }

    private fun validateValues(): Boolean =
        validateNameValue() and validateTagValue() and validateTypeValue()

    private fun validateNameValue(): Boolean =
        if (nameInputField.editText?.text.toString().isNotEmpty()) {
            nameInputField.error = null
            true
        } else {
            nameInputField.error = getString(R.string.invalid_value_message)
            false
        }

    private fun validateTypeValue(): Boolean =
        if (Widget.Type.values().contains(widgetSettingsViewModel.selectedType)) {
            typeInputField.error = null
            true
        } else {
            typeInputField.error = getString(R.string.invalid_value_message)
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
        if (validateTypeValue()) {
            when (widgetSettingsViewModel.selectedType) {
                Widget.Type.VOICE_BUTTON -> {
                    val index = getIndexByDrawableResId(R.drawable.widget_icons__mic_24)
                    setItemToIconInputField(index)
                    iconInputField.isEnabled = false
                    readonlyCheckBox.apply {
                        isChecked = false
                        isEnabled = false
                    }
                }
                else -> {
                    iconInputField.isEnabled = true
                    readonlyCheckBox.apply {
                        isEnabled = true
                    }
                }
            }
        }
    }

    private fun getIndexByDrawableResId(@DrawableRes drawableResId: Int): Int =
        widgetSettingsViewModel.widgetIcons.indexOfFirst {
            it.drawableResId == drawableResId
        }

    private fun setItemToIconInputField(position: Int) {
        val widgetIcon = widgetSettingsViewModel.widgetIcons.getOrNull(position)
            ?: widgetSettingsViewModel.widgetIcons[0]
        widgetSettingsViewModel.selectedWidgetIcon = widgetIcon

        (iconInputField.editText as? AutoCompleteTextView)?.apply {
            iconInputField.startIconDrawable =
                ContextCompat.getDrawable(context, widgetIcon.drawableResId)
            setText(getString(widgetIcon.titleResId), false)
        }
    }

    private class ArrayAdapterWithIcon(
        context: Context,
        private val resource: Int,
        private var items: List<WidgetIcon>
    ) : ArrayAdapter<String>(context, resource, items.map { context.getString(it.titleResId) }) {
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
