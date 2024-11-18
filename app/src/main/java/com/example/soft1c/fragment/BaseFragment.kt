package com.example.soft1c.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.example.soft1c.R
import com.example.soft1c.utils.Utils
import timber.log.Timber

open class BaseFragment<T : ViewBinding>(
    private val layoutInflater: (
        layoutInflate: LayoutInflater,
        viewGroup: ViewGroup?,
        attachToParent: Boolean,
    ) -> T,
) : Fragment() {

    private var _binding: T? = null
    private lateinit var dialogLoading: AlertDialog
    private lateinit var viewDialog: View

    val binding: T
        get() = _binding!!

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = layoutInflater.invoke(inflater, container, false)
        return _binding?.root
    }

    override fun onResume() {
        super.onResume()
        closeDialogLoading()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun toast(text: String) {
        closeDialogLoading()
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
        Timber.d(text)
    }

    fun toastLong(text: String) {
        closeDialogLoading()
        Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show()
        Timber.d(text)
    }

    fun errorDialog(message: String, isClose: Boolean) {
        closeDialogLoading()
        val errorDialogBuilder = AlertDialog.Builder(requireContext())
        errorDialogBuilder.setCancelable(false)
        errorDialogBuilder.setMessage(message)
        errorDialogBuilder.setNegativeButton(R.string.text_close) { dialog, _ ->
            if (isClose) {
                requireActivity().onBackPressed()
            }
            dialog.dismiss()
        }
        val errorDialog = errorDialogBuilder.create()
        errorDialog.show()
    }

    fun setSharedPref(key: String, value: String) {
        val sharedPreferences = requireContext().getSharedPreferences("", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putString(key, value)
            .apply()
    }

    fun getSharedPref(key: String): String {
        val sharedPreferences = requireContext().getSharedPreferences("", Context.MODE_PRIVATE)
        return sharedPreferences.getString(key, "") ?: ""
    }

    fun logFileDialog() {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_default, null)
        val logTextView = dialogView.findViewById<TextView>(R.id.txt_doc_info)
        logTextView.text = Utils.logFor1C

        val checkConnectionButton = dialogView.findViewById<Button>(R.id.btn_check_connection)
        checkConnectionButton.visibility = View.GONE

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.doc_log_info))
            .setView(dialogView)
//            .setPositiveButton("Send") { dialog, _ ->
//                dialog.dismiss()
//                showEditWarningDialog(disabilityReason, false)
//            }
            .setNegativeButton(R.string.text_close) { dialog, _ ->
                // Close the dialog
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    fun setTextDialogLoading(text: String = "") {
        if (text.isNotEmpty()) {
            val txt = viewDialog.findViewById<TextView>(R.id.txt_about)
            txt.text = resources.getString(R.string.text_downloading_text, text)
        }
    }


    fun showDialogLoading() {
        if (!::dialogLoading.isInitialized || !dialogLoading.isShowing) {
            viewDialog = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_loading, null)
            dialogLoading = AlertDialog.Builder(requireContext())
                .setView(viewDialog)
                .setCancelable(false)
                .create()
            dialogLoading.show()
        }
    }

    /**
     * Диалог с информацией о том, почему нельзя редактировать документ
     */
    fun showEditWarningDialog(reason: SpannableStringBuilder?, showCheckBox: Boolean, navigateToLog: Boolean, networkCheck: () -> Unit){
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_default, null)
        val reasonTextView = dialogView.findViewById<TextView>(R.id.txt_reason)
        reasonTextView.setText(reason, TextView.BufferType.SPANNABLE)
        val checkbox = dialogView.findViewById<CheckBox>(R.id.chb_dont_show)
        val checkConnectionButton = dialogView.findViewById<Button>(R.id.btn_check_connection)
        if (showCheckBox){
            checkbox.visibility = View.VISIBLE
        }
        if(reasonTextView.text.isEmpty()){
            reasonTextView.visibility = View.GONE
        }

        val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton(R.string.text_close) { dialog, _ ->
                if (checkbox.isChecked) {
                    setSharedPref(Utils.Settings.SHOW_DISABILITY_DIALOG, "НеПоказывать")
                }
                dialog.dismiss()
            }

        if (navigateToLog) {
            dialogBuilder.setNegativeButton(R.string.text_more) { dialog, _ ->
                dialog.dismiss()
                logFileDialog()
            }
            checkConnectionButton.setOnClickListener {
                networkCheck()
            }
        }else{
            checkConnectionButton.visibility = View.GONE
        }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    fun closeDialogLoading() {
        if (!::dialogLoading.isInitialized) {
            return
        }
        if (dialogLoading.isShowing) {
            dialogLoading.dismiss()  // Use dismiss instead of cancel
        }
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

}