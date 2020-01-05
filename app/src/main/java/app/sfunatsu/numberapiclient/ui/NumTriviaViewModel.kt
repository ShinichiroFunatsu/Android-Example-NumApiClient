package app.sfunatsu.numberapiclient.ui

import androidx.lifecycle.*
import app.sfunatsu.numberapiclient.repository.GetNumTriviaResult
import app.sfunatsu.numberapiclient.repository.NumTriviaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

private sealed class Status {
    data class Idling(val result: GetNumTriviaResult<Exception>) : Status()
    object Loading : Status()
}

interface NumTriviaViewModel {
    companion object {
        @FlowPreview
        @ExperimentalCoroutinesApi
        fun create(repository: NumTriviaRepository) = NumTriviaViewModelImpl(repository)
    }

    val input: LiveData<String>
    val output: LiveData<String>
    val buttonEnable: LiveData<Boolean>
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
    override val buttonEnable = MutableLiveData<Boolean>()
    override val clearInputText = MutableLiveData<Unit>()

    private val numFlow: () -> Flow<Long?>
        get() = { flow { emit(input.value?.toLongOrNull()) } }

    private val fetchNumOfTriviaFlow: suspend (Long) -> Flow<GetNumTriviaResult<Exception>>
        get() = { num: Long -> flow { emit(repository.fetchNumOfTrivia(num)) } }

    private var status: Status by Delegates.observable(Status.Idling(GetNumTriviaResult.Empty))
    { _: KProperty<*>, _: Status, newValue: Status ->
        updateStatus(newValue)
    }

    private fun updateStatus(status: Status) {
        when (status) {
            is Status.Idling -> {
                val res = status.result
                res.onSuccess {
                    clearInputText.value = Unit
                    input.value = ""
                }
                buttonEnable.value = true
                output.value = res.toResultMsg()
            }
            is Status.Loading -> {
                buttonEnable.value = false
                output.value = "Loading..."
            }
        }
    }

    override fun onClick() {
        viewModelScope.launch {
            numFlow().filterNotNull()
                .flatMapConcat { num ->
                    fetchNumOfTriviaFlow(num)
                        .onStart { status = Status.Loading }
                        .onEach { status = Status.Idling(it) }
                }
                .launchIn(this)
        }
    }

    override fun onInputTextChanged(text: CharSequence) {
        input.value = text.toString()
    }

}

private fun GetNumTriviaResult<Exception>.toResultMsg() = when (this) {
    is GetNumTriviaResult.Success -> {
        this.trivia.text
    }
    is GetNumTriviaResult.Error<Exception> -> {
        this.e.message ?: "unknown error"
    }
    else -> throw IllegalStateException()
}

