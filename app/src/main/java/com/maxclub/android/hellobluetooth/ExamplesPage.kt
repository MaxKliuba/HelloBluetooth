package com.maxclub.android.hellobluetooth

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

class ExamplesPage {
    companion object {
        private const val URL = "https://github.com/MaxKliuba/HelloBluetooth-Examples"

        fun launch(context: Context) {
            CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
                .launchUrl(context, Uri.parse(URL))
        }
    }
}