package com.amjad.opennote.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import java.io.File

fun requestFocusAndShowKeyboard(view: View, context: Context?) {
    if (view.requestFocusFromTouch())
        context?.also {
            val inputMethodManager =
                ContextCompat.getSystemService(it, InputMethodManager::class.java)
            // only shows the keyboard, if it is shown, it will not hide it
            inputMethodManager?.toggleSoftInput(
                InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY
            )
        }
}

@BindingAdapter("noteImage")
fun loadImage(view: ImageView, uuid: String?) {
    if (!uuid.isNullOrBlank()) {
        val image = File(view.context.filesDir, "images/$uuid.png")
        Glide.with(view.context)
            .load(image)
            .into(view)
    } else {
        view.setImageDrawable(null)
    }
}
