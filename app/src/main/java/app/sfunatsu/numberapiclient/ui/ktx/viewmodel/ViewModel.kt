package app.sfunatsu.numberapiclient.ui.ktx.viewmodel

import androidx.activity.ComponentActivity
import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider

@MainThread
inline fun <reified VM : ViewModel> ComponentActivity.viewModels(
    noinline viewModelProducer: (() -> VM)? = null
): Lazy<VM> {
    val factoryPromise = {
        if (viewModelProducer != null) {
            object : ViewModelProvider.AndroidViewModelFactory(this.application) {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return viewModelProducer() as T
                }
            }
        }
        else {
            val application = application ?: throw IllegalArgumentException(
                "ViewModel can be accessed only when Activity is attached"
            )
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        }
    }
    return ViewModelLazy(VM::class, { viewModelStore }, factoryPromise)
}