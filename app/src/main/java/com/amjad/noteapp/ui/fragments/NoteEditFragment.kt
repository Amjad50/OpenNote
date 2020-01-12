package com.amjad.noteapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.amjad.noteapp.data.Note
import com.amjad.noteapp.databinding.FragmentNoteEditBinding
import com.amjad.noteapp.ui.viewmodels.NoteViewModel

class NoteEditFragment : Fragment() {

    val args: NoteEditFragmentArgs by navArgs()

    private lateinit var viewModel: NoteViewModel
    private lateinit var binding: FragmentNoteEditBinding

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
        binding = FragmentNoteEditBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = this

        binding.model = viewModel

        viewModel.setNoteID(args.noteId)

        return binding.root
    }

    private fun saveNote() {
        val newNote = Note(binding.titleEdit.text.toString(), binding.noteEdit.text.toString())

        // no id sent mean that this is a new note
        if (args.noteId != -1) {
            newNote.id = args.noteId
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
