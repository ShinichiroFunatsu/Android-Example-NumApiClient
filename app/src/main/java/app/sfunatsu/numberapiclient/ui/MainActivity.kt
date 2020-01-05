package app.sfunatsu.numberapiclient.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import app.sfunatsu.numberapiclient.databinding.ActivityMainBinding
import app.sfunatsu.numberapiclient.repository.NumTriviaRepository
import app.sfunatsu.numberapiclient.shared.*
import app.sfunatsu.numberapiclient.ui.ktx.viewmodel.viewModels


class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val repository: NumTriviaRepository by lazy { NumTriviaRepository() }
    private val viewModel: NumTriviaViewModel by viewModels { NumTriviaViewModel.create(repository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.fetchButton.setOnClickListener { viewModel.button.value = Unit }
        binding.inputEditText.addTextChangedListener { viewModel.input.value = it }
        viewModel.outputLiveData.observe(this, Observer {
            binding.outputTextView.text = it
        })
    }

}