package com.shalenmathew.movieflix.core.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.shalenmathew.movieflix.databinding.ItemFaqBinding
import com.shalenmathew.movieflix.domain.model.FAQItem

class FAQAdapter(private val items: List<FAQItem>) :
    RecyclerView.Adapter<FAQAdapter.FAQViewHolder>() {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FAQViewHolder {
        val binding = ItemFaqBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FAQViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FAQViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
