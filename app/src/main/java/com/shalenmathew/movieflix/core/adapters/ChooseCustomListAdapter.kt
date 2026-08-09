package com.shalenmathew.movieflix.core.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.domain.model.UserCustomList

class ChooseCustomListAdapter(
    private val onListClick: (UserCustomList) -> Unit,
    private val checkList: List<Int>
) : ListAdapter<UserCustomList, ChooseCustomListAdapter.ViewHolder>(ListDiffCallback()) {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.choose_list_name)
        val check: ImageView = itemView.findViewById(R.id.choose_list_check)

        fun bind(list: UserCustomList) {
            name.text = list.name
            check.visibility = if (checkList.contains(list.id)) View.VISIBLE else View.GONE
            itemView.setOnClickListener { onListClick(list) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_choose_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ListDiffCallback : DiffUtil.ItemCallback<UserCustomList>() {
        override fun areItemsTheSame(oldItem: UserCustomList, newItem: UserCustomList): Boolean =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: UserCustomList, newItem: UserCustomList): Boolean =
            oldItem == newItem
    }
}
