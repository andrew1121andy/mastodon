package io.keiji.sample.mastodonclient

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope

data class UserCredential(
    val instanceUrl: String,
    var username: String? = null,
    var acccessToken: String? = null
)

