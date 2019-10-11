@file:Suppress("EXPERIMENTAL_API_USAGE")

package app.sfunatsu.numberapiclient.shared

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*


interface IMsg
interface IModel

object EmptyMsg : IMsg
typealias Cmd = suspend () -> IMsg
val CmdNone: Cmd = { EmptyMsg }


abstract class TeaActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val msg = MutableLiveData<IMsg>()
    private val model = MutableLiveData<IModel>()

    override fun onDestroy() {
        super.onDestroy()
        cancel() // CoroutineScope.cancel
    }

    fun element(
        init: IModel,
        update: suspend (IModel, IMsg) -> Pair<IModel, Cmd>,
        subscriptions: Array<() -> LiveData<IMsg>>,
        view: (IModel) -> Unit
    ) {
        model.value = init
        update(update)
        subscriptions(*subscriptions.map { it() }.toTypedArray())
        viewUpdate(view)
    }

    fun View.onClick(f: (IModel) -> IMsg): LiveData<IMsg> {
        val clickMsg = MutableLiveData<IMsg>()
        setOnClickListener { clickMsg.value = f(model.value!!) }
        return clickMsg
    }

    private fun subscriptions(vararg inputs: LiveData<IMsg>) =
        inputs.forEach { it.observeForever { msg.value = it } }

    private fun update(
        handleMsg: suspend (IModel, IMsg) -> Pair<IModel, Cmd>
    ) = msg.observeForever {
        launch {
            if (it !is EmptyMsg) {
                val (m, cmd) = handleMsg(model.value!!, it)
                model.value = m
                msg.value = cmd()
            }
        }
    }

    private fun viewUpdate(handleModel: (IModel) -> Unit) = model.observeForever {
        handleModel(it)
    }
}