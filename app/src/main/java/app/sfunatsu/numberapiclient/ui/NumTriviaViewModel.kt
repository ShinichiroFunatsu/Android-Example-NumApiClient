package app.sfunatsu.numberapiclient.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import app.sfunatsu.numberapiclient.repository.GetNumTriviaResult
import app.sfunatsu.numberapiclient.repository.NumTriviaRepository

interface NumTriviaViewModel {
    val input: LiveData<String>
    val clearInputText: LiveData<Unit>
    fun onClick(): LiveData<String>
    fun onInputTextChanged(text: CharSequence)
}

class NumTriviaViewModelImpl(
    private val repository: NumTriviaRepository
) : ViewModel(),
    NumTriviaViewModel {

    override val input = MutableLiveData<String>()
    override val clearInputText = MutableLiveData<Unit>()

    override fun onClick() = liveData {
        val num = input.value?.toLongOrNull()
            ?: return@liveData
        emit("loading...")
        clearInputText.value = Unit
        val msg = repository.findNumOfTrivia(num = num).let { res ->
            when (res) {
                is GetNumTriviaResult.Success -> res.trivia.text
                is GetNumTriviaResult.Error<Exception> -> res.e.message ?: "error"
            }
        }
        emit(msg)
    }

    override fun onInputTextChanged(text: CharSequence) {
        input.value = text.toString()
    }
}
