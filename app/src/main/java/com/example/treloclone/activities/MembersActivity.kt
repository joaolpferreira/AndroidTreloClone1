package com.example.treloclone.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.treloclone.R
import com.example.treloclone.firebase.FireStoreClass
import com.example.treloclone.models.Board
import com.example.treloclone.models.User
import com.example.treloclone.utils.Constants
import com.projemanag.adapters.MemberListItemsAdapter

class MembersActivity : BaseActivity() {

    private var toolbarMembersActivity: Toolbar? = null
    private var rvMembersList: RecyclerView? = null

    private lateinit var mBoardDetails: Board


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_members)

        toolbarMembersActivity = findViewById(R.id.toolbar_members_activity)
        rvMembersList = findViewById(R.id.rv_members_list)

        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAIL)!!
            showProgressDialog(resources.getString(R.string.please_wait))
            FireStoreClass().getAssignedMembersListDetails(this, mBoardDetails.assignedTo)
        }

        setupActionBar()

    }

    fun setupMembersList(list: ArrayList<User>){
        hideProgressDialog()

        rvMembersList!!.layoutManager = LinearLayoutManager(this)
        rvMembersList!!.setHasFixedSize(true)

        val adapter = MemberListItemsAdapter(this,list)
        rvMembersList!!.adapter = adapter
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