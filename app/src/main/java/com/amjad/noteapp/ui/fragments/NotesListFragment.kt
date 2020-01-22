package com.amjad.noteapp.ui.fragments

import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.amjad.noteapp.R
import com.amjad.noteapp.databinding.NotesListFragmentBinding
import com.amjad.noteapp.ui.adapters.NoteListSelector
import com.amjad.noteapp.ui.adapters.NotesListAdapter
import com.amjad.noteapp.ui.dialogs.ColorChooseDialog
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

    private fun observersInit(adapter: NotesListAdapter) {
        viewModel.filteredAllNotes.observe(this, Observer { notes ->
            adapter.submitList(notes)
        })
    }

    private fun openNewNote() {
        // selecting the note from here in order to fix the bug of starting a new note
        // each time the EditNote fragment is rebuilt.
        viewModel.setNoteID(-1)
        val action = NotesListFragmentDirections.actionMainFragmentToNoteEditFragment()
        findNavController()
            .navigate(action)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.notes_list_menu, menu)

        val searchAction = menu.findItem(R.id.menu_search_action)
        val searchActionView = searchAction.actionView as SearchView

        searchActionView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean =
                true

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setNotesListFilter(newText ?: "")
                return true
            }
        })

        // change the search icon on the keyboard, as we are doing on-time filtering
        searchActionView.imeOptions = EditorInfo.IME_NULL
    }

    override fun onPause() {
        // don't carry the actionMode to the EditNote fragment
        actionMode?.finish()
        // reset the filter
        viewModel.setNotesListFilter("")
        super.onPause()
    }

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.menu_delete_action -> {
                    viewModel.deleteNotes(adapter.selector.selection.toList())
                    val selectionLen = adapter.selector.selection.size
                    Snackbar.make(
                        binding.root,
                        "Deleted $selectionLen note${if (selectionLen > 1) "s" else ""}",
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(R.string.undo) {
                            viewModel.undeleteNotes()
                        }.show()
                    mode.finish()
                    return true
                }
                R.id.menu_color_action -> {
                    fragmentManager?.also { fragmentManager ->
                        ColorChooseDialog()
                            .setOnColorClick { color ->
                                viewModel.updateNotesColor(
                                    adapter.selector.selection.toList(),
                                    color
                                )
                                // on color choose will also close the dialog, so we clear
                                // the selection.
                                adapter.selector.clearSelection()
                            }.show(fragmentManager, "ColorChooseDialog")
                    }
                }
                R.id.menu_selectall_action -> {
                    // used viewModel to get the Ids for all notes (only shown now!)
                    viewModel.filteredAllNotes.value?.apply {
                        adapter.selector.setItemsSelection(this.map { it.id }, true)
                    }
                }
            }
            return false
        }

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.notes_list_selection_menu, menu)
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
