package com.amjad.noteapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.amjad.noteapp.R
import com.amjad.noteapp.databinding.NoteslistFragmentBinding
import com.amjad.noteapp.ui.adapters.NotesListAdapter
import com.amjad.noteapp.ui.viewmodels.NoteViewModel

class NotesListFragment : Fragment() {
    private lateinit var viewModel: NoteViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = NoteslistFragmentBinding.inflate(inflater, container, false)
        val adapter = NotesListAdapter()
        binding.noteslistview.adapter = adapter
        observersInit(adapter)

        binding.setOnNewNoteClick {
            openNewNote()
        }

        return binding.root
    }

    private fun openNewNote() {
        // not sending any id means that this is new note
        findNavController()
            .navigate(R.id.action_mainFragment_to_noteEditFragment)
    }

    private fun observersInit(adapter: NotesListAdapter) {
        viewModel.allNotes.observe(this, Observer { notes -> adapter.submitList(notes) })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProviders.of(this)[NoteViewModel::class.java]
        } ?: throw Exception("Invalid Activity")
    }

}
