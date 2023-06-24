package com.example.soft1c.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.SearchView
import com.example.soft1c.R
import com.example.soft1c.utils.Utils
import com.example.soft1c.databinding.FragmentSearchBinding
import com.example.soft1c.repository.model.AnyModel

class SearchFragment : BaseFragment<FragmentSearchBinding>(FragmentSearchBinding::inflate) {

    lateinit var listAdapter: ArrayAdapter<String>
    lateinit var modelList: ArrayList<String>
    var model: Int = 0
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        modelList = ArrayList()
        arguments?.let {
            model = it.getInt(KEY_MODEL, 0)
        }
        with(binding.includeToolbar.toolbar) {
            var resourseId = 0
            when (model) {
                Utils.ObjectModelType.ZONE -> {
                    resourseId = R.string.text_zone
                    modelList.addAll(Utils.zones.map {
                        (it as AnyModel.Zone).name
                    })
                }
                Utils.ObjectModelType.ADDRESS -> {
                    resourseId = R.string.text_address
                    modelList.addAll(Utils.addressess.map {
                        (it as AnyModel.AddressModel).name
                    })
                }
                Utils.ObjectModelType.PRODUCT_TYPE -> {
                    resourseId = R.string.text_product_type
                    modelList.addAll(Utils.productTypes.map {
                        (it as AnyModel.ProductType).name
                    })
                }
                Utils.ObjectModelType._PACKAGE -> {
                    resourseId = R.string.text_package
                    modelList.addAll(Utils.packages.map {
                        (it as AnyModel.PackageModel).name
                    })
                }
                else -> {
                    resourseId = R.string.text_title_search
                }
            }
            title = resources.getString(resourseId)
            setNavigationOnClickListener {
                activity?.onBackPressed()
            }
        }
        initUI()
    }

    override fun onResume() {
        super.onResume()
        binding.svText.requestFocus()
    }

    private fun initUI() {
        with(binding) {
            svText.setOnQueryTextFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    view.postDelayed({
                        activity?.let {
                            val imm =
                                it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.showSoftInput(view.findFocus(), 0)
                        }
                    }, 200)

                }
            }
            svText.isIconified = false
            svText.requestFocusFromTouch()

            listAdapter = ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                modelList
            )
            listviewResult.adapter = listAdapter

            listviewResult.setOnItemClickListener { _, _, position, _ ->
                val element = selectCloseModel(position) ?: return@setOnItemClickListener
                Utils.anyModel = element
                activity?.onBackPressed()
            }

            svText.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (listAdapter.count <= 0) return false
                    val element = selectCloseModel(0) ?: return false
                    Utils.anyModel = element
                    activity?.onBackPressed()
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    listAdapter.filter.filter(newText)
                    return false
                }
            })
        }
    }

    private fun selectCloseModel(position: Int): AnyModel? {
        return when (model) {
            Utils.ObjectModelType.ZONE -> {
                Utils.zones.find {
                    (it as AnyModel.Zone).name == listAdapter.getItem(position)
                }
            }
            Utils.ObjectModelType.ADDRESS -> {
                Utils.addressess.find {
                    (it as AnyModel.AddressModel).name == listAdapter.getItem(position)
                }
            }
            Utils.ObjectModelType.PRODUCT_TYPE -> {
                Utils.productTypes.find {
                    (it as AnyModel.ProductType).name == listAdapter.getItem(position)
                }
            }
            Utils.ObjectModelType._PACKAGE -> {
                Utils.packages.find {
                    (it as AnyModel.PackageModel).name == listAdapter.getItem(position)
                }
            }
            else -> null
        }
    }

    companion object {
        const val KEY_MODEL = "key_model"
    }

}