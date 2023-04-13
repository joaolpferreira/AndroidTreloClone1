package com.example.treloclone.activities

import android.app.Activity
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
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
    private lateinit var mAssignedMembersList: ArrayList<User>

    private var anyChangesDone: Boolean = false


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
        mAssignedMembersList = list
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu to use in the action bar
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            R.id.action_add_member -> {
                dialogSearchMember()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun dialogSearchMember() {
        val dialog = Dialog(this)
        /*Set the screen content from a layout resource.
    The resource will be inflated, adding all top-level views to the screen.*/
        dialog.setContentView(R.layout.dialog_search_member)
        dialog.findViewById<TextView>(R.id.tv_add).setOnClickListener(View.OnClickListener {
            val email = dialog.findViewById<TextView>(R.id.et_email_search_member).text.toString()

            if (email.isNotEmpty()) {
                dialog.dismiss()

                // Show the progress dialog.
                showProgressDialog(resources.getString(R.string.please_wait))
                FireStoreClass().getMemberDetails(this@MembersActivity, email)
            } else {
                showErrorSnackBar("Please enter members email address.")
            }
        })
        dialog.findViewById<TextView>(R.id.tv_cancel).setOnClickListener(View.OnClickListener {
            dialog.dismiss()
        })
        //Start the dialog and display it on screen.
        dialog.show()
    }

    fun memberDetails(user: User) {
        mBoardDetails.assignedTo.add(user.id)
        FireStoreClass().assignMemberToBoard(this@MembersActivity, mBoardDetails, user)
    }

    fun memberAssignSuccess(user: User) {
        hideProgressDialog()
        mAssignedMembersList.add(user)
        anyChangesDone = true
        setupMembersList(mAssignedMembersList)
    }

    override fun onBackPressed() {
        if(anyChangesDone){
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }
}