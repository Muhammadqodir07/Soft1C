package com.example.soft1c.fragment

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.soft1c.R
import com.example.soft1c.databinding.FragmentAuthBinding
import com.example.soft1c.network.Network
import com.example.soft1c.repository.model.AnyModel
import com.example.soft1c.repository.model.LoadingModel
import com.example.soft1c.utils.Demo
import com.example.soft1c.utils.MainActivity
import com.example.soft1c.utils.Utils
import com.example.soft1c.utils.Utils.password
import com.example.soft1c.utils.calculator.CalcDialog
import com.example.soft1c.viewmodel.BaseViewModel
import com.google.android.material.textfield.TextInputEditText
import com.phearme.macaddressedittext.MacAddressEditText
import java.io.File
import java.math.BigDecimal

class AuthFragment : BaseFragment<FragmentAuthBinding>(FragmentAuthBinding::inflate),
    CalcDialog.CalcDialogCallback {

    private val baseViewModel: BaseViewModel by viewModels()
    private var error: String = ""
    private var requiredTypes = 6
    private lateinit var accessPair: Pair<Boolean, Boolean>
    val calcDialog = CalcDialog()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.includeToolbar.toolbar.title = resources.getString(R.string.text_title_auth)
        binding.includeToolbar.toolbar.setOnMenuItemClickListener(object :
            Toolbar.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                requireActivity().let { fragActivity ->
                    when (item?.itemId) {
                        R.id.item_chinese -> {
                            fragActivity as MainActivity
                            fragActivity.setLocale("zh")
                            //refreshLayout()
                            return true
                        }

                        R.id.item_english -> {
                            fragActivity as MainActivity
                            fragActivity.setLocale("eng")
                            //refreshLayout()
                            return true
                        }

                        R.id.item_russian -> {
                            fragActivity as MainActivity
                            fragActivity.setLocale("ru")
                            //refreshLayout()
                            return true
                        }

                        R.id.item_macAddress -> {
                            fragActivity as MainActivity
                            macAddressDialog()
                            return true
                        }

                        R.id.checkable_item -> {
                            // Toggle the checked state
                            item.isChecked = !item.isChecked
                            if (item.isChecked) {
                                Utils.clientTimeout = 80L
                            } else {
                                Utils.clientTimeout = 30L
                            }
                        }

                        else -> {
                            return false
                        }
                    }
                }
                return false
            }
        })
        initUI()
        obserViewModels()
        putFocus()
    }

    private fun obserViewModels() {
        baseViewModel.authLiveData.observe(viewLifecycleOwner) {
            showPbLoading(false)
            showDialogLoading()
            if (!it) {
                toast(getString(R.string.text_no_rights))
                return@observe
            }
            if (Utils.zones.isEmpty()) {
                baseViewModel.downloadType(Utils.ObjectModelType.ADDRESS)
            }
        }
        baseViewModel.loadAuthLiveData.observe(viewLifecycleOwner) {
            showPbLoading(false)
            if (it) {
                findNavController().navigate(R.id.action_authFragment_to_loadingFragment)
            }
        }
        baseViewModel.toastLiveData.observe(viewLifecycleOwner) {
            showPbLoading(false)
            closeDialogLoading()
            error = it
            binding.txtError.text = error
        }
        baseViewModel.anyObjectLiveData.observe(viewLifecycleOwner, ::checkAcceptanceAndDownload)
        baseViewModel.loadingObjectLiveData.observe(viewLifecycleOwner, ::checkLoadingAndDownload)
    }

    private fun initUI() {
        loadFromSharedPref()
        with(binding) {
            cardLogin.setOnClickListener {
                setBase()
                val demo = Demo()
               demo.loadProfile()
               baseViewModel.acceptanceAuth()
                showPbLoading(true)

            }
            etxtUrlAdress.setOnKeyListener(::customSetOnKeyListener)
            etxtBasename.setOnKeyListener(::customSetOnKeyListener)
            etxtUsername.setOnKeyListener(::customSetOnKeyListener)
            etxtPassword.setOnKeyListener(::customSetOnKeyListener)
            etxtUrlPort.setOnKeyListener(::customSetOnKeyListener)

            etxtUrlAdress.setOnFocusChangeListener(::etxtFocusChangeListener)
            etxtBasename.setOnFocusChangeListener(::etxtFocusChangeListener)
            etxtUsername.setOnFocusChangeListener(::etxtFocusChangeListener)
            etxtPassword.setOnFocusChangeListener(::etxtFocusChangeListener)
            etxtUrlPort.setOnFocusChangeListener(::etxtFocusChangeListener)


        }
    }

    private fun customSetOnKeyListener(view: View, key: Int, keyEvent: KeyEvent): Boolean {
        if (key == 66 && keyEvent.action == KeyEvent.ACTION_DOWN) {
            with(binding) {

                val etxtView = view as TextInputEditText

                when (etxtView) {

                    etxtUrlAdress -> {
                        if (etxtBasename.isEnabled && etxtBasename.isVisible) {
                            etxtBasename.requestFocus()
                            return true
                        }
                        return false
                    }
                    etxtBasename -> {
                        if (etxtUsername.isEnabled && etxtUsername.isVisible) {
                            etxtUsername.requestFocus()
                            return true
                        }
                        return false
                    }
                    etxtUsername -> {
                        if (etxtPassword.isEnabled && etxtPassword.isVisible) {
                            etxtPassword.requestFocus()
                            return true
                        }
                        return false
                    }

                    else -> {
                        return false
                    }
                }
            }
        }
        return false
    }


    private fun etxtFocusChangeListener(view: View, hasFocus: Boolean) {
        if (hasFocus) {
            view as TextInputEditText
            view.text?.let {
                view.selectAll()
            }
        }
    }
    override fun onValueEntered(requestCode: Int, value: BigDecimal?) {
        toast("$requestCode  :  $value")
    }

    private fun loadFromSharedPref() {
        with(binding) {
            val address = getSharedPref(Network.KEY_ADDRESS)
            val port = getSharedPref(Network.KEY_PORT)
            val url = getSharedPref(Network.KEY_BASE_URL)
            val baseName = getSharedPref(Network.KEY_BASENAME)
            val username = getSharedPref(Network.KEY_USERNAME)
            etxtUrlAdress.setText(address)
            etxtUrlPort.setText(port)
            etxtBasename.setText(baseName)
            etxtUsername.setText(username)
            Utils.setAttributes(
                url,
                baseName,
                username,
                password,
                resources.getString(R.string.app_lang)
            )
        }
    }

    private fun setBase() {
        with(binding) {
            val address = checkFieldReturn(etxtUrlAdress)
            setSharedPref(Network.KEY_ADDRESS, address)
            if (address.isEmpty()) return
            var port = etxtUrlPort.text.toString()
            setSharedPref(Network.KEY_PORT, port)
            if (port.isEmpty()) port = "" else port = ":${port}"
            val url = "${spinnerProtocols.selectedItem}://${address}${port}"

            setSharedPref(Network.KEY_BASE_URL, url)
            val baseName = checkFieldReturn(etxtBasename)
            if (baseName.isEmpty()) return else setSharedPref(Network.KEY_BASENAME, baseName)
            val username = checkFieldReturn(etxtUsername)
            if (username.isEmpty()) return else setSharedPref(Network.KEY_USERNAME, username)
            val password = checkFieldReturn(etxtPassword)
            if (password.isEmpty()) return else setSharedPref(Network.KEY_PASSWORD, password)
            Utils.setAttributes(
                url,
                baseName,
                username,
                password,
                resources.getString(R.string.app_lang)
            )
        }
    }

    private fun checkFieldReturn(etxt: TextInputEditText): String {
        if (etxt.text!!.isEmpty()) {
            etxt.error = resources.getString(R.string.text_field_is_empyt)
            return ""
        }
        etxt.error = null
        return etxt.text.toString()
    }

    private fun showPbLoading(show: Boolean) {
        with(binding) {
            cardAddress.isEnabled = !show
            cardLogin.isEnabled = !show
            cardUser.isEnabled = !show
            pbLoading.isVisible = show
        }
    }

    private fun putFocus() {
        with(binding) {
            when {
                etxtUrlAdress.text?.isEmpty() == true -> etxtUrlAdress.requestFocus()
                etxtBasename.text?.isEmpty() == true -> etxtBasename.requestFocus()
                etxtUrlPort.text?.isEmpty() == true -> etxtUrlPort.requestFocus()
                etxtUsername.text?.isEmpty() == true -> etxtUsername.requestFocus()
                etxtPassword.text?.isEmpty() == true -> etxtPassword.requestFocus()
                else -> return
            }
        }
    }

    private fun macAddressDialog() {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_mac_address, null)
        val macAddressEditText = dialogView.findViewById<MacAddressEditText>(R.id.item_mac_addres)

        // Check if the cache file exists
        val cacheFile = File(requireActivity().cacheDir, "mac_address.txt")
        if (cacheFile.exists()) {
            val macAddress = cacheFile.readText()
            macAddressEditText.setText(macAddress)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.text_enter_mac_address)
            .setView(dialogView)
            .setPositiveButton(R.string.text_save) { dialog, _ ->
                val macAddress = macAddressEditText.text.toString().trim()

                // Save the Mac address to the cache
                cacheFile.writeText(macAddress)

                // Close the dialog
                dialog.dismiss()
            }
            .setNegativeButton(R.string.text_close) { dialog, _ ->
                // Close the dialog
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun checkAcceptanceAndDownload(pairOf: Pair<Int, List<AnyModel>>) {
        requiredTypes -= 1
        when (pairOf.first) {
            Utils.ObjectModelType.ADDRESS -> {
                Utils.addressess = pairOf.second.sortedBy {
                    (it as AnyModel.AddressModel).name
                }
                setTextDialogLoading(resources.getString(R.string.text_package))
                baseViewModel.downloadType(Utils.ObjectModelType._PACKAGE)
            }

            Utils.ObjectModelType._PACKAGE -> {
                Utils.packages = pairOf.second
                setTextDialogLoading(resources.getString(R.string.text_product_type))
                baseViewModel.downloadType(Utils.ObjectModelType.PRODUCT_TYPE)
            }

            Utils.ObjectModelType.PRODUCT_TYPE -> {
                Utils.productTypes = pairOf.second
                setTextDialogLoading(resources.getString(R.string.text_zone))
                baseViewModel.downloadType(Utils.ObjectModelType.ZONE)
            }

            Utils.ObjectModelType.ZONE -> {
                Utils.zones = pairOf.second
//                setTextDialogLoading(resources.getString(R.string.text_number_of_auto))
                closeDialogLoading()
                findNavController().navigate(R.id.action_authFragment_to_acceptanceFragment)
            }
        }
    }

    private fun checkLoadingAndDownload(pairOf: Pair<Int, List<LoadingModel>>) {
        requiredTypes -= 1
        when (pairOf.first) {
            Utils.ObjectModelType.CAR -> {
                Utils.cars = pairOf.second
                setTextDialogLoading(resources.getString(R.string.text_warehouse))
                baseViewModel.loadType(Utils.ObjectModelType.WAREHOUSE)
            }

            Utils.ObjectModelType.WAREHOUSE -> {
                Utils.warehouse = pairOf.second
                closeDialogLoading()
                if (accessPair.first && !accessPair.second) {
                    findNavController().navigate(R.id.action_authFragment_to_acceptanceFragment)
                } else if (!accessPair.first && accessPair.second) {
                    findNavController().navigate(R.id.action_authFragment_to_loadingFragment)
                } else if (accessPair.first && accessPair.second) {
                    findNavController().navigate(R.id.action_authFragment_to_mainFragment)
                }
            }
        }
    }

}