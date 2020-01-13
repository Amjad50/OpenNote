package com.amjad.noteapp.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import com.amjad.noteapp.R
import com.amjad.noteapp.databinding.NoteslistFragmentBinding
import com.amjad.noteapp.ui.adapters.NoteItemDetailsLookup
import com.amjad.noteapp.ui.adapters.NoteItemKeyProvider
import com.amjad.noteapp.ui.adapters.NotesListAdapter
import com.amjad.noteapp.ui.viewmodels.NoteViewModel

class NotesListFragment : Fragment() {
    private lateinit var viewModel: NoteViewModel
    private lateinit var tracker: SelectionTracker<Long>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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
        tracker =
            SelectionTracker.Builder<Long>(
                NotesListAdapter.SELECTION_ID,
                binding.noteslistview,
                NoteItemKeyProvider(binding.noteslistview),
                NoteItemDetailsLookup(binding.noteslistview),
                StorageStrategy.createLongStorage()
            ).build()

        adapter.tracker = tracker

        observersInit(adapter)

        binding.setOnNewNoteClick {
            openNewNote()
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.note_list_fragment_menu, menu)

        // if any is selected, show the delete button
        tracker.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onSelectionChanged() {
                if (!tracker.hasSelection()) {
                    if (menu.findItem(R.id.menu_delete_action).isVisible)
                        menu.findItem(R.id.menu_delete_action).isVisible = false
                } else {
                    if (!menu.findItem(R.id.menu_delete_action).isVisible)
                        menu.findItem(R.id.menu_delete_action).isVisible = true
                }
            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_delete_action) {
            viewModel.deleteNotes(tracker.selection.map { it.toInt() }.toList())
            tracker.clearSelection()
        }

        return super.onOptionsItemSelected(item)
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
