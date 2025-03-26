package com.example.soft1c.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView

class CarAdapter(
    private val context: Context,
    private val resource: Int,
    private val textViewResourceId: Int,
    private val items: MutableList<String>
) : ArrayAdapter<String>(context, resource, textViewResourceId, items) {

    private val tempItems: MutableList<String> = ArrayList(items)
    private val suggestions: MutableList<String> = ArrayList()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // This method displays the selected item in the AutoCompleteTextView
        val view = convertView ?: LayoutInflater.from(context).inflate(resource, parent, false)
        val item = getItem(position)
        val textView = view.findViewById<TextView>(textViewResourceId)
        textView.text = item // Display the string directly
        return view
    }

    override fun getFilter(): Filter = carFilter

    private val carFilter: Filter = object : Filter() {
        override fun convertResultToString(resultValue: Any): CharSequence {
            return resultValue as String // Return the string itself
        }

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filterResults = FilterResults()
            if (constraint != null) {
                suggestions.clear()
                for (item in tempItems) {
                    if (item.lowercase().contains(constraint.toString().lowercase())) {
                        suggestions.add(item)
                    }
                }
                filterResults.values = suggestions
                filterResults.count = suggestions.size
            }
            return filterResults
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            @Suppress("UNCHECKED_CAST")
            val filterList = results?.values as? List<String>
            if (filterList != null && results.count > 0) {
                clear()
                addAll(filterList)
                notifyDataSetChanged()
            }
        }
    }
}