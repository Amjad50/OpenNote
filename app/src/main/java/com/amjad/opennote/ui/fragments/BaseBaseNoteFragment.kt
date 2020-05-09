package com.amjad.opennote.ui.fragments

import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.amjad.opennote.MainActivity
import com.amjad.opennote.R
import com.amjad.opennote.databinding.ActionBarTitleBinding
import com.amjad.opennote.ui.viewmodels.BaseNoteViewModel

abstract class BaseBaseNoteFragment : Fragment() {
    protected abstract val viewModel: BaseNoteViewModel

    override fun onStart() {
        super.onStart()

        // not sure if its ok to setup observers every time onStart
        setupActionBar()
    }

    protected open fun setupActionBar() {
        (activity as MainActivity?)?.run {
            // remove the title from the actionBar
            // and enable the editable title view
            supportActionBar?.setDisplayShowCustomEnabled(true)
            supportActionBar?.setDisplayShowTitleEnabled(false)

            // build the title view, and pass the model to get the title from it
            val binding = ActionBarTitleBinding.inflate(layoutInflater)
            binding.model = viewModel
            binding.lifecycleOwner = this@BaseBaseNoteFragment

            supportActionBar?.customView = binding.root

            // save old style of the actionBar
            viewModel.oldStatusAndActionBarStyles.run {
                if (!saved) {
                    // get the stored color if present, else fallback to the colorPrimary which is
                    // the color of the root's actionBar
                    background = (supportActionBar?.customView?.background as ColorDrawable?)?.color
                        ?: getColor(R.color.colorPrimary)
                    elevation = supportActionBar?.elevation ?: 0f
                    statusBarColor = window.statusBarColor
                    saved = true
                }
            }

            // change style
            viewModel.note.observe(viewLifecycleOwner, Observer {
                // computer the dark color of a given color
                // this dark color configuration matches (very close to) the dark difference
                // from the PrimaryColor and colorPrimaryDark
                val darkColor = ColorUtils.HSLToColor(FloatArray(3).apply {
                    ColorUtils.colorToHSL(
                        it.color,
                        this
                    )
                    this[1] *= .5f
                    this[2] *= .62f
                })
                changeActionBarStyles(it.color, 0f, darkColor)
            })
        }
    }

    protected fun restoreActionBar() {
        viewModel.oldStatusAndActionBarStyles.run {
            if (saved)
                changeActionBarStyles(background, elevation, statusBarColor)
            // to save again if this is just a configuration change
            saved = false
        }
    }

    protected fun changeActionBarStyles(background: Int, elevation: Float, statusBarColor: Int) {
        (activity as MainActivity?)?.run {
            // if there is no customView present, then create an empty one just to store the color
            if (supportActionBar?.customView == null)
                supportActionBar?.customView = View(context)

            // use customView to store the color of the actionBar, to be restored later
            supportActionBar?.customView?.apply {
                setBackgroundColor(background)
            }

            supportActionBar?.setBackgroundDrawable(ColorDrawable(background))
            supportActionBar?.elevation = elevation
            window.statusBarColor = statusBarColor
        }
    }


    override fun onStop() {
        super.onStop()

        // hide the keyboard
        context?.also {
            val inputMethodManager =
                ContextCompat.getSystemService(it, InputMethodManager::class.java)
            inputMethodManager?.hideSoftInputFromWindow(view?.windowToken, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        restoreActionBar()
    }

    companion object {
        const val NEW_NOTE_ID = -1L
    }
}