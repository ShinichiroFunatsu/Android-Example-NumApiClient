@file:Suppress("EXPERIMENTAL_API_USAGE")

package app.sfunatsu.numberapiclient.shared

import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.actor

abstract class ScopedAppActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    override fun onDestroy() {
        super.onDestroy()
        cancel() // CoroutineScope.cancel
    }

    infix fun Button.onClick(action: suspend (View) -> Unit) {
        val eventActor = actor<View>(this@ScopedAppActivity.coroutineContext) {
            for (event in channel) action(event)
        }
        setOnClickListener {
            eventActor.offer(it)
        }
    }
}