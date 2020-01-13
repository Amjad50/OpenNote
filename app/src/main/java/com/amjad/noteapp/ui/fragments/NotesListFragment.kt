package com.amjad.noteapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import com.amjad.noteapp.databinding.NoteslistFragmentBinding
import com.amjad.noteapp.ui.adapters.NoteItemDetailsLookup
import com.amjad.noteapp.ui.adapters.NotesListAdapter
import com.amjad.noteapp.ui.viewmodels.NoteViewModel

class NotesListFragment : Fragment() {
    private lateinit var viewModel: NoteViewModel

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
        val binding = NoteslistFragmentBinding.inflate(inflater, container, false)

        val adapter = NotesListAdapter()
        binding.noteslistview.adapter = adapter

        // tracker must be initialized after the adapter has been assigned to the recyclerview element
        adapter.tracker =
            SelectionTracker.Builder<Long>(
                NotesListAdapter.SELECTION_ID,
                binding.noteslistview,
                StableIdKeyProvider(binding.noteslistview),
                NoteItemDetailsLookup(binding.noteslistview),
                StorageStrategy.createLongStorage()
            ).build()

        observersInit(adapter)

        binding.setOnNewNoteClick {
            openNewNote()
        }

        return binding.root
    }

    private fun openNewNote() {
        // not sending any id means that this is new note
        val action = NotesListFragmentDirections.actionMainFragmentToNoteEditFragment()
        findNavController()
            .navigate(action)
    }

    private fun observersInit(adapter: NotesListAdapter) {
        viewModel.allNotes.observe(this, Observer { notes -> adapter.submitList(notes) })
    }

}
