package com.example.soft1c.fragment

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import androidx.annotation.IdRes
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
import com.example.soft1c.utils.MainActivity
import com.example.soft1c.utils.Utils
import com.example.soft1c.utils.Utils.Settings.fillBarcodes
import com.example.soft1c.utils.Utils.Settings.macAddress
import com.example.soft1c.utils.Utils.password
import com.example.soft1c.utils.Utils.user
import com.example.soft1c.viewmodel.BaseViewModel
import com.google.android.material.textfield.TextInputEditText
import com.phearme.macaddressedittext.MacAddressEditText

class AuthFragment : BaseFragment<FragmentAuthBinding>(FragmentAuthBinding::inflate) {

    private val baseViewModel: BaseViewModel by viewModels()
    private var error: String = ""
    private var requiredTypes = -1

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
                                Utils.debugMode = true
                                Utils.authorizationTimeout = 80L
                            } else {
                                Utils.debugMode = false
                                Utils.authorizationTimeout = 30L
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
            if (!it) {
                toast(getString(R.string.text_no_rights))
                return@observe
            }
            binding.txtError.text = ""
            try {
                if (user.acceptanceAccess) {
                    if (Utils.packages.isEmpty()) {
                        requiredTypes = 6
                        showDialogLoading()
                        baseViewModel.downloadType(Utils.ObjectModelType.ADDRESS)
                    } else {
                        if (user.loadingAccess) {
                            if (Utils.warehouse.isEmpty()) {
                                baseViewModel.loadType(Utils.ObjectModelType.CAR)
                            } else {
                                findNavController().navigate(R.id.action_authFragment_to_mainFragment)
                            }
                        } else
                            findNavController().navigate(R.id.action_authFragment_to_acceptanceFragment)
                    }
                } else if (user.loadingAccess) {
                    if (Utils.cars.isEmpty()) {
                        requiredTypes = 2
                        showDialogLoading()
                        baseViewModel.loadType(Utils.ObjectModelType.CAR)
                    } else {
                        findNavController().navigate(R.id.action_authFragment_to_loadingFragment)
                    }
                }
            } catch (_: IllegalArgumentException) {

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
        baseViewModel.toastResIdLiveData.observe(viewLifecycleOwner) {
            showPbLoading(false)
            closeDialogLoading()
            error = getString(it)
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
        if (key == 66 && keyEvent.action == KeyEvent.ACTION_UP) {
            with(binding) {
                view as TextInputEditText
                when (view) {

                    etxtUrlAdress -> {
                        if (etxtUrlPort.isEnabled && etxtUrlPort.isVisible) {
                            etxtUrlPort.requestFocus()
                            return true
                        }
                        return false
                    }

                    etxtUrlPort -> {
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
                        requireContext().hideKeyboard(etxtPassword)
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

    private fun loadFromSharedPref() {
        with(binding) {
            val address = getSharedPref(Network.KEY_ADDRESS)
            val port = getSharedPref(Network.KEY_PORT)
            val url = getSharedPref(Network.KEY_BASE_URL)
            val baseName = getSharedPref(Network.KEY_BASENAME)
            val username = getSharedPref(Network.KEY_USERNAME)
            val protocol = getSharedPref(Network.KEY_PROTOCOL).toIntOrNull() ?: 0
            etxtUrlAdress.setText(address)
            etxtUrlPort.setText(port)
            etxtBasename.setText(baseName)
            etxtUsername.setText(username)
            spinnerProtocols.setSelection(protocol)
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
            setSharedPref(Network.KEY_PROTOCOL, spinnerProtocols.selectedItemPosition.toString())
            val url = "${spinnerProtocols.selectedItem}://${address}${port}"

            setSharedPref(Network.KEY_BASE_URL, url)
            val baseName = checkFieldReturn(etxtBasename)
            if (baseName.isEmpty()) return else setSharedPref(Network.KEY_BASENAME, baseName)
            val username = checkFieldReturn(etxtUsername)
            if (username.isEmpty()) return else setSharedPref(Network.KEY_USERNAME, username)
            val password = checkFieldReturn(etxtPassword)
            if (password.isNotEmpty()) setSharedPref(Network.KEY_PASSWORD, password)
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
        val etxtMacAddress = dialogView.findViewById<MacAddressEditText>(R.id.item_mac_addres)
        val chbFillBarcode = dialogView.findViewById<CheckBox>(R.id.chb_fill_barcode)

        // Get the shared preferences
        val sharedPreferences = requireActivity().getSharedPreferences(Utils.Settings.SETTINGS_PREF_NAME, Context.MODE_PRIVATE)

        // Check if the MAC address exists in shared preferences
        macAddress = sharedPreferences.getString(Utils.Settings.MAC_ADDRESS_PREF, "")
        fillBarcodes = sharedPreferences.getString(Utils.Settings.FILL_BARCODE_PREF, "").toString()
        if (!macAddress.isNullOrEmpty()) {
            etxtMacAddress.setText(macAddress)
        }
        chbFillBarcode.isChecked = fillBarcodes.isNotEmpty() && fillBarcodes == "true"

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.text_enter_mac_address)
            .setView(dialogView)
            .setPositiveButton(R.string.text_save) { dialog, _ ->
                macAddress = etxtMacAddress.text.toString().trim()
                fillBarcodes = chbFillBarcode.isChecked.toString()


                // Save the MAC address to shared preferences
                sharedPreferences.edit().putString(Utils.Settings.MAC_ADDRESS_PREF, macAddress).apply()
                sharedPreferences.edit().putString(Utils.Settings.FILL_BARCODE_PREF, fillBarcodes).apply()

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
        if (requiredTypes < 0) {
            return
        }
        when (pairOf.first) {
            Utils.ObjectModelType.ADDRESS -> {
                Utils.addressess = pairOf.second.sortedBy {
                    (it as AnyModel.AddressModel).name
                }
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
                setTextDialogLoading(resources.getString(R.string.text_package))
                baseViewModel.downloadType(Utils.ObjectModelType._PACKAGE)
            }


            Utils.ObjectModelType._PACKAGE -> {
                Utils.packages = pairOf.second
                if (user.loadingAccess) {
                    if (Utils.warehouse.isEmpty()) {
                        setTextDialogLoading(resources.getString(R.string.text_number_of_auto))
                        baseViewModel.loadType(Utils.ObjectModelType.CAR)
                    } else {
                        closeDialogLoading()
                        if (findNavController().currentDestination?.id == R.id.authFragment)
                            navigate(R.id.action_authFragment_to_loadingFragment)
                    }
                } else {
                    closeDialogLoading()
                    if (findNavController().currentDestination?.id == R.id.authFragment)
                        navigate(R.id.action_authFragment_to_acceptanceFragment)
                }
            }

            Utils.ObjectModelType.EMPTY -> {
                closeDialogLoading()
                binding.txtError.text = "Error loading data"
            }
        }
    }

    private fun checkLoadingAndDownload(pairOf: Pair<Int, List<LoadingModel>>) {
        requiredTypes -= 1
        if (requiredTypes < 0) {
            return
        }
        when (pairOf.first) {
            Utils.ObjectModelType.CAR -> {
                Utils.cars = pairOf.second
//                setTextDialogLoading(resources.getString(R.string.text_container))
//                baseViewModel.loadType(Utils.ObjectModelType.CONTAINER)
                setTextDialogLoading(resources.getString(R.string.text_warehouse))
                baseViewModel.loadType(Utils.ObjectModelType.WAREHOUSE)
            }

            Utils.ObjectModelType.CONTAINER -> {
                Utils.container = pairOf.second
                setTextDialogLoading(resources.getString(R.string.text_warehouse))
                baseViewModel.loadType(Utils.ObjectModelType.WAREHOUSE)
            }

            Utils.ObjectModelType.WAREHOUSE -> {
                Utils.warehouse = pairOf.second
                if (Utils.packages.isEmpty()) {
                    setTextDialogLoading(resources.getString(R.string.text_product_type))
                    baseViewModel.downloadType(Utils.ObjectModelType._PACKAGE)
                }
                closeDialogLoading()
                if (user.acceptanceAccess)
                    navigate(R.id.action_authFragment_to_mainFragment)
                else
                    navigate(R.id.action_authFragment_to_loadingFragment)
            }
        }
    }

    private fun navigate(@IdRes action: Int){
        Network.refreshConnection(Utils.clientTimeout)
        findNavController().navigate(action)
    }

}