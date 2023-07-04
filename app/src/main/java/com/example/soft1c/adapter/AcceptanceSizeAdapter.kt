package com.example.soft1c.adapter

import android.annotation.SuppressLint
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.soft1c.R
import com.example.soft1c.databinding.ItemAcceptanceSizeBinding
import com.example.soft1c.extension.inflateLayout
import com.example.soft1c.repository.model.ItemClicked
import com.example.soft1c.repository.model.SizeAcceptance

class AcceptanceSizeAdapter(private val onItemClicked: (acceptanceSize: SizeAcceptance.SizeData, itemClicked: ItemClicked) -> Unit) :
    ListAdapter<SizeAcceptance.SizeData, AcceptanceSizeAdapter.AcceptanceSizeHolder>(
        AcceptanceSizeDiffUtil()
    ) {
    private var selectedItem: LinearLayout? = null
    var focusNumber: Int = -1

    inner class AcceptanceSizeHolder(
        private val onItemClicked: (acceptanceSize: SizeAcceptance.SizeData, itemClicked: ItemClicked) -> Unit,
        view: View,
    ) : RecyclerView.ViewHolder(view) {

        private val itemBinding = ItemAcceptanceSizeBinding.bind(view)
        fun onBind(acceptance: SizeAcceptance.SizeData) {
            itemView.setOnClickListener {
                onItemClicked(acceptance, ItemClicked.SIZE_ITEM)
                selectedItem?.setBackgroundResource(R.color.white)
                selectedItem = itemBinding.linearSizeContainer
                selectedItem?.setBackgroundResource(R.color.selectedItem)
            }
            with(itemBinding) {
                txtSeatNumber.text = acceptance.seatNumber.toString()
                txtLength.text = acceptance.length.toString()
                txtWidth.text = acceptance.width.toString()
                txtHeight.text = acceptance.height.toString()
                txtWeight.text = String.format("%.6f", acceptance.weight)

                if(focusNumber == acceptance.seatNumber){
                    selectedItem?.setBackgroundResource(R.color.white)
                    selectedItem = linearSizeContainer
                    selectedItem?.setBackgroundResource(R.color.selectedItem)
                    focusNumber = -1
                }
            }

            if (adapterPosition == 0) {
                val paddingInDp = 1.5
                val paddingInPx = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    paddingInDp.toFloat(),
                    itemView.context.resources.displayMetrics
                ).toInt()
                itemView.setPadding(
                    itemView.paddingLeft,
                    paddingInPx,
                    itemView.paddingRight,
                    itemView.paddingBottom
                )
            } else {
                // Reset padding for other items
                itemView.setPadding(
                    itemView.paddingLeft,
                    0,
                    itemView.paddingRight,
                    itemView.paddingBottom
                )
            }
        }
    }

    class AcceptanceSizeDiffUtil : DiffUtil.ItemCallback<SizeAcceptance.SizeData>() {
        override fun areItemsTheSame(
            oldItem: SizeAcceptance.SizeData,
            newItem: SizeAcceptance.SizeData,
        ): Boolean = (oldItem == newItem)

        override fun areContentsTheSame(
            oldItem: SizeAcceptance.SizeData,
            newItem: SizeAcceptance.SizeData,
        ): Boolean = (oldItem.seatNumber == newItem.seatNumber && oldItem.weight == newItem.weight)

    }

    override fun getItemCount(): Int = currentList.size

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AcceptanceSizeHolder {
        return AcceptanceSizeHolder(
            onItemClicked,
            parent.inflateLayout(R.layout.item_acceptance_size)
        )
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(
        holder: AcceptanceSizeAdapter.AcceptanceSizeHolder,
        position: Int
    ) {
        holder.onBind(currentList[position])
    }

}