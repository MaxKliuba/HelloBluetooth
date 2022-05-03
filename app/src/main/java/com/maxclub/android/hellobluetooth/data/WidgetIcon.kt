package com.maxclub.android.hellobluetooth.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class WidgetIcon(
    @DrawableRes val drawableResId: Int,
    @StringRes val title: Int,
)