package com.amjad.opennote.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.amjad.opennote.R
import com.amjad.opennote.data.entities.CheckableListNote
import com.amjad.opennote.data.entities.NoteType
import com.amjad.opennote.databinding.CheckableListNoteEditFragmentBinding
import com.amjad.opennote.ui.adapters.CheckableNoteListAdapter

class CheckableListNoteEditFragment : BaseNoteEditFragment() {

    private val args: CheckableListNoteEditFragmentArgs by navArgs()

    private lateinit var binding: CheckableListNoteEditFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CheckableListNoteEditFragmentBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = this

        binding.model = viewModel

        // start editing right away if this is a new note and insert the note into the database
        if (args.noteId == NEW_NOTE_ID) {
            if (!viewModel.isNoteSelected) {
                viewModel.insertNewNote(NoteType.CHECKABLE_LIST_NOTE)
            }
        } else
        // this might be redundant if the id was already set
            viewModel.setNoteID(args.noteId)

        val adapter = CheckableNoteListAdapter(viewModel)
        binding.notesList.adapter = adapter

        observersInit(adapter)

        return binding.root
    }

    private fun observersInit(adapter: CheckableNoteListAdapter) {
        viewModel.note.observe(viewLifecycleOwner, Observer {
            if (it.type != NoteType.CHECKABLE_LIST_NOTE) {

                // This is a very peaceful way of showing error, and I will not get any report
                // regarding it
                // TODO: should be changed to more aggressive crash?
                Toast.makeText(
                    context,
                    getString(R.string.error_not_list_type_note),
                    Toast.LENGTH_LONG
                ).show()
                // exit
                findNavController().navigateUp()
            }
            val note = it as CheckableListNote

            // copy the whole list every time, this is ok as it copies the references
            // also when using references, when updating the inside data of any of the Items
            // it will also be updated here as its a refernce to the same data
            // which helps a lot when saving the data to the database
            adapter.submitList(note.noteList.toList())

            // FIXME: when removed notifyDataSetChanged(), when starting the fragment
            //  it jumps to the end of the list, not sure if the problem is here or not

            Log.i("BB", note.noteList.size.toString())
        })
    }
}
