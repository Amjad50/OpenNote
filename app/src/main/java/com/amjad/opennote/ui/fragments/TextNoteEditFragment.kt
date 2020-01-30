package com.amjad.opennote.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.fragment.navArgs
import com.amjad.opennote.data.entities.NoteType
import com.amjad.opennote.databinding.TextNoteEditFragmentBinding


class TextNoteEditFragment : BaseNoteEditFragment() {

    private val args: TextNoteEditFragmentArgs by navArgs()

    private lateinit var binding: TextNoteEditFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TextNoteEditFragmentBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = this

        binding.model = viewModel

        // start editing right away if this is a new note and insert the note into the database
        if (args.noteId == NEW_NOTE_ID) {
            if (!viewModel.isNoteSelected) {
                requestFocusAndShowKeyboard(binding.noteEdit)

                viewModel.insertNewNote(NoteType.TEXT_NOTE)
            }
        } else
        // this might be redundant if the id was already set
            viewModel.setNoteID(args.noteId)

        return binding.root
    }

    private fun requestFocusAndShowKeyboard(noteEdit: View) {
        if (noteEdit.requestFocusFromTouch())
            context?.also {
                val inputMethodManager = getSystemService(it, InputMethodManager::class.java)
                inputMethodManager?.toggleSoftInput(SHOW_IMPLICIT, 0)
            }
    }

    override fun onStop() {
        // hide the keyboard
        context?.also {
            val inputMethodManager = getSystemService(it, InputMethodManager::class.java)
            inputMethodManager?.hideSoftInputFromWindow(binding.root.windowToken, 0)
        }

        super.onStop()
    }
}
