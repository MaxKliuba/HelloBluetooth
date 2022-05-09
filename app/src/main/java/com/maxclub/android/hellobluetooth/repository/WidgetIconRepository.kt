package com.maxclub.android.hellobluetooth.repository

import com.maxclub.android.hellobluetooth.R
import com.maxclub.android.hellobluetooth.model.WidgetIcon

class WidgetIconRepository {
    companion object {
        val widgetIcons: List<WidgetIcon> = listOf(
            WidgetIcon(
                R.drawable.ic_baseline_image_not_supported_24,
                R.string.widget_icon_none,
                false
            ),
            WidgetIcon(R.drawable.widget_icons__android_24, R.string.widget_icon_android),
            WidgetIcon(R.drawable.widget_icons__arrow_up_24, R.string.widget_icon_arrow_up),
            WidgetIcon(R.drawable.widget_icons__arrow_down_24, R.string.widget_icon_arrow_down),
            WidgetIcon(R.drawable.widget_icons__arrow_left_24, R.string.widget_icon_arrow_left),
            WidgetIcon(R.drawable.widget_icons__arrow_right_24, R.string.widget_icon_arrow_right),
            WidgetIcon(R.drawable.widget_icons__rotate_left_24, R.string.widget_icon_rotate_left),
            WidgetIcon(R.drawable.widget_icons__rotate_right_24, R.string.widget_icon_rotate_right),
            WidgetIcon(R.drawable.widget_icons__stop_24, R.string.widget_icon_stop),
            WidgetIcon(R.drawable.widget_icons__mic_24, R.string.widget_icon_mic),
        )
    }
}