package com.example.soft1c.adapter

import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.soft1c.R
import com.example.soft1c.databinding.ItemAcceptanceBinding
import com.example.soft1c.extension.inflateLayout
import com.example.soft1c.repository.model.Acceptance
import com.example.soft1c.repository.model.ItemClicked

class AcceptanceAdapter(
    private val onItemClicked: (itemClicked: ItemClicked, acceptance: Acceptance) -> Unit,
    private val showColumnZone: Boolean,
) :
    ListAdapter<Acceptance, AcceptanceAdapter.AcceptanceHolder>(AcceptanceDiffUtil()) {

    private var selectedTextView: TextView? = null
    private lateinit var binding: ItemAcceptanceBinding


    inner class AcceptanceHolder(
        private val onItemClicked: (itemClicked: ItemClicked, acceptance: Acceptance) -> Unit,
        private val showColumnZone: Boolean,
        private val binding: ItemAcceptanceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(acceptance: Acceptance) {
            itemView.setOnClickListener {
                onItemClicked(ItemClicked.ITEM, acceptance)
            }
            with(binding) {
                if (ACCEPTANCE_GUID == acceptance.ref)
                    binding.linearContainer.setBackgroundResource(R.color.selectedItem)
                txtDocumentNumber.text = acceptance.number.replace("[A-Z]".toRegex(), "").trimStart('0')
                txtClient.text = acceptance.client.trimStart('0')
                txtPackage.text = acceptance._package.filter { !it.isDigit() }
                txtZone.text = acceptance.zone
                txtZone.isVisible = showColumnZone

                txtEmptyWeight.isVisible = !acceptance.weight
                txtEmptyCapacity.isVisible = !acceptance.capacity

                ivWeight.isVisible = acceptance.weight
                ivCapacity.isVisible = acceptance.capacity
            }
        }


    }

    class AcceptanceDiffUtil : DiffUtil.ItemCallback<Acceptance>() {
        override fun areItemsTheSame(oldItem: Acceptance, newItem: Acceptance): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Acceptance, newItem: Acceptance): Boolean {
            return oldItem.ref == newItem.ref
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AcceptanceHolder {
        val view = parent.inflateLayout(R.layout.item_acceptance)
        binding = ItemAcceptanceBinding.bind(view)
        val holder = AcceptanceHolder(onItemClicked, showColumnZone, binding)

        // Назначение слушателей здесь
        binding.txtClient.setOnClickListener {
            changeBackgroundColor(it as TextView)
        }

        binding.txtPackage.setOnClickListener {
            changeBackgroundColor(it as TextView)
        }

        binding.txtZone.setOnClickListener {
            changeBackgroundColor(it as TextView)
        }

        binding.ivWeight.setOnClickListener {
            selectedTextView?.setBackgroundResource(0) // Сброс фона предыдущего выбранного TextView
            selectedTextView = it as TextView
            selectedTextView!!.setBackgroundResource(R.color.selectedWeight)
        }

        binding.txtEmptyWeight.setOnClickListener {
            changeBackgroundColor(it as TextView)
        }

        binding.txtEmptyCapacity.setOnClickListener {
            changeBackgroundColor(it as TextView)
        }

        binding.ivCapacity.setOnClickListener {
            selectedTextView?.setBackgroundResource(0) // Сброс фона предыдущего выбранного TextView
            selectedTextView = it as TextView
            selectedTextView!!.setBackgroundResource(R.color.selectedCapacity)
        }

        return holder
    }

    private fun changeBackgroundColor(textView: TextView) {
        when (selectedTextView?.id) {
            binding.ivWeight.id -> {
                selectedTextView!!.setBackgroundResource(R.color.weight)
            }
            binding.ivCapacity.id -> {
                selectedTextView!!.setBackgroundResource(R.color.capacity)
            }
            else -> {
                selectedTextView?.setBackgroundResource(0)
            }
        } // Reset background of previously selected TextView
        selectedTextView = textView
        selectedTextView?.setBackgroundResource(R.color.selected)
    }

    override fun onBindViewHolder(holder: AcceptanceHolder, position: Int) {
        holder.onBind(currentList[position])
        if (selectedTextView != null) {
            selectedTextView = null
        }
    }



    fun updateFilteredItems(): List<Acceptance> {
        if (selectedTextView != null) {
            val query = selectedTextView!!.text
            val filteredItems = currentList.filter { acceptance ->
                when (selectedTextView!!.id) {
                    binding.txtClient.id -> acceptance.client.trimStart('0') == query
                    binding.txtPackage.id -> acceptance._package.filter { !it.isDigit() } == query
                    binding.txtZone.id -> acceptance.zone == query
                    binding.txtEmptyWeight.id -> !acceptance.weight
                    binding.ivWeight.id -> acceptance.weight
                    binding.txtEmptyCapacity.id -> !acceptance.capacity
                    binding.ivCapacity.id -> acceptance.capacity
                    else -> false
                }
            }
            return filteredItems
        } else {
            return currentList
        }
    }

    override fun getItemCount(): Int = currentList.size

    companion object {
        var ACCEPTANCE_GUID = ""
        var IS_CLICKABLE = false
    }
}