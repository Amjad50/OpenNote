package com.amjad.noteapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.amjad.noteapp.databinding.FragmentNoteEditBinding
import com.amjad.noteapp.ui.viewmodels.NotesListViewModel

class NoteEditFragment : Fragment() {

    private lateinit var viewModel: NotesListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNoteEditBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(NotesListViewModel::class.java)
        // TODO: Use the ViewModel
//        Log.i("BB", viewModel.notes.toString())
    }

}
