package com.amjad.noteapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.amjad.noteapp.data.Note
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
            viewModel.insert(Note("another", null))
        }

        return binding.root
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
