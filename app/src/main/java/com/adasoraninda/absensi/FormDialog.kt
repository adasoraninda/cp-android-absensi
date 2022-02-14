package com.adasoraninda.absensi

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputLayout

class FormDialog(
    private val clickListener: ((di: DialogInterface, name: String?) -> Unit)? = null
) : DialogFragment() {

    private var inputName: TextInputLayout? = null
    private var name: String? = null

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun afterTextChanged(editable: Editable?) {
            name = editable?.toString()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = View.inflate(context, R.layout.layout_form_dialog, null)

        inputName = view.findViewById(R.id.input_name)

        inputName?.editText?.addTextChangedListener(textWatcher)

        return AlertDialog.Builder(context)
            .setView(view)
            .setTitle("Check In Form")
            .setPositiveButton("Submit") { di, _ ->
                clickListener?.invoke(di, name)
            }.create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        inputName?.editText?.removeTextChangedListener(textWatcher)
    }

    companion object {
        const val TAG = "FormDialog"
    }

}