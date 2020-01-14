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
import com.amjad.noteapp.databinding.NotesListFragmentBinding
import com.amjad.noteapp.ui.adapters.NoteItemDetailsLookup
import com.amjad.noteapp.ui.adapters.NoteItemKeyProvider
import com.amjad.noteapp.ui.adapters.NotesListAdapter
import com.amjad.noteapp.ui.viewmodels.NoteViewModel

class NotesListFragment : Fragment() {
    private lateinit var viewModel: NoteViewModel
    private lateinit var tracker: SelectionTracker<Long>
    private var actionMode: ActionMode? = null

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
        val binding = NotesListFragmentBinding.inflate(inflater, container, false)

        val adapter = NotesListAdapter()
        binding.noteslistview.adapter = adapter

        // tracker must be initialized after the adapter has been assigned to the recyclerview element
        setupTracker(binding)

        adapter.tracker = tracker

        observersInit(adapter)

        binding.setOnNewNoteClick {
            openNewNote()
        }

        return binding.root
    }

    private fun setupTracker(binding: NotesListFragmentBinding) {
        tracker =
            SelectionTracker.Builder<Long>(
                NotesListAdapter.SELECTION_ID,
                binding.noteslistview,
                NoteItemKeyProvider(binding.noteslistview),
                NoteItemDetailsLookup(binding.noteslistview),
                StorageStrategy.createLongStorage()
            ).build()

        tracker.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onSelectionChanged() {
                if (tracker.hasSelection()) {
                    when (actionMode) {
                        null -> actionMode = activity?.startActionMode(actionModeCallback)
                        else -> actionMode?.title = tracker.selection.size().toString()
                    }
                }
            }
        })
    }

    override fun onPause() {
        // don't carry the actionMode to the EditNote fragment
        actionMode?.finish()
        super.onPause()
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

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.menu_delete_action -> {
                    viewModel.deleteNotes(tracker.selection.map { it.toInt() }.toList())
                    mode.finish()
                    return true
                }
                R.id.menu_selectall_action -> {
                    // used viewModel to get the Ids for all notes
                    viewModel.allNotes.value?.apply {
                        tracker.setItemsSelected(this.map { it.id.toLong() }, true)
                    }
                }
            }
            return false
        }

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.note_list_fragment_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            tracker.clearSelection()
            actionMode = null
        }
    }

}
