package com.example.soft1c.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.soft1c.R
import com.example.soft1c.extension.inflateLayout
import com.example.soft1c.repository.model.LoadingBarcode
import com.example.soft1c.utils.Utils.inputDateFormat
import com.example.soft1c.utils.Utils.outputDateFormat

//Класс определяет, как должен отображаться каждый элемент списка сканированных штрих-кодов.
class BarcodeAdapter(val onRemoveBarcode: (LoadingBarcode) -> Unit) :
    RecyclerView.Adapter<BarcodeAdapter.BarcodeViewHolder>() {

    private val scannedBarcodes = mutableListOf<LoadingBarcode>()

    //Внутренний класс предоставляющий ссылки на элементы пользовательского интерфейса, которые будут отображаться для каждого элемента списка.
    inner class BarcodeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val barcodeClient: TextView = view.findViewById(R.id.txt_code_client)
        private val barcodeDate: TextView = view.findViewById(R.id.txt_date)
        private val barcodePackage: TextView = view.findViewById(R.id.txt_package)
        private val actionButton: ImageButton = view.findViewById(R.id.action_button)

        fun bind(barcode: LoadingBarcode, position: Int) {
            barcodeClient.text = barcode.clientCode
            barcodeDate.text = inputDateFormat.parse(barcode.date)
                ?.let { outputDateFormat.format(it) }
            barcodePackage.text = barcode.packageType.filter { !it.isDigit() }
            actionButton.setOnClickListener {
                removeBarcode(position)
                onRemoveBarcode(barcode)
            }
        }
    }

    //Создает новый объект BarcodeViewHolder, который будет использоваться для отображения каждого элемента списка.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarcodeViewHolder {
        val view = parent.inflateLayout(R.layout.item_barcode)
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
    fun addBarcodeData(barcodeData: LoadingBarcode) {
        scannedBarcodes.add(barcodeData)
        notifyItemInserted(scannedBarcodes.size - 1)
    }

    fun removeBarcode(position: Int) {
        scannedBarcodes.removeAt(position)
        notifyItemRemoved(position)
        notifyDataSetChanged()
    }

    fun removeBarcodeByBarcode(barcode: String) {
        val barcodeForDelete = scannedBarcodes.find { it.barcode == barcode }
        if (barcodeForDelete != null) {
            scannedBarcodes.remove(barcodeForDelete)
            onRemoveBarcode(barcodeForDelete)
            notifyDataSetChanged()
        }
    }

    fun removeBarcodeByUid(acceptanceUid: String) {
        val barcodesToRemove = scannedBarcodes.filter { it.acceptanceUid == acceptanceUid }

        barcodesToRemove.forEach { barcode ->
            scannedBarcodes.remove(barcode)
            onRemoveBarcode(barcode)
        }

        notifyDataSetChanged()
    }
    fun getList(): List<LoadingBarcode>{
        return scannedBarcodes
    }

    //Очищает список сканированных штрих-кодов и обновляет представление RecyclerView.
    fun clearBarcodeData() {
        scannedBarcodes.clear()
        notifyDataSetChanged()
    }
}

