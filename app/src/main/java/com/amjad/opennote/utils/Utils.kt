package com.amjad.opennote.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat


fun requestFocusAndShowKeyboard(view: View, context: Context?) {
    if (view.requestFocusFromTouch())
        context?.also {
            val inputMethodManager =
                ContextCompat.getSystemService(it, InputMethodManager::class.java)
            if (inputMethodManager?.isAcceptingText != true)
                inputMethodManager?.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, -1)
        }
}

