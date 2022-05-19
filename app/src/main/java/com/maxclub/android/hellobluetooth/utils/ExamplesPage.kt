package com.maxclub.android.hellobluetooth.utils

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

class ExamplesPage {
    fun launch(context: Context) {
        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
            .launchUrl(context, Uri.parse(URL))
    }

    companion object {
        const val URL = "https://github.com/MaxKliuba/HelloBluetooth-Examples"
    }
}