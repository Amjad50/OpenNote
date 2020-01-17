package com.amjad.noteapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.amjad.noteapp.databinding.NoteEditFragmentBinding
import com.amjad.noteapp.ui.viewmodels.NoteViewModel


class NoteEditFragment : Fragment() {

    private val args: NoteEditFragmentArgs by navArgs()

    private lateinit var viewModel: NoteViewModel
    private lateinit var binding: NoteEditFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProviders.of(this)[NoteViewModel::class.java]
        } ?: throw Exception("Invalid Activity")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = NoteEditFragmentBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = this

        binding.model = viewModel

        // start editing right away if this is a new note
        if (args.noteId == NEW_NOTE_ID)
            requestFocusAndShowKeyboard(binding.noteEdit)
        else
        // only set the id on notes already exists,
        // new notes are handlerd by NoteListFragment when creating a new note
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

    private fun saveNote() {
        // TODO: dont change the date if value of note did not change: add diff comparator
        // ^ handle difference changes and date assignment

        // no id sent mean that this is a new note
        if (args.noteId == NEW_NOTE_ID) {
            viewModel.insertCurrentNote()
        } else {
            viewModel.updateCurrentNote()
        }
    }

    override fun onStop() {
        super.onStop()
        // only save if not changing configuration (rotating)
        if (activity?.isChangingConfigurations != true)
            saveNote()

        // hide the keyboard
        context?.also {
            val inputMethodManager = getSystemService(it, InputMethodManager::class.java)
            inputMethodManager?.hideSoftInputFromWindow(binding.root.windowToken, 0)
        }
    }

    companion object {
        const val NEW_NOTE_ID = -1L
    }
}
