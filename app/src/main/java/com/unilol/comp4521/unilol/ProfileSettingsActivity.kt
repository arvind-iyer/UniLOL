package com.unilol.comp4521.unilol

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.unilol.comp4521.unilol.interfaces.SettingsListAdapter
import kotlinx.android.synthetic.main.activity_profile_settings.*

class ProfileSettingsActivity : AppCompatActivity() {

    private val tag = "ProfileSettingsActivity"
    private val mAuth = FirebaseAuth.getInstance()
    private lateinit var adapter: SettingsListAdapter
    private val settingNames: Array<String> = arrayOf(
        "Edit Name",
        "Edit School",
        "Update Current Status",
        "Sign Out"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)

        adapter = SettingsListAdapter(this, R.layout.settings_listitem, settingNames, { section: Int -> sectionClicked(section) })
        settingsListView.adapter = adapter

    }

    private fun sectionClicked(section : Int) {
        if (section == 3) {
            // correct?
            mAuth.signOut()
            "Signed out".toast(this, 2)
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
            finish()
        } else {
            val intent = Intent(this, EditProfileDataActivity::class.java)
            intent.putExtra("mode", section)
            startActivity(intent)
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
        }
    }
}
