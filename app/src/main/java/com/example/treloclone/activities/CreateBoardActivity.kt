package com.example.treloclone.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.treloclone.R
import com.example.treloclone.firebase.FireStoreClass
import com.example.treloclone.models.Board
import com.example.treloclone.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException

class CreateBoardActivity : BaseActivity() {

    private var toolbarCreateBoardActivity: Toolbar? = null
    private var etBoardName: AppCompatEditText? = null
    private var btnCreateBoard: Button? = null


    private var mSelectedImageFileUri: Uri? = null
    private var ciView: CircleImageView? = null

    private var mBoardImageURL: String = ""
    private lateinit var mUsername: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_board)

        toolbarCreateBoardActivity = findViewById(R.id.toolbar_create_board_activity)
        ciView = findViewById(R.id.iv_board_image)
        etBoardName = findViewById(R.id.et_board_name)
        btnCreateBoard= findViewById(R.id.btn_create)

        setupActionBar()

        if(intent.hasExtra(Constants.NAME)){
            mUsername = intent.getStringExtra(Constants.NAME).toString()
        }

        ciView?.setOnClickListener{
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                Constants.showImageChoser(this)

            }else{
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        btnCreateBoard?.setOnClickListener {
            if(mSelectedImageFileUri != null){
                uploadBoardImage()
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))
                createBoard()
            }
        }
    }

    private fun validateForm(boardName: String) : Boolean{
        return when{
            TextUtils.isEmpty(boardName)->{
                showErrorSnackBar("Please enter a board name")
                false
            }
            else ->{ true }
        }
    }

    private fun createBoard(){
        if(validateForm(etBoardName?.text.toString())){
            val assignedUserArrayList: ArrayList<String> = ArrayList()
            assignedUserArrayList.add(getCurrentUserId())

            var board = Board(
                etBoardName?.text.toString(),
                mBoardImageURL,
                mUsername,
                assignedUserArrayList
            )

            FireStoreClass().createBoard(this, board)
        }else{
            hideProgressDialog()
        }
    }

    private fun uploadBoardImage(){
        showProgressDialog(resources.getString(R.string.please_wait))

        val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
            "BOARD_IMAGE" + System.currentTimeMillis() + Constants.getFileExtension(this, mSelectedImageFileUri))

        sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot ->
            Log.e("Board Image Url",
                taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
            )
            taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri ->
                Log.e("Downloadable Image URL", uri.toString())
                mBoardImageURL = uri.toString()
                createBoard()
                hideProgressDialog()

            }

        }.addOnFailureListener{
                exception ->
            Toast.makeText(this,exception.message,Toast.LENGTH_SHORT).show()
            hideProgressDialog()
        }
    }

    fun boardCreatedSuccessfully(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun setupActionBar(){
        setSupportActionBar(toolbarCreateBoardActivity)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title =resources.getString(R.string.create_board_title)
        }
        toolbarCreateBoardActivity?.setNavigationOnClickListener{ onBackPressed() }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == Constants.READ_STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Constants.showImageChoser(this)
            }
        }else{
            Toast.makeText(this, "Oops, you just denied the permission for storage.", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == Constants.PICK_IMAGE_REQUEST_CODE && data!!.data != null){
            mSelectedImageFileUri = data.data

            try{
                Glide
                    .with(this@CreateBoardActivity)
                    .load(mSelectedImageFileUri) // URL of the image
                    .centerCrop() // Scale type of the image.
                    .placeholder(R.drawable.ic_board_place_holder) // A default place holder
                    .into(ciView!!) // the view in which the image will be loaded.
            }catch (e: IOException){
                e.printStackTrace()
            }

        }

    }
}