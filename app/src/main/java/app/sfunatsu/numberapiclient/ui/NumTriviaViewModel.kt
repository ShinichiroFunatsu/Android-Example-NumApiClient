package app.sfunatsu.numberapiclient.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.sfunatsu.numberapiclient.repository.GetNumTriviaResult
import app.sfunatsu.numberapiclient.repository.NumTriviaRepository
import app.sfunatsu.numberapiclient.shared.*
import kotlinx.coroutines.delay


private sealed class NumTriviaMsg : IMsg {
    data class GotInputText(val text: CharSequence) : NumTriviaMsg()
    data class GotInputNum(val num: Long?) : NumTriviaMsg()
    data class GotNumTrivia(val result: GetNumTriviaResult<Exception>) : NumTriviaMsg()
}

private sealed class NumTriviaModel : IModel {
    object Initialized : NumTriviaModel()
    data class UpdateText(val text: CharSequence) : NumTriviaModel()
    object Loading : NumTriviaModel()
    data class Success(val msg: String) : NumTriviaModel()
    data class Error(val e: Exception) : NumTriviaModel()
}

interface NumTriviaViewModel {
    companion object {
        fun create(repository: NumTriviaRepository) = NumTriviaViewModelImpl(repository)
    }
    val outputLiveData: LiveData<String>
    val buttonEnableLiveData: LiveData<Boolean>
    val input: MutableLiveData<CharSequence>
    val button: MutableLiveData<Unit>
}

class NumTriviaViewModelImpl(
    private val repository: NumTriviaRepository
): TeaViewModel(), NumTriviaViewModel {

    override val outputLiveData = MutableLiveData<String>()
    override val buttonEnableLiveData = MutableLiveData<Boolean>(true)
    override val button = MutableLiveData<Unit>()
    override val input = MutableLiveData<CharSequence>()

    private val view: (IModel) -> Unit = { model ->
        when (model) {
            is NumTriviaModel.Initialized -> {
                outputLiveData.value = ""
                buttonEnableLiveData.value = true
            }
            is NumTriviaModel.Loading -> {
                outputLiveData.value = "Loading"
                buttonEnableLiveData.value = false
            }
            is NumTriviaModel.Success -> {
                outputLiveData.value = model.msg
                buttonEnableLiveData.value = true
            }
            is NumTriviaModel.Error -> {
                outputLiveData.value = model.e.message ?: "error message null"
                buttonEnableLiveData.value = true
            }
        }
    }

    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    private val update: suspend (IModel, IMsg) -> Pair<IModel, Cmd> =
        { model, msg ->
            when (msg) {
                is NumTriviaMsg.GotInputText -> {
                    NumTriviaModel.UpdateText(msg.text) to CmdNone
                }
                is NumTriviaMsg.GotInputNum ->
                    if (msg.num == null || model is NumTriviaModel.Loading) {
                        model to CmdNone
                    } else {
                        NumTriviaModel.Loading to getNumTrivia(msg)
                    }

                is NumTriviaMsg.GotNumTrivia ->
                    when (msg.result) {
                        is GetNumTriviaResult.Success ->
                            NumTriviaModel.Success(
                                msg.result.trivia.text
                            ) to CmdNone
                        is GetNumTriviaResult.Error<Exception> ->
                            NumTriviaModel.Error(
                                msg.result.e
                            ) to CmdNone
                    }

                else ->
                    throw IllegalArgumentException()
            }
        }

    private val getNumTrivia: (NumTriviaMsg.GotInputNum) -> Cmd =
        { input ->
            {
                delay(1000L)
                NumTriviaMsg.GotNumTrivia(
                    repository.findNumOfTrivia(input.num!!)
                )
            }
        }

    private val getInputNum = {
        button.onClick {
            // FIXME remove input, and add submit to get long value form model.
            NumTriviaMsg.GotInputNum(input.value!!.toString().toLongOrNull())
        }
    }

    private val getInputText = {
        input.onInput {
            NumTriviaMsg.GotInputText(it)
        }
    }

    init {
        element(
            init = NumTriviaModel.Initialized,
            update = update,
            subscriptions = arrayOf(getInputNum, getInputText),
            view = view
        )
    }

}