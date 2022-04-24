package com.maxclub.android.hellobluetooth.destinations

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

private const val URL = "https://github.com/MaxKliuba/HelloBluetooth-Examples"

class ExamplesPage {
    fun launch(context: Context) {
        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
            .launchUrl(context, Uri.parse(URL))
    }
}