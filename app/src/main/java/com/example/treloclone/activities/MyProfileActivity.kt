package com.example.treloclone.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.treloclone.R
import com.example.treloclone.firebase.FireStoreClass
import com.example.treloclone.models.User
import com.example.treloclone.utils.Constants
import com.google.android.material.navigation.NavigationView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.handleCoroutineException
import java.io.IOException

class MyProfileActivity : BaseActivity() {

    private var toolbarMyProfileActivity: Toolbar? = null
    private var ciView: CircleImageView? = null
    private var etName: AppCompatEditText? = null
    private var etEmail: AppCompatEditText? = null
    private var etMobile: AppCompatEditText? = null
    private var btnUpdate: Button? = null


    private var mSelectedImageFileUri: Uri? = null
    private var mProfileImageURL: String = ""
    private lateinit var mUserDetails: User


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        toolbarMyProfileActivity = findViewById(R.id.toolbar_my_profile_activity)
        ciView = findViewById(R.id.iv_profile_user_image)
        etName = findViewById(R.id.et_name)
        etEmail = findViewById(R.id.et_email)
        etMobile = findViewById(R.id.et_mobile)
        btnUpdate = findViewById(R.id.btn_update)

        setupActionBar()

        FireStoreClass().loadUserData(this@MyProfileActivity)

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

        btnUpdate?.setOnClickListener{
            if(mSelectedImageFileUri != null){
                uploadUserImage()
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()

            }
        }
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
                    .with(this@MyProfileActivity)
                    .load(mSelectedImageFileUri) // URL of the image
                    .centerCrop() // Scale type of the image.
                    .placeholder(R.drawable.ic_user_place_holder) // A default place holder
                    .into(ciView!!) // the view in which the image will be loaded.
            }catch (e: IOException){
                e.printStackTrace()
            }

        }

    }

    private fun setupActionBar(){
        setSupportActionBar(toolbarMyProfileActivity)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title =resources.getString(R.string.my_profile)
        }

        toolbarMyProfileActivity?.setNavigationOnClickListener{ onBackPressed() }
    }

    fun setUserDataInUI(user: User){
        mUserDetails = user
        Glide
            .with(this@MyProfileActivity)
            .load(user.image) // URL of the image
            .centerCrop() // Scale type of the image.
            .placeholder(R.drawable.ic_user_place_holder) // A default place holder
            .into(ciView!!) // the view in which the image will be loaded.


        etName?.setText(user.name)
        etEmail?.setText(user.email)
        if(user.mobile != 0L){
            etMobile?.setText(user.mobile.toString())
        }
    }

    private fun updateUserProfileData(){
        val userHashMap = HashMap<String, Any>()

        if(mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image){
            userHashMap[Constants.IMAGE] = mProfileImageURL
        }

        if(etName?.text.toString() != mUserDetails.name){
            userHashMap[Constants.NAME] = etName?.text.toString()
        }

        Toast.makeText(this, "Mobiles: ${etMobile?.text.toString()} and ${mUserDetails.mobile.toString()}", Toast.LENGTH_LONG).show()
        if(etMobile?.text.toString() != mUserDetails.mobile.toString()){
            if(etMobile?.text.toString() != ""){
                userHashMap[Constants.MOBILE] = etMobile?.text.toString().toLong()
            }else{
                userHashMap[Constants.MOBILE] = 0
            }

        }
        FireStoreClass().updateUserProfileData(this, userHashMap)

    }

    private fun uploadUserImage(){
        showProgressDialog(resources.getString(R.string.please_wait))
        if(mSelectedImageFileUri != null){
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE" + System.currentTimeMillis() + Constants.getFileExtension(this, mSelectedImageFileUri))

            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot ->
                Log.e("Firebase Image Url",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri ->
                    Log.e("Downloadable Image URL", uri.toString())
                    mProfileImageURL = uri.toString()
                    updateUserProfileData()
                    hideProgressDialog()

                }

            }.addOnFailureListener{
                exception ->
                Toast.makeText(this@MyProfileActivity,exception.message,Toast.LENGTH_SHORT).show()
                hideProgressDialog()
            }
        }
    }

    fun profileUpdateSuccess(){
        hideProgressDialog()

        setResult(Activity.RESULT_OK)
        finish()

    }
}