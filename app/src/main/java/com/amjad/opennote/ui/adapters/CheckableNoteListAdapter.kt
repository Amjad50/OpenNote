package com.amjad.opennote.ui.adapters

import android.graphics.Paint
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.amjad.opennote.R
import com.amjad.opennote.data.entities.CheckableListNote
import com.amjad.opennote.databinding.CheckableAddNewItemViewBinding
import com.amjad.opennote.databinding.CheckableImageItemBinding
import com.amjad.opennote.databinding.CheckableNoteItemBinding
import com.amjad.opennote.databinding.CheckableTitleItemBinding
import com.amjad.opennote.ui.viewmodels.NoteEditViewModel
import com.amjad.opennote.utils.requestFocusAndShowKeyboard
import com.google.android.material.snackbar.Snackbar

class CheckableNoteListAdapter(private val viewModel: NoteEditViewModel) :
    OffsettedListAdapter<CheckableListNote.Item, CheckableNoteListAdapter.BaseCheckableNoteItemViewHolder>(
        CheckableNoteListDiffItemCallBack(), 1, 1, 1
    ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseCheckableNoteItemViewHolder {
        return when (viewType) {
            VIEWTYPE_HEADER -> TitleNoteItemViewHolder(
                CheckableTitleItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            VIEWTYPE_HEADER_IMAGE -> ImageNoteViewHolder(
                CheckableImageItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            VIEWTYPE_LIST_ITEM ->
                CheckableListNoteItemViewHolder(
                    CheckableNoteItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            VIEWTYPE_FOOTER -> AddNewItemViewHolder(
                CheckableAddNewItemViewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> {
                throw IllegalArgumentException("Unknown type with code $viewType")
            }
        }

    }

    override fun onBindViewHolder(holder: BaseCheckableNoteItemViewHolder, position: Int) {
        when (holder) {
            is CheckableListNoteItemViewHolder -> {
                holder.bind(getItem(position))
            }
            is TitleNoteItemViewHolder -> {
                holder.bind(viewModel)
            }
            is ImageNoteViewHolder -> {
                holder.bind(viewModel)
            }
            is AddNewItemViewHolder -> {
                holder.bind()
            }
        }
    }

    fun refreshImage() {
        if (headerImageOffset > 0)
            notifyItemRangeChanged(0, headerImageOffset)
    }

    private inner class CheckableListNoteItemViewHolder(private val binding: CheckableNoteItemBinding) :
        BaseCheckableNoteItemViewHolder(binding.root) {

        private fun updateTextEditStrikethough(item: CheckableListNote.Item) {
            binding.textEdit.paintFlags =
                if (item.isChecked)
                    binding.textEdit.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                else
                    binding.textEdit.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        fun bind(item: CheckableListNote.Item) {
            binding.item = item

            binding.setOnCheckboxChange {
                viewModel.notifyNoteUpdated()
                // when changing the state of the note
                updateTextEditStrikethough(item)
            }
            // when creating
            updateTextEditStrikethough(item)

            if (viewModel.selectNextListItem) {
                // FIXME: when adding item while editing another, it will add it but hide the
                //  keyboard, as it is set to "toggle"
                requestFocusAndShowKeyboard(binding.textEdit, binding.root.context)
                viewModel.selectNextListItem = false
            }

            binding.setOnDelete {
                val notelist = (viewModel.note.value as CheckableListNote?)?.noteList
                val saved_position = item.position
                val saved_list_item = notelist?.removeAt(saved_position)

                Snackbar.make(
                    binding.root,
                    "Deleted list item",
                    Snackbar.LENGTH_LONG
                )
                    .setAction(R.string.undo) {
                        if (saved_list_item != null) {
                            notelist.add(saved_position, saved_list_item)
                            viewModel.notifyNoteUpdated()
                        }
                    }.show()
                viewModel.notifyNoteUpdated()
            }

            binding.textEdit.apply {
                // for some reason, this value will not be applied if it was set from xml
                setHorizontallyScrolling(false)
                maxLines = Int.MAX_VALUE

                // FIXME: change the icon to ENTER icon
                imeOptions = EditorInfo.IME_ACTION_DONE

                setOnEditorActionListener { _, actionId, event ->
                    if ((event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_NEXT
                    ) {
                        (viewModel.note.value as CheckableListNote?)?.noteList?.add(
                            item.position + 1,
                            CheckableListNote.Item()
                        )
                        viewModel.selectNextListItem = true
                        viewModel.notifyNoteUpdated()
                    }
                    true
                }
            }
        }
    }

    private class TitleNoteItemViewHolder(private val binding: CheckableTitleItemBinding) :
        BaseCheckableNoteItemViewHolder(binding.root) {
        fun bind(viewModel: NoteEditViewModel) {
            binding.model = viewModel
        }
    }

    private class ImageNoteViewHolder(private val binding: CheckableImageItemBinding) :
        BaseCheckableNoteItemViewHolder(binding.root) {
        fun bind(viewModel: NoteEditViewModel) {
            binding.model = viewModel
        }
    }

    private inner class AddNewItemViewHolder(val binding: CheckableAddNewItemViewBinding) :
        CheckableNoteListAdapter.BaseCheckableNoteItemViewHolder(binding.root) {

        fun bind() {
            binding.setOnAddNewNote {
                (viewModel.note.value as CheckableListNote?)?.noteList?.add(CheckableListNote.Item())
                viewModel.selectNextListItem = true
                viewModel.notifyNoteUpdated()
            }
        }

    }

    abstract class BaseCheckableNoteItemViewHolder(view: View) : RecyclerView.ViewHolder(view)
}


private class CheckableNoteListDiffItemCallBack : DiffUtil.ItemCallback<CheckableListNote.Item>() {
    override fun areContentsTheSame(
        oldItem: CheckableListNote.Item,
        newItem: CheckableListNote.Item
    ): Boolean =
        newItem.text == oldItem.text && newItem.isChecked == oldItem.isChecked


    override fun areItemsTheSame(
        oldItem: CheckableListNote.Item,
        newItem: CheckableListNote.Item
    ): Boolean =
        newItem === oldItem
}