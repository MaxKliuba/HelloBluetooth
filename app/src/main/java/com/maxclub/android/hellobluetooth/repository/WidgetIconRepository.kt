package com.maxclub.android.hellobluetooth.repository

import com.maxclub.android.hellobluetooth.R
import com.maxclub.android.hellobluetooth.model.WidgetIcon

class WidgetIconRepository {
    companion object {
        val widgetIcons: List<WidgetIcon> = listOf(
            WidgetIcon(0, R.string.widget_icon_none),
            WidgetIcon(R.drawable.widget_icons__android_24, R.string.widget_icon_android),
            WidgetIcon(R.drawable.widget_icons__mic_24, R.string.widget_icon_mic),
        )
    }
}