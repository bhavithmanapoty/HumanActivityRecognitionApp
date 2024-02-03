package com.specknet.pdiotapp.utils

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.autofill.AutofillValue
import android.widget.EditText
import androidx.annotation.RequiresApi

class DateEditText : androidx.appcompat.widget.AppCompatEditText {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun getAutofillType(): Int {
        // Return AUTOFILL_TYPE_DATE to indicate that this view supports date autofill.
        return AUTOFILL_TYPE_DATE
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getAutofillValue(): AutofillValue {
        // Return an AutofillValue object representing the date in milliseconds.
        return AutofillValue.forDate(System.currentTimeMillis())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun autofill(value: AutofillValue) {
        // Handle the AutofillValue parameter, which is of type AUTOFILL_TYPE_DATE.
        // Convert the value to a proper string representation, such as mm/yyyy.
        // Use the string representation to set the text property of your view.
        val dateMillis = value.dateValue
        val dateString = formatDate(dateMillis)
        setText(dateString)
    }

    private fun formatDate(dateMillis: Long): String {
        // Implement your logic to format the date as desired (e.g., mm/yyyy).
        // For simplicity, this example uses a basic date format.
        val dateFormat = java.text.SimpleDateFormat("MM/yyyy")
        return dateFormat.format(dateMillis)
    }
}
