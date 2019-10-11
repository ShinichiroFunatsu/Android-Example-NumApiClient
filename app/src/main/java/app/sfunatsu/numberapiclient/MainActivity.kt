package app.sfunatsu.numberapiclient

import android.os.Bundle
import android.widget.TextView
import app.sfunatsu.numberapiclient.databinding.ActivityMainBinding
import app.sfunatsu.numberapiclient.repository.GetNumTriviaResult
import app.sfunatsu.numberapiclient.repository.NumTriviaRepository
import app.sfunatsu.numberapiclient.shared.*
import kotlinx.coroutines.delay


private sealed class Msg : IMsg {
    data class GotInputNum(val num: Long) : Msg()
    data class GotNumTrivia(val result: GetNumTriviaResult<Exception>) : Msg()
}


private sealed class Model : IModel {
    object Initialized : Model()
    object Loading : Model()
    data class Success(val msg: String) : Model()
    data class Error(val e: Exception) : Model()
}



class MainActivity : TeaActivity() {

    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val repository: NumTriviaRepository by lazy { NumTriviaRepository() }
    private val output by lazy { binding.outputTextView }
    private val input by lazy { binding.inputEditText }
    private val button by lazy { binding.fetchButton }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        element(
            init = Model.Initialized,
            update = update,
            subscriptions = arrayOf(click),
            view = view
        )
    }


    @Suppress("MoveLambdaOutsideParentheses")
    private val click = {
        button.onClick({ model ->
            if (model !is Model.Loading) {
                input.toLong()?.let { Msg.GotInputNum(it) } ?: EmptyMsg
            } else {
                EmptyMsg
            }
        })
    }

    private val view: (IModel) -> Unit = { model ->
        when (model) {
            is Model.Initialized ->
                output.text = ""
            is Model.Loading ->
                output.text = "Loading"
            is Model.Success ->
                output.text = model.msg
            is Model.Error ->
                output.text = model.e.message ?: "error message null"
        }
    }

    private val update: suspend (IModel, IMsg) -> Pair<IModel, Cmd> =
        { model, msg ->
            when (msg) {
                is Msg.GotInputNum ->
                    Model.Loading to getNumTrivia(msg)

                is Msg.GotNumTrivia ->
                    when (msg.result) {
                        is GetNumTriviaResult.Success ->
                            Model.Success(msg.result.trivia.text) to CmdNone
                        is GetNumTriviaResult.Error<Exception> ->
                            Model.Error(msg.result.e) to CmdNone
                    }

                else ->
                    throw IllegalArgumentException()
            }
        }

    private val getNumTrivia: (Msg.GotInputNum) -> Cmd =
        { input ->
            {
                delay(1000L)
                Msg.GotNumTrivia(repository.findNumOfTrivia(input.num))
            }
        }

}


fun TextView.toLong(): Long? = runCatchingOrNull { text.toString().toLong() }

fun <T> runCatchingOrNull(f: () -> T): T? = try {
    f()
} catch (e: Exception) {
    null
}