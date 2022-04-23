package com.maxclub.android.hellobluetooth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.button.MaterialButtonToggleGroup

class SettingsFragment : Fragment() {
    private lateinit var themeButtonToggleGroup: MaterialButtonToggleGroup
    private lateinit var themeModeTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        themeModeTextView = view.findViewById(R.id.themeModeTextView)

        themeButtonToggleGroup =
            view.findViewById<MaterialButtonToggleGroup>(R.id.themeButtonToggleGroup).apply {
                addOnButtonCheckedListener { _, checkedId, isChecked ->
                    if (isChecked) {
                        val newMode = when (checkedId) {
                            R.id.lightModeButton -> AppCompatDelegate.MODE_NIGHT_NO
                            R.id.darkModeButton -> AppCompatDelegate.MODE_NIGHT_YES
                            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                        }
                        ThemePreferences.setMode(requireContext(), newMode)
                        updateThemeModeTextView()
                        AppCompatDelegate.setDefaultNightMode(newMode)
                    }
                }
            }

        updateThemeModeTextView()

        return view
    }

    private fun updateThemeModeTextView() {
        when (ThemePreferences.getMode(requireContext())) {
            AppCompatDelegate.MODE_NIGHT_NO -> {
                themeModeTextView.text = getString(R.string.light_mode_label)
                themeButtonToggleGroup.check(R.id.lightModeButton)
            }
            AppCompatDelegate.MODE_NIGHT_YES -> {
                themeModeTextView.text = getString(R.string.dark_mode_label)
                themeButtonToggleGroup.check(R.id.darkModeButton)
            }
            else -> {
                themeModeTextView.text = getString(R.string.auto_mode_label)
                themeButtonToggleGroup.check(R.id.autoModeButton)
            }
        }
    }
}