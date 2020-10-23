package com.example.k1story

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.k1story.models.Post
import com.example.k1story.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_create.*
import kotlinx.android.synthetic.main.activity_login.*

private const val TAG="CreateActivity"
private const val PICK_PHOTO_CODE= 1234
class CreateActivity : AppCompatActivity() {

    private var photoUri: Uri? = null
    private var signedInUser: User? = null
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var storageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        storageReference = FirebaseStorage.getInstance().reference
        firestoreDb = FirebaseFirestore.getInstance()
        firestoreDb.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid as String)
            .get()
            .addOnSuccessListener { userSnapshot ->
                signedInUser= userSnapshot.toObject(User::class.java)
                Log.i(TAG,"Signed in user: $signedInUser")
            }
            .addOnFailureListener { exception ->
                Log.i(TAG,"Failure fetching signed in User",exception)
            }

        btnPickImage.setOnClickListener {
            Log.i(TAG, "Image picker is opened")
            val imagePickerIntent = Intent(Intent.ACTION_GET_CONTENT)
            imagePickerIntent.type = "image/*"
            if(imagePickerIntent.resolveActivity(packageManager)!=null){
                startActivityForResult(imagePickerIntent,PICK_PHOTO_CODE)
            }
        }

        btnSubmit.setOnClickListener {
            handleSubmitButtonClick()
        }

    }

    private fun handleSubmitButtonClick(){
        if(photoUri==null){
            Toast.makeText(this,"Please select an Image",Toast.LENGTH_LONG).show()
            return
        }
        if(etDescription.text.isBlank()){
            Toast.makeText(this,"Please provide a Description",Toast.LENGTH_LONG).show()
            return
        }
        if(signedInUser==null){
            Toast.makeText(this,"User not signed in",Toast.LENGTH_LONG).show()
            return
        }

        btnSubmit.isEnabled = false
        val photoUploadUri = photoUri as Uri
        val photoReference = storageReference.child("images/${System.currentTimeMillis()}-photo.jpg")
        //Upload image to Firebase Storage
        photoReference.putFile(photoUploadUri)
            .continueWithTask { photoUploadTask ->
                Log.i(TAG,"upload bytes: ${photoUploadTask.result?.bytesTransferred}")
                //Retrieve image url of uploaded image
                photoReference.downloadUrl
            }.continueWithTask { downloadUrlTask ->
                //create a post with image url and add it to posts collection
                val post = Post(
                    etDescription.text.toString(),
                    downloadUrlTask.result.toString(),
                    System.currentTimeMillis(),
                    signedInUser
                )
                firestoreDb.collection("posts").add(post)
            }.addOnCompleteListener { postCreationTask ->
                btnSubmit.isEnabled = false
                if(!postCreationTask.isSuccessful){
                    Log.i(TAG,"Exception during Firebase Operations", postCreationTask.exception)
                    Toast.makeText(this,"Failed to Post",Toast.LENGTH_LONG).show()
                }
                etDescription.text.clear()
                imageView.setImageResource(0)
                Toast.makeText(this,"Posted Successfully",Toast.LENGTH_LONG).show()
                val profileIntent = Intent(this,ProfileActivity::class.java)
                profileIntent.putExtra(EXTRA_USERNAME, signedInUser?.username)
                startActivity(profileIntent)
                finish()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_PHOTO_CODE){
            if(resultCode == Activity.RESULT_OK){
                photoUri = data?.data
                Log.i(TAG,"photoUri $photoUri")
                imageView.setImageURI(photoUri)
            }
            else{
                Toast.makeText(this,"App to Select Image not Found",Toast.LENGTH_LONG).show()
            }
        }
    }
}