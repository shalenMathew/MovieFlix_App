package com.shalenmathew.movieflix.core.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.databinding.ItemFaqBinding
import com.shalenmathew.movieflix.domain.model.FAQItem
import com.shalenmathew.movieflix.domain.model.FAQRow

class FAQAdapter(private val rows: List<FAQRow>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (rows[position]) {
            is FAQRow.Header -> VIEW_TYPE_HEADER
            is FAQRow.Item -> VIEW_TYPE_ITEM
        }
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_category_title)

        fun bind(header: FAQRow.Header) {
            tvTitle.text = header.title
        }
    }

    inner class FAQViewHolder(private val binding: ItemFaqBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FAQItem) {
            binding.faq = item

            // Update visibility based on state
            binding.tvAnswer.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
            binding.ivArrow.rotation = if (item.isExpanded) 180f else 0f

            binding.clMain.setOnClickListener {
                val expanded = item.isExpanded
                item.isExpanded = !expanded

                // Animate the parent RecyclerView to ensure smooth list updates
                val parent = binding.root.parent as? ViewGroup
                if (parent != null) {
                    TransitionManager.beginDelayedTransition(parent, AutoTransition().apply {
                        duration = 300
                    })
                }

                binding.tvAnswer.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
                binding.ivArrow.rotation = if (item.isExpanded) 180f else 0f
            }

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_faq_header,
                parent,
                false
            )
            HeaderViewHolder(view)
        } else {
            val binding = ItemFaqBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            FAQViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = rows[position]) {
            is FAQRow.Header -> (holder as HeaderViewHolder).bind(row)
            is FAQRow.Item -> (holder as FAQViewHolder).bind(row.item)
        }
    }

    override fun getItemCount(): Int = rows.size
}
