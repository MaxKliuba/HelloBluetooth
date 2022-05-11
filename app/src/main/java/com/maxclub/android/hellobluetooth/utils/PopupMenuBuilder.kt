package com.maxclub.android.hellobluetooth.utils

import android.content.Context
import android.os.Build
import android.view.View
import android.widget.PopupMenu
import androidx.annotation.MenuRes
import java.lang.reflect.Method

class PopupMenuBuilder {
    companion object {
        fun create(
            context: Context,
            anchor: View,
            @MenuRes menuRes: Int,
            onMenuItemClickListener: PopupMenu.OnMenuItemClickListener
        ): PopupMenu =
            PopupMenu(context, anchor).apply {
                menuInflater.inflate(menuRes, menu)
                setOnMenuItemClickListener(onMenuItemClickListener)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    setForceShowIcon(true)
                } else {
                    try {
                        val fields = this.javaClass.declaredFields
                        for (field in fields) {
                            if ("mPopup" == field.name) {
                                field.isAccessible = true
                                val menuPopupHelper = field[this]
                                val classPopupHelper =
                                    Class.forName(menuPopupHelper.javaClass.name)
                                val setForceIcons: Method = classPopupHelper.getMethod(
                                    "setForceShowIcon",
                                    Boolean::class.javaPrimitiveType
                                )
                                setForceIcons.invoke(menuPopupHelper, true)
                                break
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
    }
}