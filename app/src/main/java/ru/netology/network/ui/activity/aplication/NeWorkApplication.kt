package ru.netology.network.ui.activity.aplication

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.network.BuildConfig
import ru.netology.network.ui.activity.auth.AppAuth
import javax.inject.Inject


@HiltAndroidApp
class NeWorkApplication : Application() {
    private val appScope = CoroutineScope(Dispatchers.Default)

    @Inject
    lateinit var auth: AppAuth

    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey("${BuildConfig.API_KEY}")
        MapKitFactory.initialize(this);
        setupAuth()
    }

    private fun setupAuth() {
        appScope.launch {
        }
    }
}