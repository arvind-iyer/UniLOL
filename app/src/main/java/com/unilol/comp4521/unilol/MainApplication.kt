package com.unilol.comp4521.unilol

import android.app.Application
import ly.img.android.PESDK

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        PESDK.init(this, "android_license_unilol")
    }
}
