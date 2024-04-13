package com.example.soft1c.reloading.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.soft1c.R
import com.example.soft1c.databinding.ItemLoadingBinding
import com.example.soft1c.extension.inflateLayout
import com.example.soft1c.repository.model.ItemClicked
import com.example.soft1c.repository.model.Loading
import com.example.soft1c.utils.Utils.inputDateFormat
import com.example.soft1c.utils.Utils.outputDateFormat

class ReloadingAdapter(
    private val onItemClicked: (itemClicked: ItemClicked, loading: Loading) -> Unit,
) : ListAdapter<Loading, ReloadingAdapter.LoadingHolder>(LoadingDiffUtil()) {

    class LoadingHolder(
        private val onItemClicked: (itemClicked: ItemClicked, loading: Loading) -> Unit,
        view: View,
    ) : RecyclerView.ViewHolder(view) {

        private val binding = ItemLoadingBinding.bind(view)

        fun onBind(loading: Loading){
            itemView.setOnClickListener {
                onItemClicked(ItemClicked.ITEM, loading)
            }
            with(binding){
                if(LOADING_GUID == loading.guid)
                    binding.linearLoadingContainer.setBackgroundResource(R.color.selectedItem)
                txtDocumentNumber.text=loading.number.replace("[A-Z]".toRegex(), "").trimStart('0')
                txtDate.text= inputDateFormat.parse(loading.date)
                    ?.let { outputDateFormat.format(it) }
                txtNumberAuto.text = loading.car.number
                txtRecipient.text = loading.senderWarehouse.prefix
            }
        }
    }

    class LoadingDiffUtil : DiffUtil.ItemCallback<Loading>(){
        override fun areItemsTheSame(oldItem: Loading, newItem: Loading): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Loading, newItem: Loading): Boolean {
            return oldItem.guid == newItem.guid
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoadingHolder{
        return LoadingHolder(onItemClicked, parent.inflateLayout(R.layout.item_loading))
    }

    override fun onBindViewHolder(holder: LoadingHolder, position: Int){
        holder.onBind(currentList[position])
    }

    override fun getItemCount(): Int = currentList.size

    companion object {
        var LOADING_GUID = ""
    }
}