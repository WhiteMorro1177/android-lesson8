package ru.mirea.tsybulko.lesson8

import android.app.Application
import com.yandex.mapkit.MapKitFactory

class App: Application() {
    companion object {
        const val MAP_API_KEY = "a4dc574b-5149-4ddf-9308-78c37250d80f"
    }

    override fun onCreate() {
        super.onCreate()

        MapKitFactory.setApiKey(MAP_API_KEY)
    }
}