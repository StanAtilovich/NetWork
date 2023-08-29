package ru.netology.network.aplication

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.network.BuildConfig
import ru.netology.network.auth.AppAuth
import javax.inject.Inject


@HiltAndroidApp
class NeWorkApplication : Application() {
    private val appScope = CoroutineScope(Dispatchers.Default)

    @Inject
    lateinit var auth: AppAuth

    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey("6ef37456-21a4-413f-aeb3-c953026d0be4")
        MapKitFactory.initialize(this);
        setupAuth()
    }

    private fun setupAuth() {
        appScope.launch {
        }
    }
}