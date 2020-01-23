package com.amjad.opennote.ui.fragments

import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.amjad.opennote.MainActivity
import com.amjad.opennote.R
import com.amjad.opennote.databinding.NoteEditFragmentBinding
import com.amjad.opennote.ui.dialogs.ColorChooseDialog
import com.amjad.opennote.ui.viewmodels.NoteEditViewModel


class NoteEditFragment : Fragment() {

    private val args: NoteEditFragmentArgs by navArgs()

    private lateinit var viewModel: NoteEditViewModel
    private lateinit var binding: NoteEditFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this)[NoteEditViewModel::class.java]

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = NoteEditFragmentBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = this

        binding.model = viewModel

        // start editing right away if this is a new note and insert the note into the database
        if (args.noteId == NEW_NOTE_ID) {
            if (!viewModel.isNoteSelected) {
                requestFocusAndShowKeyboard(binding.noteEdit)

                viewModel.insertNewNote()
            }
        } else
        // this might be redundant if the id was already set
            viewModel.setNoteID(args.noteId)

        setupActionBar()

        return binding.root
    }

    private fun setupActionBar() {
        // remove the title from the actionBar
        (activity as MainActivity?)?.run {
            supportActionBar?.title = null
        }
    }

    private fun requestFocusAndShowKeyboard(noteEdit: View) {
        if (noteEdit.requestFocusFromTouch())
            context?.also {
                val inputMethodManager = getSystemService(it, InputMethodManager::class.java)
                inputMethodManager?.toggleSoftInput(SHOW_IMPLICIT, 0)
            }
    }

    override fun onStop() {
        super.onStop()

        // hide the keyboard
        context?.also {
            val inputMethodManager = getSystemService(it, InputMethodManager::class.java)
            inputMethodManager?.hideSoftInputFromWindow(binding.root.windowToken, 0)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.note_edit_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit_menu_color_action -> {
                fragmentManager?.also { fragmentManager ->
                    ColorChooseDialog().setOnColorClick { color ->
                        viewModel.note.value?.color = color

                        // so we can update the value to the observers
                        (viewModel.note as MutableLiveData).run {
                            value = value
                        }
                    }.show(fragmentManager, "ColorChooseDialogInEdit")
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val NEW_NOTE_ID = -1L
    }
}
