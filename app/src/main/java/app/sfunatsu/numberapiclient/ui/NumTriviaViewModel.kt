package app.sfunatsu.numberapiclient.ui

import androidx.lifecycle.*
import app.sfunatsu.numberapiclient.repository.GetNumTriviaResult
import app.sfunatsu.numberapiclient.repository.NumTriviaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

interface NumTriviaViewModel {
    companion object {
        @ExperimentalCoroutinesApi
        fun create(repository: NumTriviaRepository) = NumTriviaViewModelImpl(repository)
    }

    val input: LiveData<String>
    val output: LiveData<String>
    val clearInputText: LiveData<Unit>
    fun onClick(): Unit
    fun onInputTextChanged(text: CharSequence)
}

@ExperimentalCoroutinesApi
class NumTriviaViewModelImpl(
    private val repository: NumTriviaRepository
) : ViewModel(),
    NumTriviaViewModel {

    override val input = MutableLiveData<String>()
    override val output = MutableLiveData<String>()
    override val clearInputText = MutableLiveData<Unit>()

    private val numTriviaFlow: Flow<GetNumTriviaResult<Exception>>
        get() = flowOf(input.value?.toLongOrNull()).filterNotNull()
            .map { repository.findNumOfTrivia(it) }
            .onStart { output.value = "Loading.." }
            .onEach { if (it is GetNumTriviaResult.Success) clearInputText.value = Unit }

    override fun onClick() {
        viewModelScope.launch {
            numTriviaFlow.collect { output.value = it.toResultMsg() }
        }
    }

    private fun GetNumTriviaResult<Exception>.toResultMsg() = when (this) {
        is GetNumTriviaResult.Success -> {
            this.trivia.text
        }
        is GetNumTriviaResult.Error<Exception> -> {
            this.e.message ?: "error"
        }
    }

    override fun onInputTextChanged(text: CharSequence) {
        input.value = text.toString()
    }
}
