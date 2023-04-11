package com.example.treloclone.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.example.treloclone.R
import com.example.treloclone.models.Board
import com.example.treloclone.utils.Constants

class MembersActivity : BaseActivity() {

    private var toolbarMembersActivity: Toolbar? = null
    private lateinit var mBoardDetails: Board


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_members)

        toolbarMembersActivity = findViewById(R.id.toolbar_members_activity)

        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAIL)!!
        }

        setupActionBar()

    }


    /**
     * A function to setup action bar
     */
    private fun setupActionBar() {
        setSupportActionBar(toolbarMembersActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbarMembersActivity?.setNavigationOnClickListener { onBackPressed() }
    }
}