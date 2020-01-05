package app.sfunatsu.numberapiclient.ui

import android.os.Bundle
import app.sfunatsu.numberapiclient.databinding.ActivityMainBinding
import app.sfunatsu.numberapiclient.repository.NumTriviaRepository
import app.sfunatsu.numberapiclient.shared.ScopedAppActivity
import app.sfunatsu.numberapiclient.ui.ktx.lifecycle.bindClear
import app.sfunatsu.numberapiclient.ui.ktx.lifecycle.bindText
import app.sfunatsu.numberapiclient.ui.ktx.lifecycle.clicks
import app.sfunatsu.numberapiclient.ui.ktx.lifecycle.textChanges
import app.sfunatsu.numberapiclient.ui.ktx.viewmodel.viewModels
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
        binding.fetchButton.clicks { viewModel.onClick() }
        binding.outputTextView.bindText(viewModel.output)
        val textWatcher = binding.inputEditText.textChanges { viewModel.onInputTextChanged(it) }
        binding.inputEditText.bindClear(viewModel.clearInputText, textWatcher)
    }
}









