package com.amjad.opennote.ui.fragments

import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.amjad.opennote.MainActivity
import com.amjad.opennote.R
import com.amjad.opennote.ui.viewmodels.BaseNoteViewModel

abstract class BaseBaseNoteFragment : Fragment() {
    protected abstract val viewModel: BaseNoteViewModel

    override fun onStart() {
        super.onStart()

        // not sure if its ok to setup observers every time onStart
        setupActionBar()
    }

    open protected fun setupActionBar() {
        // remove the title from the actionBar
        (activity as MainActivity?)?.run {
            supportActionBar?.title = null

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

    protected fun changeActionBarStyles(background: Int, elevation: Float, statusBarColor: Int) {
        (activity as MainActivity?)?.run {
            // use customView to store the color of the actionBar, to be restored later
            // we don't want to use customView, its just for storing purpose
            supportActionBar?.setDisplayShowCustomEnabled(false)
            // create a new view just to hold the color
            supportActionBar?.customView = View(context).apply {
                setBackgroundColor(background)
            }
            supportActionBar?.setBackgroundDrawable(ColorDrawable(background))
            supportActionBar?.elevation = elevation
            window.statusBarColor = statusBarColor
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.oldStatusAndActionBarStyles.run {
            if (saved)
                changeActionBarStyles(background, elevation, statusBarColor)
            // to save again if this is just a configuration change
            saved = false
        }
    }

    companion object {
        const val NEW_NOTE_ID = -1L
    }
}