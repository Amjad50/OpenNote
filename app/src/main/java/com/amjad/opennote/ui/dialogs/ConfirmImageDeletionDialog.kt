package com.amjad.opennote.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.amjad.opennote.R

class ConfirmImageDeletionDialog : DialogFragment() {

    private var handler: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { activity ->
            val builder = AlertDialog.Builder(activity)

            builder.setMessage(R.string.delete_image_dialog_message)
                .setPositiveButton(R.string.okay) { _, _ ->
                    handler?.invoke()
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }

            builder.create()

        } ?: throw IllegalStateException("Activity cannot be null")
    }

    fun setOnConfirm(handler: () -> Unit): ConfirmImageDeletionDialog {
        this.handler = handler
        return this
    }
}