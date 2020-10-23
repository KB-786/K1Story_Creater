package com.example.k1story

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.k1story.models.Post
import com.example.k1story.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_posts.*
import kotlin.math.sign

private const val TAG="PostsActivity"
const val EXTRA_USERNAME= "EXTRA_USERNAME"
open class PostsActivity : AppCompatActivity() {

    private var signedInUser: User? = null
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var posts: MutableList<Post>
    private lateinit var adapter: PostsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts)
        
        //Create data source for posts
        posts = mutableListOf()

        //Create Adapter
        adapter = PostsAdapter(this,posts)

        //Bind the adapter and layout manager to the Recycler View
        rvPosts.adapter = adapter
        rvPosts.layoutManager = LinearLayoutManager(this)


        //Query to Firestore to retrieve data
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

        var postsReference = firestoreDb
            .collection("posts")
            .limit(20)
            .orderBy("creation_time",Query.Direction.DESCENDING)

        val username= intent.getStringExtra(EXTRA_USERNAME)
        if(username!=null){
            supportActionBar?.title= username
           postsReference= postsReference.whereEqualTo("user.username",username)
        }

        postsReference.addSnapshotListener{ snapshot , exception->
            if(exception!=null || snapshot==null){
                Log.e(TAG,"Exception when quering post",exception)
                return@addSnapshotListener
            }

            val postList = snapshot.toObjects(Post::class.java)

            //Update adapter for receiving data
            posts.clear()
            posts.addAll(postList)
            adapter.notifyDataSetChanged()

            for(post in postList){
                Log.i(TAG,"Post ${post}")
            }
//            for(document in snapshot.documents){
//                Log.i(TAG,"Document ${document.id}: ${document.data}")
//            }
        }

        fabCreate.setOnClickListener {
            val intent = Intent(this,CreateActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_posts,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.menu_profile){
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra(EXTRA_USERNAME,signedInUser?.username)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}