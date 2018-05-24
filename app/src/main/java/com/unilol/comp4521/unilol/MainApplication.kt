/*
# COMP 4521 # IYER ARVIND   20218689    aiyer@connect.ust.hk
# COMP 4521 # RYAN BUDI     20214841    bryanaa@connect.ust.hk
# COMP 4521 # JI TONY WAI SUM 20217881  twsji@connect.ust.hk
*/
package com.unilol.comp4521.unilol

import android.app.Application
import android.content.Context
import ly.img.android.PESDK

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        PESDK.init(this, "android_license")
    }
}
