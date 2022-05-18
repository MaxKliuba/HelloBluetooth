package com.maxclub.android.hellobluetooth.model

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.maxclub.android.hellobluetooth.R
import java.util.*

enum class Language(
    @DrawableRes val drawableResId: Int,
    @StringRes val titleResId: Int,
    val languageName: String
) {
    @SuppressLint("ConstantLocale")
    DEFAULT(
        R.drawable.ic_language_default_24,
        R.string.default_language_title,
        Locale.getDefault().language
    ),
    ENGLISH(
        R.drawable.ic_language_english_24,
        R.string.english_language_title,
        Locale.US.language
    ),
}