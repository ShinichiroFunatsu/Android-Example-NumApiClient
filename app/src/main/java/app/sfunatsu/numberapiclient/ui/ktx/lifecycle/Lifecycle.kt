package app.sfunatsu.numberapiclient.ui.ktx.lifecycle

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

fun TextView.bindText(liveData: LiveData<String>) {
    liveData.observe(this.context as AppCompatActivity, Observer { text = it })
}

fun EditText.bindClear(liveData: LiveData<*>, textWatcher: TextWatcher? = null) {
    liveData.observe(this.context as AppCompatActivity,
        if (textWatcher == null) {
            Observer { text.clear() }
        } else {
            Observer { clearWithoutNotify(textWatcher) }
        }
    )
}

fun View.clicks(f: () -> Unit) {
    setOnClickListener { f() }
}

fun EditText.textChanges(f: (Editable)-> Unit): TextWatcher {
    return addTextChangedListener {
        if (it.isNullOrEmpty()) return@addTextChangedListener
        f(it)
    }
}

fun EditText.clearWithoutNotify(textWatcher: TextWatcher) {
    removeTextChangedListener(textWatcher)
    text.clear()
    addTextChangedListener(textWatcher)
}