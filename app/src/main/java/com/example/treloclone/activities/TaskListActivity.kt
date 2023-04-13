package com.example.treloclone.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.treloclone.R
import com.example.treloclone.adapters.TaskListItemsAdapter
import com.example.treloclone.firebase.FireStoreClass
import com.example.treloclone.models.Board
import com.example.treloclone.models.Card
import com.example.treloclone.models.Task
import com.example.treloclone.utils.Constants

class TaskListActivity : BaseActivity() {
    private var toolbarTaskListActivity: Toolbar? = null
    private var rvTaskList: RecyclerView? = null

    private lateinit var mBoardDetails: Board
    private lateinit var mBoardDocumentId: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        toolbarTaskListActivity = findViewById(R.id.toolbar_task_list_activity)
        rvTaskList = findViewById(R.id.rv_task_list)

        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            mBoardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)!!
        }

        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().getBoardDetails(this@TaskListActivity, mBoardDocumentId)
    }

    //reload screen everytime, better for user experience, more requests on database
//    override fun onResume() {
//        super.onResume()
//        showProgressDialog(resources.getString(R.string.please_wait))
//        FireStoreClass().getBoardDetails(this@TaskListActivity, mBoardDocumentId)
//    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == MEMBER_REQUEST_CODE){
            showProgressDialog(resources.getString(R.string.please_wait))
            FireStoreClass().getBoardDetails(this@TaskListActivity, mBoardDocumentId)
        }else{
            Log.e("Cancelled","Cancelled")
        }
    }
    /**
     * A function to setup action bar
     */
    private fun setupActionBar() {

        setSupportActionBar(toolbarTaskListActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = mBoardDetails.name
        }

        toolbarTaskListActivity?.setNavigationOnClickListener { onBackPressed() }
    }

    fun cardDetails(taskListPosition: Int, cardPosition: Int){
        startActivity(Intent(this, CardDetailsActivity::class.java))
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu to use in the action bar
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            R.id.action_members -> {


                val intent = Intent(this@TaskListActivity, MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
                startActivityForResult(intent, MEMBER_REQUEST_CODE)

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    /**
     * A function to get the result of Board Detail.
     */
    fun boardDetails(board: Board) {

        // TODO (Step 3: Initialize and Assign the value to the global variable for Board Details.
        //  After replace the parameter variable with global so from onwards the global variable will be used.)
        // START
        mBoardDetails = board
        // END

        hideProgressDialog()

        // TODO (Step 4: Remove the parameter and add the title from global variable in the setupActionBar function.)
        // START
        // Call the function to setup action bar.
        setupActionBar()
        // END

        // Here we are appending an item view for adding a list task list for the board.
        val addTaskList = Task(resources.getString(R.string.add_list))
        mBoardDetails.taskList.add(addTaskList)

        rvTaskList!!.layoutManager =
            LinearLayoutManager(this@TaskListActivity, LinearLayoutManager.HORIZONTAL, false)
        rvTaskList!!.setHasFixedSize(true)

        // Create an instance of TaskListItemsAdapter and pass the task list to it.
        val adapter = TaskListItemsAdapter(this@TaskListActivity, mBoardDetails.taskList)
        rvTaskList!!.adapter = adapter // Attach the adapter to the recyclerView.
    }

    // TODO (Step 10: Create a function to get the task list name from the adapter class which we will be using to create a new task list in the database.)
    // START
    /**
     * A function to get the task list name from the adapter class which we will be using to create a new task list in the database.
     */
    fun createTaskList(taskListName: String) {

        Log.e("Task List Name", taskListName)

        // Create and Assign the task details
        val task = Task(taskListName, FireStoreClass().getCurrentUserId())

        mBoardDetails.taskList.add(0, task) // Add task to the first position of ArrayList
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1) // Remove the last position as we have added the item manually for adding the TaskList.

        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))

        FireStoreClass().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }

    fun updateTaskList(position: Int, listName: String, model: Task) {

        val task = Task(listName, model.createdBy)

        mBoardDetails.taskList[position] = task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }

    /**
     * A function to delete the task list from database.
     */
    fun deleteTaskList(position: Int) {

        mBoardDetails.taskList.removeAt(position)

        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }
    fun addUpdateTaskListSuccess() {
        hideProgressDialog()

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().getBoardDetails(this@TaskListActivity, mBoardDetails.documentId)
    }

    fun addCardToTaskList(position: Int, cardName: String) {
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        val cardAssignedUsersList: ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(FireStoreClass().getCurrentUserId())

        val card = Card(cardName, FireStoreClass().getCurrentUserId(), cardAssignedUsersList)

        val cardsList = mBoardDetails.taskList[position].cards
        cardsList.add(card)

        val task = Task(
            mBoardDetails.taskList[position].title,
            mBoardDetails.taskList[position].createdBy,
            cardsList
        )
        mBoardDetails.taskList[position] = task

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }

    companion object{
        const val MEMBER_REQUEST_CODE: Int = 13
    }
}