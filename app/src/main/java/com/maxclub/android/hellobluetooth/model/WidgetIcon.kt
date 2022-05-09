package com.maxclub.android.hellobluetooth.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class WidgetIcon(
    @DrawableRes val drawableResId: Int,
    @StringRes val titleResId: Int,
    val isValid: Boolean = true,
)