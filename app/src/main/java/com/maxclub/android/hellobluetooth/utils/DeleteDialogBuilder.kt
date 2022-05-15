package com.maxclub.android.hellobluetooth.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import com.maxclub.android.hellobluetooth.R

class DeleteDialogBuilder {
    companion object {
        fun create(context: Context, itemName: String, onDelete: () -> Unit): AlertDialog =
            AlertDialog.Builder(context)
                .apply {
                    setIcon(R.drawable.ic_baseline_delete_24)
                    setTitle(context.getString(R.string.delete_item_dialog_title))
                    val message = HtmlCompat.fromHtml(
                        context.getString(R.string.delete_item_dialog_message, "<b>$itemName</b>"),
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                    setMessage(message)
                    setNegativeButton(android.R.string.cancel, null)
                    setPositiveButton(android.R.string.ok) { _, _ ->
                        onDelete()
                    }
                    setCancelable(true)
                }.create()
                .apply {
                    window?.setBackgroundDrawableResource(R.drawable.popup_menu_background)
                }
    }
}