package app.sfunatsu.numberapiclient.shared

import androidx.lifecycle.*
import kotlinx.coroutines.launch

abstract class TeaViewModel : ViewModel() {
    private val msg = MutableLiveData<IMsg>()
    private val model = MutableLiveData<IModel>()

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

    fun LiveData<Unit>.onClick(f: () -> IMsg): LiveData<IMsg> =
        MediatorLiveData<IMsg>().apply {
            addSource(this@onClick) { value = f() }
        }

    fun LiveData<CharSequence>.onInput(f: (CharSequence) -> IMsg): LiveData<IMsg> =
        MediatorLiveData<IMsg>().apply {
            addSource(this@onInput) { value = f(this@onInput.value!!) }
        }

    private fun subscriptions(vararg inputs: LiveData<IMsg>) =
        inputs.forEach { it.observeForever { msg.value = it } }

    private fun update(
        handleMsg: suspend (IModel, IMsg) -> Pair<IModel, Cmd>
    ) = msg.observeForever {
        viewModelScope.launch {
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