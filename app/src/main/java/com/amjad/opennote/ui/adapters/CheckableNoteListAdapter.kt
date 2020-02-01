package com.amjad.opennote.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.amjad.opennote.data.entities.CheckableListNote
import com.amjad.opennote.databinding.CheckableAddNewItemViewBinding
import com.amjad.opennote.databinding.CheckableNoteItemBinding
import com.amjad.opennote.databinding.CheckableTitleItemBinding
import com.amjad.opennote.ui.viewmodels.NoteEditViewModel

class CheckableNoteListAdapter(private val viewModel: NoteEditViewModel) :
    OffsettedListAdapter<CheckableListNote.Item, CheckableNoteListAdapter.BaseCheckableNoteItemViewHolder>(
        CheckableNoteListDiffItemCallBack(), 1, 1
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
            is AddNewItemViewHolder -> {
                holder.bind()
            }
        }
    }

    private inner class CheckableListNoteItemViewHolder(private val binding: CheckableNoteItemBinding) :
        BaseCheckableNoteItemViewHolder(binding.root) {

        fun bind(item: CheckableListNote.Item) {
            binding.item = item

            binding.setOnCheckboxChange {
                (viewModel.note as MutableLiveData).run {
                    value = value
                }
            }

            binding.setOnDelete {
                (viewModel.note.value as CheckableListNote?)?.noteList?.removeAt(item.position)
                (viewModel.note as MutableLiveData).run {
                    value = value
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

    private inner class AddNewItemViewHolder(val binding: CheckableAddNewItemViewBinding) :
        CheckableNoteListAdapter.BaseCheckableNoteItemViewHolder(binding.root) {

        fun bind() {
            binding.setOnAddNewNote {
                // TODO: make it auto focus to type immediately
                (viewModel.note.value as CheckableListNote?)?.noteList?.add(CheckableListNote.Item())
                (viewModel.note as MutableLiveData).run {
                    value = value
                }
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