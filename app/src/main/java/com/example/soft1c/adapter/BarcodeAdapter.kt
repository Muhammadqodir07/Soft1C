package com.example.soft1c.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.soft1c.R

//Класс определяет, как должен отображаться каждый элемент списка сканированных штрих-кодов.
class BarcodeAdapter : RecyclerView.Adapter<BarcodeAdapter.BarcodeViewHolder>() {

    private val scannedBarcodes = mutableListOf<String>()

    //Внутренний класс предоставляющий ссылки на элементы пользовательского интерфейса, которые будут отображаться для каждого элемента списка.
    inner class BarcodeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val barcodeCount: TextView = view.findViewById(R.id.txt_barcode_count)
        private val barcodeData: TextView = view.findViewById(R.id.txt_barcode)

        fun bind(barcode: String, position: Int) {
            barcodeCount.text = (position + 1).toString()
            barcodeData.text = barcode
        }
    }

    //Создает новый объект BarcodeViewHolder, который будет использоваться для отображения каждого элемента списка.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarcodeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_barcode, parent, false)
        return BarcodeViewHolder(view)
    }

    //Связывает данные элемента списка с элементом пользовательского интерфейса, который предоставляется объектом BarcodeViewHolder.
    override fun onBindViewHolder(holder: BarcodeViewHolder, position: Int) {
        holder.bind(scannedBarcodes[position], position)
    }

    //Возвращает количество элементов в списке
    override fun getItemCount(): Int {
        return scannedBarcodes.size
    }

    //Добавляет новый элемент в список сканированных штрих-кодов и обновляет представление RecyclerView (Элемент пользовательского интерфейса).
    fun addBarcodeData(barcodeData: String) {
        scannedBarcodes.add(barcodeData)
        notifyItemInserted(scannedBarcodes.size - 1)
    }

    fun removeBarcode(position: Int) {
        scannedBarcodes.removeAt(position)
        notifyItemRemoved(position)
    }

    //Очищает список сканированных штрих-кодов и обновляет представление RecyclerView.
    fun clearBarcodeData() {
        scannedBarcodes.clear()
        notifyDataSetChanged()
    }
}

