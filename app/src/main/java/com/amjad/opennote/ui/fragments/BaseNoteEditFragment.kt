package com.amjad.opennote.ui.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.amjad.opennote.MainActivity
import com.amjad.opennote.R
import com.amjad.opennote.ui.viewmodels.NoteEditViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

abstract class BaseNoteEditFragment : BaseBaseNoteFragment() {

    override lateinit var viewModel: NoteEditViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[NoteEditViewModel::class.java]

        setHasOptionsMenu(true)
    }

    override fun setupActionBar() {
        super.setupActionBar()

        // after setup just hide the new custom view, as this should be shown only on FolderNotes
        (activity as MainActivity?)?.run {
            supportActionBar?.setDisplayShowCustomEnabled(false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.note_edit_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
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

    private fun addImageToNote(uri: Uri) {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ADD_IMAGE_ACTION
            && resultCode == Activity.RESULT_OK
            && data != null
        ) {
            data.data?.also {
                addImageToNote(it)
            }
        }
    }

    companion object {
        const val REQUEST_ADD_IMAGE_ACTION = 6
    }
}