package com.maxclub.android.hellobluetooth.repository

import com.maxclub.android.hellobluetooth.R
import com.maxclub.android.hellobluetooth.model.WidgetIcon

class WidgetIconRepository {
    companion object {
        val widgetIconMap: Map<Int, WidgetIcon> = mapOf(
            0 to WidgetIcon(
                R.drawable.widget_icons__none,
                R.string.widget_icons__none,
                false
            ),

            20 to WidgetIcon(R.drawable.widget_icons__android, R.string.widget_icons__android),
            21 to WidgetIcon(R.drawable.widget_icons__arrow_up, R.string.widget_icons__arrow_up),
            22 to WidgetIcon(
                R.drawable.widget_icons__arrow_down,
                R.string.widget_icons__arrow_down
            ),
            23 to WidgetIcon(
                R.drawable.widget_icons__arrow_left,
                R.string.widget_icons__arrow_left
            ),
            24 to WidgetIcon(
                R.drawable.widget_icons__arrow_right,
                R.string.widget_icons__arrow_right
            ),
            25 to WidgetIcon(
                R.drawable.widget_icons__rotate_left,
                R.string.widget_icons__rotate_left
            ),
            26 to WidgetIcon(
                R.drawable.widget_icons__rotate_right,
                R.string.widget_icons__rotate_right
            ),
            27 to WidgetIcon(R.drawable.widget_icons__stop, R.string.widget_icons__stop),
            28 to WidgetIcon(R.drawable.widget_icons__speed, R.string.widget_icons__speed),
            29 to WidgetIcon(R.drawable.widget_icons__lightbulb, R.string.widget_icons__lightbulb),
            30 to WidgetIcon(R.drawable.widget_icons__power, R.string.widget_icons__power),
            31 to WidgetIcon(R.drawable.widget_icons__battery, R.string.widget_icons__battery),
            32 to WidgetIcon(R.drawable.widget_icons__volume, R.string.widget_icons__volume),
            33 to WidgetIcon(R.drawable.widget_icons__time, R.string.widget_icons__time),
            34 to WidgetIcon(
                R.drawable.widget_icons__temperature,
                R.string.widget_icons__temperature
            ),

            /*
             * Types
             */
            1 to WidgetIcon(R.drawable.widget_types__button, R.string.widget_types__button),
            2 to WidgetIcon(R.drawable.widget_types__switch, R.string.widget_types__switch),
            3 to WidgetIcon(R.drawable.widget_types__slider, R.string.widget_types__slider),
            4 to WidgetIcon(R.drawable.widget_types__text_field, R.string.widget_types__text_field),
            5 to WidgetIcon(R.drawable.widget_types__mic, R.string.widget_icons__mic),
            /*
             * [6 - 19] Reserved for new types
             */
        )

        val widgetIcons: List<WidgetIcon>
            get() = widgetIconMap.values.toList()
    }
}