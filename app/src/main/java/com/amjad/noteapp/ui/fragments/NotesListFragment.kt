package com.amjad.noteapp.ui.fragments

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amjad.noteapp.databinding.NoteslistFragmentBinding
import com.amjad.noteapp.ui.adapters.NotesListAdapter
import com.amjad.noteapp.ui.viewmodels.NotesListViewModel

class NotesListFragment : Fragment() {
    private lateinit var viewModel: NotesListViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val binding = NoteslistFragmentBinding.inflate(inflater, container, false)
        val adapter =  NotesListAdapter()
        binding.noteslistview.adapter = adapter

        observersInit(adapter)

        return binding.root
    }

    private fun observersInit(adapter: NotesListAdapter) {
        adapter.submitList(viewModel.notes)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(NotesListViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
