package com.amjad.opennote.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amjad.opennote.data.entities.CheckableListNote
import com.amjad.opennote.databinding.CheckableNoteItemBinding
import com.amjad.opennote.databinding.CheckableTitleItemBinding
import com.amjad.opennote.ui.viewmodels.NoteEditViewModel

class CheckableNoteListAdapter(private val viewModel: NoteEditViewModel) :
    ListAdapter<CheckableListNote.Item, CheckableNoteListAdapter.BaseCheckableNoteItemViewHolder>(
        CheckableNoteListDiffItemCallBack()
    ) {

    override fun getItemCount(): Int {
        return super.getItemCount() + 1
    }

    override fun getItemViewType(position: Int): Int {
        // first one is the title
        return if (position == 0)
            VIEWTYPE_TITLE
        else
            VIEWTYPE_NOTE_ITEM
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseCheckableNoteItemViewHolder {
        return if (viewType == VIEWTYPE_TITLE)
            TitleNoteItemViewHolder(
                CheckableTitleItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        else
            CheckableListNoteItemViewHolder(
                CheckableNoteItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

    }

    override fun onBindViewHolder(holder: BaseCheckableNoteItemViewHolder, position: Int) {
        when (holder) {
            is CheckableListNoteItemViewHolder -> {
                holder.bind(getItem(position - 1))
            }
            is TitleNoteItemViewHolder -> {
                holder.bind(viewModel)
            }
        }
    }

    inner class CheckableListNoteItemViewHolder(private val binding: CheckableNoteItemBinding) :
        BaseCheckableNoteItemViewHolder(binding.root) {

        fun bind(item: CheckableListNote.Item) {
            binding.item = item
            binding.setOnDelete {
                (viewModel.note.value as CheckableListNote?)?.noteList?.removeAt(adapterPosition)
                (viewModel.note as MutableLiveData).run {
                    value = value
                }
            }
        }
    }

    inner class TitleNoteItemViewHolder(private val binding: CheckableTitleItemBinding) :
        BaseCheckableNoteItemViewHolder(binding.root) {
        fun bind(viewModel: NoteEditViewModel) {
            binding.model = viewModel
        }
    }

    abstract inner class BaseCheckableNoteItemViewHolder(view: View) : RecyclerView.ViewHolder(view)

    companion object {
        const val VIEWTYPE_TITLE = 0
        const val VIEWTYPE_NOTE_ITEM = 1
    }
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