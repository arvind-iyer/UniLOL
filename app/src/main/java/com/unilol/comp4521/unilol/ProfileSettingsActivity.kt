package com.unilol.comp4521.unilol

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
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

        // Toolbar and actionbar stuff --> places an actionbar with a back button
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
        actionbar?.title = "Settings"

        adapter = SettingsListAdapter(this, R.layout.settings_listitem, settingNames, { section: Int -> sectionClicked(section) })
        settingsListView.adapter = adapter
    }

    private fun sectionClicked(section : Int) {
        if (section == 3) {
            // correct?
            mAuth.signOut()
            "Signed out".toast(this, 2)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
        } else {
            val intent = Intent(this, EditProfileDataActivity::class.java)
            intent.putExtra("mode", section)
            startActivity(intent)
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // When user presses back button, go back to previous MainActivity
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
}
