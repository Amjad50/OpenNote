package com.amjad.noteapp.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.amjad.noteapp.R
import com.amjad.noteapp.databinding.NotesListFragmentBinding
import com.amjad.noteapp.ui.adapters.NoteListSelector
import com.amjad.noteapp.ui.adapters.NotesListAdapter
import com.amjad.noteapp.ui.viewmodels.NoteViewModel
import com.google.android.material.snackbar.Snackbar

class NotesListFragment : Fragment() {
    private lateinit var binding: NotesListFragmentBinding
    private lateinit var viewModel: NoteViewModel
    private lateinit var adapter: NotesListAdapter
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
        binding = NotesListFragmentBinding.inflate(inflater, container, false)

        adapter = NotesListAdapter()
        binding.noteslistview.adapter = adapter
        binding.noteslistview.emptyView = binding.emptyView

        setupSelectorObservers(adapter.selector)

        observersInit(adapter)

        binding.setOnNewNoteClick {
            openNewNote()
        }

        return binding.root
    }

    private fun setupSelectorObservers(selector: NoteListSelector<Long>) {
        selector.addChangeObserver {
            if (selector.hasSelection()) {
                if (actionMode == null) {
                    actionMode = activity?.startActionMode(actionModeCallback)
                }
                actionMode?.title = selector.selection.size.toString()
            } else {
                actionMode?.finish()
            }
        }
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
                    viewModel.deleteNotes(adapter.selector.selection.map { it }.toList())
                    Snackbar.make(
                        binding.root,
                        "deleted ${adapter.selector.selection.size} notes",
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(R.string.undo) {
                            viewModel.undeleteNotes()
                        }.show()
                    mode.finish()
                    return true
                }
                R.id.menu_selectall_action -> {
                    // used viewModel to get the Ids for all notes
                    viewModel.allNotes.value?.apply {
                        adapter.selector.setItemsSelection(this.map { it.id }, true)
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
            actionMode = null
            adapter.selector.clearSelection()
        }
    }

}
