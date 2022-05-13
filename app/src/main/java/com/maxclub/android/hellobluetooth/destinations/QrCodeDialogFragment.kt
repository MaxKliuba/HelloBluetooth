package com.maxclub.android.hellobluetooth.destinations

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.maxclub.android.hellobluetooth.R
import com.maxclub.android.hellobluetooth.data.ControllerWithWidgets
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.glxn.qrgen.android.QRCode

class QrCodeDialogFragment : DialogFragment() {
    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val controllerWithWidgets: ControllerWithWidgets =
            arguments?.getSerializable(ARG_CONTROLLER_WITH_WIDGETS) as ControllerWithWidgets

        val view = try {
            val json: String = Json.encodeToString(controllerWithWidgets)
            val qrCodeBitmap: Bitmap = QRCode.from(json)
                .withCharset(Charsets.UTF_8.name())
                .withSize(QR_CODE_SIZE, QR_CODE_SIZE)
                .bitmap()

            layoutInflater.inflate(R.layout.dialog_fragment_qr_code, null).also { view ->
                view.findViewById<TextView>(R.id.controller_name_text_view).apply {
                    text = controllerWithWidgets.controller.name
                }
                view.findViewById<TextView>(R.id.widgets_count_text_view).apply {
                    text = resources.getQuantityString(
                        R.plurals.widget_plural,
                        controllerWithWidgets.widgets.size,
                        controllerWithWidgets.widgets.size
                    )
                }
                view.findViewById<ImageView>(R.id.qr_code_image_view).apply {
                    setImageBitmap(qrCodeBitmap)
                }
            }
        } catch (e: Exception) {
            layoutInflater.inflate(R.layout.dialog_fragment_qr_code_error, null).also { view ->
                view.findViewById<ImageView>(R.id.error_image_view).also { imageView ->
                    // TODO
                    imageView.setImageResource(R.drawable.ic_baseline_qr_code_24)
                }
                view.findViewById<TextView>(R.id.error_message_text_view).apply {
                    text =
                        getString(
                            R.string.qr_code_error_message,
                            controllerWithWidgets.controller.name
                        )
                }
            }
        }

        return AlertDialog.Builder(requireContext())
            .apply {
                setView(view)
                setCancelable(true)
            }.create()
            .apply {
                window?.setBackgroundDrawableResource(R.drawable.dialog_fragment_background)
            }
    }

    companion object {
        const val TAG = "QrCodeDialogFragment"
        private const val ARG_CONTROLLER_WITH_WIDGETS = "controllerWithWidgets"
        private const val QR_CODE_SIZE = 2048

        fun newInstance(controllerWithWidgets: ControllerWithWidgets) =
            QrCodeDialogFragment().apply {
                arguments = bundleOf(ARG_CONTROLLER_WITH_WIDGETS to controllerWithWidgets)
            }
    }
}