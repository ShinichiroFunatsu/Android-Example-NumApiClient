package app.sfunatsu.numberapiclient.ui

import android.os.Bundle
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.*
import app.sfunatsu.numberapiclient.databinding.ActivityMainBinding
import app.sfunatsu.numberapiclient.repository.NumTriviaRepository
import app.sfunatsu.numberapiclient.shared.ScopedAppActivity
import app.sfunatsu.numberapiclient.ui.ktx.viewModels
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview


/**
 * http://numbersapi.com/#42
 */
@FlowPreview
@ExperimentalCoroutinesApi
class MainActivity : ScopedAppActivity() {

    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val repository: NumTriviaRepository by lazy { NumTriviaRepository() }
    private val viewModel: NumTriviaViewModel by viewModels { NumTriviaViewModel.create(repository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        subscribeUi()
    }

    private fun subscribeUi() {
        // observe click button and bind result to output
        binding.fetchButton.clicks { viewModel.onClick() }
        viewModel.output.observe(this, Observer {
            binding.outputTextView.text = it
        })

        // observe input
        val inputTextWatcher = binding.inputEditText.addTextChangedListener {
            viewModel.onInputTextChanged(it!!)
        }

        // observe clear
        viewModel.clearInputText.observe(this, Observer<Unit> {
            binding.inputEditText.clearWithoutNotify(inputTextWatcher)
        })
    }
}

fun View.clicks(f: () -> Unit) {
    setOnClickListener { f() }
}

fun EditText.clearWithoutNotify(textWatcher: TextWatcher) {
    removeTextChangedListener(textWatcher)
    text.clear()
    addTextChangedListener(textWatcher)
}







