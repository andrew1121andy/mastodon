package io.keiji.sample.mastodonclient.entity

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope

class TootDetailViewModelFactory(
    private val toot: Toot?,
    private val coroutineScope: CoroutineScope,
    private val context: Context
) : ViewModelProvider.Factory {
     override fun <T : ViewModel?> create(modelClass: Class<T>): T {
         return TootDetailViewModelFactory(
             toot,
             coroutineScope,
             context.applicationContext as Application
         ) as T
     }
}