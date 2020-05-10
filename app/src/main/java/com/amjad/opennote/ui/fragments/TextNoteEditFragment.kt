package com.amjad.opennote.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.amjad.opennote.data.entities.NoteType
import com.amjad.opennote.databinding.TextNoteEditFragmentBinding
import com.amjad.opennote.ui.dialogs.ConfirmImageDeletionDialog
import com.amjad.opennote.utils.requestFocusAndShowKeyboard


class TextNoteEditFragment : BaseNoteEditFragment() {

    private val args: TextNoteEditFragmentArgs by navArgs()

    private lateinit var binding: TextNoteEditFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TextNoteEditFragmentBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = this

        binding.model = viewModel

        // start editing right away if this is a new note and insert the note into the database
        if (args.noteId == NEW_NOTE_ID) {
            if (!viewModel.isNoteSelected) {
                requestFocusAndShowKeyboard(binding.noteEdit, context)

                viewModel.insertNewNote(NoteType.TEXT_NOTE, args.parentId)
            }
        } else
        // this might be redundant if the id was already set
            viewModel.setNoteID(args.noteId)

        setupScrollViewShadowsEffect()

        binding.outsideNoteClickView.setOnClickListener {
            requestFocusAndShowKeyboard(binding.noteEdit, context)
            binding.noteEdit.run {
                // move selection to the end (cursor)
                setSelection(text?.length ?: 0)
            }
        }

        binding.image.setOnLongClickListener {
            context?.also { context ->
                parentFragmentManager.also { fragmentManager ->
                    ConfirmImageDeletionDialog()
                        .setOnConfirm {
                            viewModel.deleteLastImage(context)
                            viewModel.notifyNoteUpdated()
                        }.show(fragmentManager, "ImageDeleteDialogInEdit")
                }
            }
            true
        }

        return binding.root
    }

    private fun setupScrollViewShadowsEffect() {
        // FIXME: bottom shadow not activated from the start
        binding.scrollView.setOnScrollChangeListener { v, _, _, _, _ ->
            binding.topShadowVisible = v.canScrollVertically(-1)
            binding.bottomShadowVisible = v.canScrollVertically(1)
        }
    }
}
