package com.unilol.comp4521.unilol

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.unilol.comp4521.unilol.interfaces.RelationListAdapter
import com.unilol.comp4521.unilol.interfaces.SettingsListAdapter
import com.unilol.comp4521.unilol.interfaces.User
import kotlinx.android.synthetic.main.activity_profile_settings.*

class ProfileRelationActivity : AppCompatActivity() {

    private lateinit var adapter: RelationListAdapter
    private var mode = -1
    private lateinit var users: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)

        mode = intent.extras?.getInt("@string/mode") ?: -1
        adapter = RelationListAdapter(this, R.layout.viewusers_listitem, ArrayList(), { section: Int -> sectionClicked(section) })

        // Toolbar and actionbar stuff --> places an actionbar with a back button
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        settingsListView.adapter = adapter
        actionbar?.title = if (mode == RELATION_FOLLOWERS) "Following" else "Followers"

        val userId = intent.extras?.getString("@string/userId") ?: ""
        val db = FirebaseFirestore.getInstance()
        val selfProfile = db.collection("users").document(userId)

        selfProfile.get().addOnCompleteListener() { task ->
            if (task.isSuccessful) {
                val self = task.result.toObject(User::class.java)!!
                users = if (mode == RELATION_FOLLOWERS) self.followers else self.following
                adapter = RelationListAdapter(this, R.layout.viewusers_listitem, users, { section: Int -> sectionClicked(section) })
                settingsListView.adapter = adapter
            }
        }
    }

    private fun sectionClicked(section : Int) {
        val userId = users[section]
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("@string/user_id", userId)
        startActivity(intent)
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
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

    companion object {
        const val RELATION_FOLLOWING = 0
        const val RELATION_FOLLOWERS = 1
    }
}
