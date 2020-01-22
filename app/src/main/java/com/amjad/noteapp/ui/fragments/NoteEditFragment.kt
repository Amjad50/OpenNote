package com.amjad.noteapp.ui.fragments

import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.amjad.noteapp.MainActivity
import com.amjad.noteapp.R
import com.amjad.noteapp.databinding.NoteEditFragmentBinding
import com.amjad.noteapp.ui.dialogs.ColorChooseDialog
import com.amjad.noteapp.ui.viewmodels.NoteViewModel


class NoteEditFragment : Fragment() {

    private val args: NoteEditFragmentArgs by navArgs()

    private lateinit var viewModel: NoteViewModel
    private lateinit var binding: NoteEditFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProviders.of(this)[NoteViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = NoteEditFragmentBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = this

        binding.model = viewModel

        // start editing right away if this is a new note
        if (args.noteId == NEW_NOTE_ID)
        // FIXME: when closing and reopening the app (or rotating) when editing, selectedNoteId is
        //  changed from -1 but this statement is executed, which would focus on the beginning of
        //  the line, which is not good.
            requestFocusAndShowKeyboard(binding.noteEdit)
        else
        // only set the id on notes already exists,
        // new notes are handled by NoteListFragment when creating a new note
            viewModel.setNoteID(args.noteId)

        // remove the title from the actionBar
        (activity as MainActivity?)?.run {
            supportActionBar?.title = null
        }

        return binding.root
    }

    private fun requestFocusAndShowKeyboard(noteEdit: View) {
        if (noteEdit.requestFocusFromTouch())
            context?.also {
                val inputMethodManager = getSystemService(it, InputMethodManager::class.java)
                inputMethodManager?.toggleSoftInput(SHOW_IMPLICIT, 0)
            }
    }

    private fun saveNote() {
        // TODO: dont change the date if value of note did not change: add diff comparator
        //  handle difference changes and date assignment

        // no id sent mean that this is a new note
        if (viewModel.getSelectedNoteID() == NEW_NOTE_ID) {
            viewModel.insertCurrentNote()
        } else {
            viewModel.updateCurrentNote()
        }
    }

    override fun onStop() {
        super.onStop()
        saveNote()

        // hide the keyboard
        context?.also {
            val inputMethodManager = getSystemService(it, InputMethodManager::class.java)
            inputMethodManager?.hideSoftInputFromWindow(binding.root.windowToken, 0)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.note_edit_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit_menu_color_action -> {
                fragmentManager?.also { fragmentManager ->
                    ColorChooseDialog().setOnColorClick { color ->
                        viewModel.currentNote.value?.color = color

                        // so we can update the value to the observers
                        (viewModel.currentNote as MutableLiveData).run {
                            value = value
                        }
                    }.show(fragmentManager, "ColorChooseDialogInEdit")
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val NEW_NOTE_ID = -1L
    }
}
