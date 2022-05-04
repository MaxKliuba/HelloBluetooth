package com.maxclub.android.hellobluetooth.destinations

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.button.MaterialButtonToggleGroup
import com.maxclub.android.hellobluetooth.R
import com.maxclub.android.hellobluetooth.preferences.SettingsPreferences

class SettingsFragment : Fragment() {
    private lateinit var themeButtonToggleGroup: MaterialButtonToggleGroup
    private lateinit var themeModeTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        themeModeTextView = view.findViewById(R.id.theme_mode_text_view)

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

        updateThemeModeTextView()

        return view
    }

    private fun updateThemeModeTextView() {
        when (SettingsPreferences.getThemeMode(requireContext())) {
            AppCompatDelegate.MODE_NIGHT_NO -> {
                themeModeTextView.text = getString(R.string.light_mode_label)
                themeButtonToggleGroup.check(R.id.light_mode_button)
            }
            AppCompatDelegate.MODE_NIGHT_YES -> {
                themeModeTextView.text = getString(R.string.dark_mode_label)
                themeButtonToggleGroup.check(R.id.dark_mode_button)
            }
            else -> {
                themeModeTextView.text = getString(R.string.auto_mode_label)
                themeButtonToggleGroup.check(R.id.auto_mode_button)
            }
        }
    }
}