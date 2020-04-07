package com.amjad.opennote.ui.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.amjad.opennote.MainActivity
import com.amjad.opennote.R
import com.amjad.opennote.ui.dialogs.ColorChooseDialog
import com.amjad.opennote.ui.viewmodels.NoteEditViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

abstract class BaseNoteEditFragment : Fragment() {
    protected lateinit var viewModel: NoteEditViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[NoteEditViewModel::class.java]

        setHasOptionsMenu(true)
    }

    override fun onStart() {
        super.onStart()

        // not sure if its ok to setup observers every time onStart
        setupActionBar()
    }

    private fun setupActionBar() {
        // remove the title from the actionBar
        (activity as MainActivity?)?.run {
            supportActionBar?.title = null

            // save old style of the actionBar
            viewModel.oldStatusAndActionBarStyles.run {
                if (!saved) {
                    background = getColor(R.color.colorPrimary)
                    elevation = supportActionBar?.elevation ?: 0f
                    statusBarColor = window.statusBarColor
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

    private fun changeActionBarStyles(background: Int, elevation: Float, statusBarColor: Int) {
        (activity as MainActivity?)?.run {
            supportActionBar?.setBackgroundDrawable(ColorDrawable(background))
            supportActionBar?.elevation = elevation
            window.statusBarColor = statusBarColor
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.note_edit_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit_menu_color_action -> {
                parentFragmentManager.also { fragmentManager ->
                    ColorChooseDialog().setOnColorClick { color ->
                        viewModel.note.value?.color = color

                        viewModel.notifyNoteUpdated()
                    }.show(fragmentManager, "ColorChooseDialogInEdit")
                }
                true
            }
            R.id.edit_menu_add_image_action -> {
                startActivityForResult(
                    Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    ),
                    REQUEST_ADD_IMAGE_ACTION
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ADD_IMAGE_ACTION
            && resultCode == Activity.RESULT_OK
            && data != null
        ) {
            val uri = data.data

            Glide.with(this).asBitmap().load(uri).into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {
                }

                override fun onResourceReady(
                    bitmap: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    try {
                        // TODO: add error handling and more robust structure
                        val imagesFolder = File(context?.filesDir, "images")
                        imagesFolder.mkdir()

                        val uuid = UUID.randomUUID().toString()
                        viewModel.addImage(uuid)

                        val savedImage = File(imagesFolder, "$uuid.png")

                        val output = FileOutputStream(savedImage)
                        bitmap.compress(Bitmap.CompressFormat.PNG, 90, output)
                        output.flush()
                        output.close()
                    } catch (e: Exception) {
                        when (e) {
                            is FileNotFoundException, is IOException -> {
                                Toast.makeText(
                                    context,
                                    "Error happened when trying to save the image",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            else -> throw e
                        }
                    }
                }
            })
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

        viewModel.oldStatusAndActionBarStyles.run {
            changeActionBarStyles(background, elevation, statusBarColor)
            // to save again if this is just a configuration change
            saved = false
        }
    }

    companion object {
        const val NEW_NOTE_ID = -1L
        const val REQUEST_ADD_IMAGE_ACTION = 6
    }
}