package app.sfunatsu.numberapiclient.ui

import androidx.lifecycle.*
import app.sfunatsu.numberapiclient.repository.GetNumTriviaResult
import app.sfunatsu.numberapiclient.repository.NumTriviaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

interface NumTriviaViewModel {
    companion object {
        @FlowPreview
        @ExperimentalCoroutinesApi
        fun create(repository: NumTriviaRepository) = NumTriviaViewModelImpl(repository)
    }

    val input: LiveData<String>
    val output: LiveData<String>
    val clearInputText: LiveData<Unit>
    fun onClick(): Unit
    fun onInputTextChanged(text: CharSequence)
}

@FlowPreview
@ExperimentalCoroutinesApi
class NumTriviaViewModelImpl(
    private val repository: NumTriviaRepository
) : ViewModel(),
    NumTriviaViewModel {

    override val input = MutableLiveData<String>()
    override val output = MutableLiveData<String>()
    override val clearInputText = MutableLiveData<Unit>()

    private val numFlow: () -> Flow<Long?>
        get() = { flow { emit(input.value?.toLongOrNull()) } }

    private val fetchNumOfTriviaFlow: suspend (Long) -> Flow<GetNumTriviaResult<Exception>>
        get() = { num: Long -> flow { emit(repository.fetchNumOfTrivia(num)) } }

    override fun onClick() {
        viewModelScope.launch {
            numFlow().filterNotNull()
                .flatMapConcat { num ->
                    fetchNumOfTriviaFlow(num)
                        .onStart { output.value = "Loading.." }
                }
                .onEach { it.onSuccess { resetStatus() } }
                .collect { output.value = it.toResultMsg() }
        }
    }

    private fun resetStatus() {
        clearInputText.value = Unit
        input.value = ""
    }

    private fun GetNumTriviaResult<Exception>.toResultMsg() = when (this) {
        is GetNumTriviaResult.Success -> {
            this.trivia.text
        }
        is GetNumTriviaResult.Error<Exception> -> {
            this.e.message ?: "unknown error"
        }
    }

    override fun onInputTextChanged(text: CharSequence) {
        input.value = text.toString()
    }
}
