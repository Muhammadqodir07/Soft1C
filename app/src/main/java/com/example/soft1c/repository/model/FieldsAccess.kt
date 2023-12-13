package com.example.soft1c.repository.model

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import com.example.soft1c.R

data class FieldsAccess(
    var readOnly: Boolean = true,
    var weightEnable: Boolean = false,
    var sizeEnable: Boolean = false,
    var isCreator: Boolean = false,
    var zoneEnable: Boolean = false,
    var chBoxEnable: Boolean = false,
    var properties: Boolean = false,
    var packageEnable: Boolean = false
) {
    fun getInaccessibilityReason(
        context: Context,
        user: User,
        isPrinted: Boolean = false,
        creator: String,
        type: DocumentType
    ): SpannableStringBuilder {
        val reasons = mutableListOf<String>()

        var title = SpannableString("")
        with(context) {
            val reasonTitle = SpannableString(getString(R.string.disable_reason))
            reasonTitle.setSpan(RelativeSizeSpan(1.3f), 0, reasonTitle.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            val spannableStringBuilder = SpannableStringBuilder()

            if (readOnly) {
                title = SpannableString("${getString(R.string.disabled)}: ${getString(R.string.all_fields)}")

                if (!isCreator || (user.username != creator && !user.isAdmin)) {
                    when(type){
                        DocumentType.ACCEPTANCE -> reasons.add(getString(R.string.another_accept))
                        DocumentType.ACCEPTANCE_WEIGHT -> reasons.add(getString(R.string.another_weigh))
                        DocumentType.ACCEPTANCE_SIZE -> reasons.add(getString(R.string.another_measure))
                    }

                }
            }
            if (isPrinted && title.isEmpty() && type == DocumentType.ACCEPTANCE) {
                title = SpannableString("${getString(R.string.disabled)}: ${getString(R.string.text_code_client)}")
                reasons.add(getString(R.string.is_printed))
            }

            title.setSpan(RelativeSizeSpan(1.5f), 0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            title.setSpan(StyleSpan(Typeface.BOLD), 0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            title.setSpan(ForegroundColorSpan(Color.BLACK),0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)


            return if (title.isNotEmpty()) {
                with(spannableStringBuilder){
                    append(title)
                    append("\n\n")
                    append(reasonTitle)
                    append(": ")
                    append(reasons.joinToString("\n"))
                }
            } else {
                SpannableStringBuilder()
            }
        }
    }
}
