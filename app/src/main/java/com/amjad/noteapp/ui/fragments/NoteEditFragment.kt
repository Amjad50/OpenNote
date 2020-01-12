package com.amjad.noteapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.amjad.noteapp.data.Note
import com.amjad.noteapp.databinding.FragmentNoteEditBinding
import com.amjad.noteapp.ui.viewmodels.NoteViewModel

class NoteEditFragment : Fragment() {

    companion object {
        const val NOTEID_ARGUMENT = "com.android.noteapp.NoteEditFragment.note_id"
    }

    private lateinit var viewModel: NoteViewModel
    private lateinit var binding: FragmentNoteEditBinding
    private var noteId: Int = -1

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
        noteId = arguments?.getInt(NOTEID_ARGUMENT) ?: -1
        binding = FragmentNoteEditBinding.inflate(inflater, container, false)

        binding.setLifecycleOwner(this)

        binding.model = viewModel

        viewModel.setNoteID(noteId)

        return binding.root
    }

    private fun saveNote() {
        val newNote = Note(binding.titleEdit.text.toString(), binding.noteEdit.text.toString())

        // no id sent mean that this is a new note
        if (noteId != -1) {
            newNote.id = noteId
            viewModel.updateNote(newNote)
        } else {
            viewModel.insert(newNote)
        }
    }

    override fun onStop() {
        super.onStop()
        saveNote()
    }

}
