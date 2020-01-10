package com.amjad.noteapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.amjad.noteapp.R
import com.amjad.noteapp.databinding.FragmentNoteEditBinding
import com.amjad.noteapp.ui.viewmodels.NotesEditViewModel

class NoteEditFragment : Fragment() {

    companion object {
        const val NOTEID_ARGUMENT = "com.android.noteapp.NoteEditFragment.note_id"
    }

    private lateinit var viewModel: NotesEditViewModel
    private var noteId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        noteId = arguments?.getInt(NOTEID_ARGUMENT) ?: -1
        val binding = FragmentNoteEditBinding.inflate(inflater, container, false)

        observersInit(binding)

        viewModel.setNoteID(noteId)

        return binding.root
    }

    private fun observersInit(binding: FragmentNoteEditBinding?) {
        viewModel.note.observe(this, Observer {
            binding?.title?.text = it?.title ?: getString(R.string.no_title)
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(NotesEditViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
