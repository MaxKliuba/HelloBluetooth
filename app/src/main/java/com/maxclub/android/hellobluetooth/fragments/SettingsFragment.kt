package com.maxclub.android.hellobluetooth.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputLayout
import com.maxclub.android.hellobluetooth.R
import com.maxclub.android.hellobluetooth.preferences.SettingsPreferences
import com.maxclub.android.hellobluetooth.utils.ArrayAdapterWithIcon
import com.maxclub.android.hellobluetooth.model.Language

class SettingsFragment : Fragment() {
    private lateinit var themeButtonToggleGroup: MaterialButtonToggleGroup
    private lateinit var themeModeTextView: TextView
    private lateinit var languageInputField: TextInputLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        themeButtonToggleGroup =
            view.findViewById<MaterialButtonToggleGroup>(R.id.theme_button_toggle_group).apply {
                addOnButtonCheckedListener { _, checkedId, isChecked ->
                    if (isChecked) {
                        val newMode = when (checkedId) {
                            R.id.light_mode_button -> AppCompatDelegate.MODE_NIGHT_NO
                            R.id.dark_mode_button -> AppCompatDelegate.MODE_NIGHT_YES
                            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                        }
                        SettingsPreferences.setThemeMode(requireContext(), newMode)
                        updateThemeModeTextView()
                        AppCompatDelegate.setDefaultNightMode(newMode)
                    }
                }
            }

        themeModeTextView = view.findViewById(R.id.theme_mode_text_view)
        updateThemeModeTextView()

        languageInputField = view.findViewById(R.id.language_dropdown_layout)

        return view
    }

    override fun onStart() {
        super.onStart()

        (languageInputField.editText as? AutoCompleteTextView)?.apply {
            val items = Language.values()
                .map { ArrayAdapterWithIcon.Item(it.drawableResId, getString(it.titleResId)) }
            val adapter = ArrayAdapterWithIcon(requireContext(), R.layout.list_item_dropdown, items)
            setAdapter(adapter)
            setOnItemClickListener { _, _, position, _ ->
                setItemToLanguageInputField(position)
            }
            val position = SettingsPreferences.getLanguage(requireContext()).ordinal
            setItemToLanguageInputField(position)
        }
    }

    private fun updateThemeModeTextView() {
        when (SettingsPreferences.getThemeMode(requireContext())) {
            AppCompatDelegate.MODE_NIGHT_NO -> {
                themeModeTextView.text = getString(R.string.light_mode_label_text)
                themeButtonToggleGroup.check(R.id.light_mode_button)
            }
            AppCompatDelegate.MODE_NIGHT_YES -> {
                themeModeTextView.text = getString(R.string.dark_mode_label_text)
                themeButtonToggleGroup.check(R.id.dark_mode_button)
            }
            else -> {
                themeModeTextView.text = getString(R.string.auto_mode_label_text)
                themeButtonToggleGroup.check(R.id.auto_mode_button)
            }
        }
    }

    private fun setItemToLanguageInputField(position: Int) {
        val language = Language.values().getOrNull(position) ?: Language.DEFAULT
        (languageInputField.editText as? AutoCompleteTextView)?.setText(
            getString(language.titleResId),
            false
        )
        SettingsPreferences.setLanguage(requireContext(), language)
    }
}