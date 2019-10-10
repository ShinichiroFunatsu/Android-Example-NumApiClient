package app.sfunatsu.numberapiclient

import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.ViewModel
import app.sfunatsu.numberapiclient.databinding.ActivityMainBinding
import app.sfunatsu.numberapiclient.repository.GetNumTriviaResult
import app.sfunatsu.numberapiclient.repository.NumTriviaRepository
import app.sfunatsu.numberapiclient.shared.ScopedAppActivity


/**
 * http://numbersapi.com/#42
 */
class MainActivity : ScopedAppActivity() {

    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val repository: NumTriviaRepository by lazy { NumTriviaRepository() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with(binding) {
            val output = outputTextView
            val input = inputEditText
            val button = fetchButton
            button onClick {
                val num = input.toLong() ?: return@onClick
                output.text = "loading..."
                val res = repository.findNumOfTrivia(num = num)
                output.text = when (res) {
                    is GetNumTriviaResult.Success -> res.trivia.text
                    is GetNumTriviaResult.Error<Exception> -> res.e.message
                }
                input.text.clear()
            }
        }
    }
}

fun TextView.toLong(): Long? = runCatchingOrNull { text.toString().toLong() }

fun <T> runCatchingOrNull(f: () -> T): T? = try {
    f()
} catch (e: Exception) {
    null
}







