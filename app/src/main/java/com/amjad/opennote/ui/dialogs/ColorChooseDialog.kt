package com.amjad.opennote.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.amjad.opennote.databinding.ColorChooseDialogLayoutBinding

class ColorChooseDialog : DialogFragment() {

    private var handler: ((Int) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { activity ->
            val binding =
                ColorChooseDialogLayoutBinding.inflate(LayoutInflater.from(activity), null, false)
            val builder = AlertDialog.Builder(activity)

            binding.setOnClick {
                handler?.invoke(it.backgroundTintList?.defaultColor ?: Color.WHITE)

                // to finish the dialog
                dialog?.cancel()
            }

            builder.setView(binding.root)

            builder.create()

        } ?: throw IllegalStateException("Activity cannot be null")
    }

    fun setOnColorClick(handler: (Int) -> Unit): ColorChooseDialog {
        this.handler = handler
        return this
    }
}