package com.example.soft1c.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.soft1c.R
import com.example.soft1c.extension.inflateLayout
import com.example.soft1c.repository.model.ExpandableLoadingList
import com.example.soft1c.repository.model.LoadingBarcodeChild
import com.example.soft1c.repository.model.LoadingBarcodeParent
import com.example.soft1c.utils.Utils.inputDateFormat
import com.example.soft1c.utils.Utils.outputDateFormat

//Класс определяет, как должен отображаться каждый элемент списка сканированных штрих-кодов.
class BarcodeAdapter(val onRemoveBarcode: (LoadingBarcodeChild) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val scannedBarcodes = mutableListOf<ExpandableLoadingList>()

    //Внутренний класс предоставляющий ссылки на элементы пользовательского интерфейса, которые будут отображаться для каждого элемента списка.
    inner class BarcodeChildViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val barcodeNumber: TextView = view.findViewById(R.id.txt_barcode)
        private val barcodeSeatNumber: TextView = view.findViewById(R.id.txt_seat_number)
//        private val actionButton: ImageButton = view.findViewById(R.id.action_button)

        fun bind(barcode: LoadingBarcodeChild, position: Int) {
            barcodeNumber.text = barcode.barcode
            barcodeSeatNumber.text = barcode.seatNumber.toString()
//            actionButton.setOnClickListener {
//                removeBarcode(position)
//                onRemoveBarcode(barcode)
//            }
        }
    }

    inner class BarcodeParentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val barcodeDocNumber: TextView = view.findViewById(R.id.txt_doc_number)
        private val barcodeClient: TextView = view.findViewById(R.id.txt_code_client)
        private val barcodeDate: TextView = view.findViewById(R.id.txt_date)
        private val barcodeSeatsCount: TextView = view.findViewById(R.id.txt_total_seats)

        fun bind(barcodeGroup: LoadingBarcodeParent, position: Int) {
            barcodeDocNumber.text = barcodeGroup.acceptanceNumber.replace("[A-Z]".toRegex(), "").trimStart('0')
            barcodeClient.text = barcodeGroup.clientCode
            barcodeDate.text = inputDateFormat.parse(barcodeGroup.date)
                ?.let { outputDateFormat.format(it) }
            barcodeSeatsCount.text = barcodeGroup.totalSeats.toString()
            itemView.setOnClickListener {
                when (scannedBarcodes[position].isExpanded) {
                    true -> {
                        scannedBarcodes[position].isExpanded = false
                        collapseRow(position)
                    }
                    false -> {
                        scannedBarcodes[position].isExpanded = true
                        expandRow(position)
                    }
                }
            }
        }
    }

    //Создает новый объект BarcodeViewHolder, который будет использоваться для отображения каждого элемента списка.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            ExpandableLoadingList.PARENT -> {
                val view = parent.inflateLayout(R.layout.item_barcode_parent)
                return BarcodeParentViewHolder(view)
            }
            ExpandableLoadingList.CHILD -> {
                val view = parent.inflateLayout(R.layout.item_barcode_child)
                return BarcodeChildViewHolder(view)
            }
            else -> {
                val view = parent.inflateLayout(R.layout.item_barcode_parent)
                return BarcodeParentViewHolder(view)
            }
        }
    }

    //Связывает данные элемента списка с элементом пользовательского интерфейса, который предоставляется объектом BarcodeViewHolder.
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        //holder.bind(scannedBarcodes[position], position)
        val row = scannedBarcodes[position]
        when(row.type){
            ExpandableLoadingList.PARENT -> {
                (holder as BarcodeParentViewHolder)
                holder.bind(row.loadingParent, position)
            }


            ExpandableLoadingList.CHILD -> {
                (holder as BarcodeChildViewHolder)
                holder.bind(row.loadingChild, position)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = scannedBarcodes[position].type

    //Возвращает количество элементов в списке
    override fun getItemCount(): Int = scannedBarcodes.size

    //Добавляет новый элемент в список сканированных штрих-кодов и обновляет представление RecyclerView (Элемент пользовательского интерфейса).
    fun addBarcodeData(newBarcodes: ExpandableLoadingList) {
        val parent = scannedBarcodes.find {
            it.type == ExpandableLoadingList.PARENT &&
                    it.loadingParent.acceptanceUid == newBarcodes.loadingParent.acceptanceUid
        }
        if (parent != null) {
            val existingBarcodes = parent.loadingParent.barcodes.toMutableList()

            newBarcodes.loadingParent.barcodes.forEach { newBarcode ->
                if (!existingBarcodes.any { it.barcode == newBarcode.barcode }) {
                    existingBarcodes.add(0, newBarcode)
                }
            }

            parent.loadingParent.barcodes = existingBarcodes
            parent.loadingParent.totalSeats = existingBarcodes.size

            if (parent.isExpanded) {
                var parentPosition = scannedBarcodes.indexOf(parent)

                newBarcodes.loadingParent.barcodes.forEach { newBarcode ->
                    if (!scannedBarcodes.any {
                            it.type == ExpandableLoadingList.CHILD &&
                                    it.loadingChild.barcode == newBarcode.barcode
                        }) {
                        scannedBarcodes.add(++parentPosition, ExpandableLoadingList(ExpandableLoadingList.CHILD, newBarcode))
                    }
                }
            }

            notifyDataSetChanged()
        } else {
            scannedBarcodes.add(0, newBarcodes)
            notifyItemInserted(0)
        }
    }

    private fun expandRow(position: Int){
        val row = scannedBarcodes[position]
        var nextPosition = position
        when (row.type) {
            ExpandableLoadingList.PARENT -> {
                for(child in row.loadingParent.barcodes){
                    scannedBarcodes.add(++nextPosition, ExpandableLoadingList(ExpandableLoadingList.CHILD, child))
                }
                notifyDataSetChanged()
            }
            ExpandableLoadingList.CHILD -> {
                notifyDataSetChanged()
            }
        }
    }

    private fun collapseRow(position: Int){
        val row = scannedBarcodes[position]
        var nextPosition = position + 1
        when (row.type) {
            ExpandableLoadingList.PARENT -> {
                outerloop@ while (true) {

                    if (nextPosition == scannedBarcodes.size || scannedBarcodes[nextPosition].type == ExpandableLoadingList.PARENT) {

                        break@outerloop
                    }

                    scannedBarcodes.removeAt(nextPosition)
                }

                notifyDataSetChanged()
            }
        }
    }

    fun removeBarcodeByBarcode(barcode: String) {
        val parentIndex = scannedBarcodes.indexOfFirst {
            it.type == ExpandableLoadingList.PARENT &&
                    it.loadingParent.barcodes.any { child -> child.barcode == barcode }
        }

        if (parentIndex != -1) {
            val parent = scannedBarcodes[parentIndex].loadingParent

            val childToRemove = parent.barcodes.find { it.barcode == barcode }

            if (childToRemove != null) {
                parent.barcodes = parent.barcodes.filter { it.barcode != barcode }
                parent.totalSeats = parent.barcodes.size

                onRemoveBarcode(childToRemove)

                if (parent.barcodes.isEmpty()) {
                    scannedBarcodes.removeAt(parentIndex)
                }
            }
        }

        val childToRemove = scannedBarcodes.find {
            it.type == ExpandableLoadingList.CHILD && it.loadingChild.barcode == barcode
        }?.loadingChild

        val removedChild = scannedBarcodes.removeAll {
            it.type == ExpandableLoadingList.CHILD && it.loadingChild.barcode == barcode
        }

        if (removedChild && childToRemove != null) {
            onRemoveBarcode(childToRemove)
        }

        notifyDataSetChanged()
    }

    fun removeBarcodeByUid(acceptanceUid: String) {
        val itemsToRemove = scannedBarcodes.filter {
            (it.type == ExpandableLoadingList.PARENT && it.loadingParent.acceptanceUid == acceptanceUid) ||
                    (it.type == ExpandableLoadingList.CHILD && it.loadingChild.parentUid == acceptanceUid)
        }

        itemsToRemove.forEach { item ->
            scannedBarcodes.remove(item)

            when (item.type) {
                ExpandableLoadingList.PARENT -> {
                    item.loadingParent.barcodes.forEach { child ->
                        onRemoveBarcode(child)
                    }
                }
                ExpandableLoadingList.CHILD -> {
                    onRemoveBarcode(item.loadingChild)
                }
            }
        }

        notifyDataSetChanged()
    }

    fun getList(): List<ExpandableLoadingList>{
        return scannedBarcodes.filter { it.type == ExpandableLoadingList.PARENT }
    }
    fun getCount(): Int{
        return scannedBarcodes.filter { it.type == ExpandableLoadingList.PARENT }.sumOf { it.loadingParent.totalSeats }
    }

    fun find(barcode: String): ExpandableLoadingList? {
        val child =
            scannedBarcodes.find { it.type == ExpandableLoadingList.CHILD && it.loadingChild.barcode == barcode }
        if (child == null) {
            val parents =
                scannedBarcodes.find { it.type == ExpandableLoadingList.PARENT }?.loadingParent
            if (parents == null) return null
            val childOfParent = parents.barcodes.find { it.barcode == barcode }
            if (childOfParent == null) return null
            return ExpandableLoadingList(
                type = ExpandableLoadingList.CHILD,
                loadingChild = childOfParent
            )
        } else
            return child
    }

    //Очищает список сканированных штрих-кодов и обновляет представление RecyclerView.
    fun clearBarcodeData() {
        scannedBarcodes.clear()
        notifyDataSetChanged()
    }
}