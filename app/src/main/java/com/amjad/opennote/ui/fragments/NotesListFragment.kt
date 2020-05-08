package com.amjad.opennote.ui.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.amjad.opennote.MainActivity
import com.amjad.opennote.R
import com.amjad.opennote.data.entities.NoteType
import com.amjad.opennote.databinding.NotesListFragmentBinding
import com.amjad.opennote.ui.adapters.NoteListSelector
import com.amjad.opennote.ui.adapters.NotesListAdapter
import com.amjad.opennote.ui.dialogs.ColorChooseDialog
import com.amjad.opennote.ui.viewmodels.NoteListViewModel
import com.google.android.material.snackbar.Snackbar
import com.leinardi.android.speeddial.SpeedDialView

class NotesListFragment : BaseBaseNoteFragment() {
    private val args: NotesListFragmentArgs by navArgs()

    private lateinit var binding: NotesListFragmentBinding
    override lateinit var viewModel: NoteListViewModel
    private lateinit var adapter: NotesListAdapter
    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[NoteListViewModel::class.java]

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = NotesListFragmentBinding.inflate(inflater, container, false)

        adapter = NotesListAdapter(
            viewModel.selector,
            // CYAN is the closest to the default accent color
            context?.getColor(R.color.colorAccent) ?: Color.CYAN
        )

        // TODO: edit this as there should always be root (id=0) folder note.
        if (args.noteId == NEW_NOTE_ID) {
            if (!viewModel.isNoteSelected) {
                viewModel.insertNewNote(NoteType.FOLDER_NOTE, args.parentId)
            }
        } else
            viewModel.setNoteID(args.noteId)

        binding.lifecycleOwner = this
        binding.model = viewModel

        binding.noteslistview.adapter = adapter
        binding.noteslistview.emptyView = binding.emptyView

        setupSelectorObservers(viewModel.selector)

        observersInit(adapter)

        initSpeedDial(binding.speedDial)

        // show the back button if the current folder is not the root
        // this is used because for some reason when navigating to an instance of self
        // ie. from NotesListFragment to another NotesListFragment it does
        // not show the back button, so I'm showing it here by force
        (activity as MainActivity?)?.run {
            supportActionBar?.setDisplayHomeAsUpEnabled(args.noteId != 0L)
        }
        return binding.root
    }

    override fun setupActionBar() {
        // if not the root
        if (args.noteId != 0L)
            super.setupActionBar()
    }

    private fun setupSelectorObservers(selector: NoteListSelector<Long>) {
        val changeHandler: () -> Unit = {
            if (selector.hasSelection()) {
                if (actionMode == null) {
                    actionMode = activity?.startActionMode(actionModeCallback)
                }
                actionMode?.title = selector.selection.size.toString()
            } else {
                actionMode?.finish()
            }
        }

        selector.addChangeObserver(changeHandler)

        // call it to initiate the actionMode if needed (configuration changes happened)
        changeHandler()
    }

    private fun observersInit(adapter: NotesListAdapter) {
        viewModel.filteredAllNotes.observe(viewLifecycleOwner, Observer { notes ->
            adapter.submitList(notes)

            // update filter here as it will change together with the filtered list
            // so, there is no need to listen to changes in the filter string
            adapter.updateFilter(viewModel.getNotesListFilter())
        })
    }

    private fun initSpeedDial(speedDial: SpeedDialView) {

        speedDial.inflate(R.menu.speed_dial_menu)

        // childrens of speeddial actions
        speedDial.setOnActionSelectedListener {
            when (it.id) {
                R.id.speed_dial_add_list_note -> openNewListNote()
                R.id.speed_dial_add_folder_note -> openNewFolderNote()
            }

            false
        }

        // main button actions
        speedDial.setOnChangeListener(object : SpeedDialView.OnChangeListener {
            override fun onMainActionSelected(): Boolean {
                openNewTextNote()
                return false
            }

            override fun onToggleChanged(isOpen: Boolean) {}
        })

    }

    private fun openNewTextNote() {
        // before going to the new note, we clear the actionMode
        actionMode?.finish()

        val action = NotesListFragmentDirections.actionMainFragmentToNoteEditFragment(
            parentId = viewModel.getNoteId()
        )
        findNavController()
            .navigate(action)
    }

    private fun openNewListNote() {
        actionMode?.finish()

        val action =
            NotesListFragmentDirections.actionNoteListFragmentToCheckableListNoteEditFragment(
                parentId = viewModel.getNoteId()
            )
        findNavController()
            .navigate(action)
    }

    private fun openNewFolderNote() {
        actionMode?.finish()

        val action =
            NotesListFragmentDirections.actionNoteListFragmentToSelf(
                parentId = viewModel.getNoteId(),
                noteId = -1L    // to make a new note (because the default is 0)
            )
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

        // increase the width of the actionView to cover as much as possible
        searchActionView.maxWidth = Int.MAX_VALUE

        // when calling expandActionView, the onQueryTextChange is called with an empty String
        // which would reset the filter but also expand it (in empty state)
        // that's why, I saved the current filter outside first then use it to submit a new
        // query string

        val currentFilter = viewModel.getNotesListFilter()

        if (currentFilter.isNotEmpty()) {
            searchAction.expandActionView()
            searchActionView.setQuery(currentFilter, true)
            searchActionView.clearFocus()
        }

        // change the search icon on the keyboard, as we are doing on-time filtering
        searchActionView.imeOptions = EditorInfo.IME_NULL
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_backup_database -> {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "*/*"
                intent.putExtra(Intent.EXTRA_TITLE, "opennote.onbak")

                startActivityForResult(
                    intent,
                    REQUEST_BACKUP_DB_ACTION
                )
                true
            }
            R.id.menu_restore_database -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "*/*"
                intent.putExtra(Intent.EXTRA_TITLE, "opennote.onbak")

                startActivityForResult(
                    intent,
                    REQUEST_RESTORE_DB_ACTION
                )
                true
            }
            R.id.menu_delete_database -> {
                context?.also {
                    viewModel.deleteAll(it)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (
            resultCode == Activity.RESULT_OK
            && data != null
        ) {
            when (requestCode) {
                REQUEST_BACKUP_DB_ACTION -> {
                    context?.also { context ->
                        data.data?.also { uri ->
                            val outstream = context.contentResolver.openOutputStream(uri)

                            if (outstream != null) {
                                viewModel.backupDatabase(context, outstream) {
                                    Toast.makeText(context, "BACKUP DONE", Toast.LENGTH_LONG).show()
                                    outstream.close()
                                }
                            }
                        }
                    }
                }
                REQUEST_RESTORE_DB_ACTION -> {
                    context?.also { context ->
                        data.data?.also { uri ->
                            val instream = context.contentResolver.openInputStream(uri)

                            if (instream != null) {
                                viewModel.restoreDatabase(context, instream, {
                                    Toast.makeText(context, "RESTORE DONE", Toast.LENGTH_LONG)
                                        .show()
                                    instream.close()
                                }, { throwable ->
                                    // FIXME handle `throwable` properly for each type of exception
                                    Toast.makeText(context, "ERROR IN RESTORING", Toast.LENGTH_LONG)
                                        .show()

                                    instream.close()
                                })
                            }
                        }
                    }
                }
            }

        }
    }

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.menu_delete_action -> {
                    viewModel.deleteNotes(viewModel.selector.selection.toList())
                    val selectionLen = viewModel.selector.selection.size
                    Snackbar.make(
                        binding.root,
                        "Deleted $selectionLen note${if (selectionLen > 1) "s" else ""}",
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(R.string.undo) {
                            viewModel.unDeleteNotes()
                        }.show()
                    mode.finish()
                    return true
                }
                R.id.menu_color_action -> {
                    parentFragmentManager.also { fragmentManager ->
                        ColorChooseDialog()
                            .setOnColorClick { color ->
                                viewModel.updateNotesColor(
                                    viewModel.selector.selection.toList(),
                                    color
                                )
                                // on color choose will also close the dialog, so we clear
                                // the selection.
                                viewModel.selector.clearSelection()
                            }.show(fragmentManager, "ColorChooseDialog")
                    }
                }
                R.id.menu_selectall_action -> {
                    // used viewModel to get the Ids for all notes (only shown now!)
                    viewModel.filteredAllNotes.value?.apply {
                        viewModel.selector.setItemsSelection(this.map { it.id }, true)
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
            viewModel.selector.clearSelection()
        }
    }

    companion object {
        const val REQUEST_BACKUP_DB_ACTION = 2
        const val REQUEST_RESTORE_DB_ACTION = 3
    }

}
