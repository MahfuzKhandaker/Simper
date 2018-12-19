package com.example.mahfuzkhandaker.simper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.latest_messages_row.view.*

class LatestMessagesActivity : AppCompatActivity() {

    companion object {
        var currentUser: User ? = null
    }
    val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)

        recyclerView_latest_messages.adapter = adapter

        recyclerView_latest_messages.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        adapter.setOnItemClickListener { item, view ->
            val row = item as LatestMessageRow
            val intent = Intent(this, ChatLogActivity::class.java)
            intent.putExtra(NewMessageActivity.USER_KEY, row.chatPartnerUser)
            startActivity(intent)
        }

        fetchCurrentUser()

        verifyUserLogin()

        listenForLatestMessages()
    }
    val latestMessagesMap = HashMap<String, ChatLogActivity.ChatMessage>()

    private fun refreshRecyclerViewMessages(){
        adapter.clear()
        latestMessagesMap.values.forEach {
            adapter.add(LatestMessageRow(it))
        }
    }
    private fun listenForLatestMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("latest-messages/$fromId")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatLogActivity.ChatMessage::class.java) ?: return
                latestMessagesMap[p0.key !!] = chatMessage
                refreshRecyclerViewMessages()
             //   adapter.add(LatestMessageRow(chatMessage))
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatLogActivity.ChatMessage::class.java) ?: return
                latestMessagesMap[p0.key !!] = chatMessage
                refreshRecyclerViewMessages()
            }
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }
            override fun onCancelled(p0: DatabaseError) {
            }
            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })
    }
    class LatestMessageRow(val chatMessage: ChatLogActivity.ChatMessage): Item<ViewHolder>(){
        var chatPartnerUser: User ? = null

        override fun bind(viewHolder: ViewHolder, position: Int) {

            viewHolder.itemView.tV_latest_messages.text = chatMessage.text

            val chatPartnerId : String
            if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                chatPartnerId = chatMessage.toId
            } else {
                chatPartnerId = chatMessage.fromId
            }
            val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    chatPartnerUser = p0.getValue(User::class.java)
                    viewHolder.itemView.tV_username_latest_message.text = chatPartnerUser?.username
                    val targetProfileImage = viewHolder.itemView.imageView
                    Picasso.get().load(chatPartnerUser?.profilePicUrl).into(targetProfileImage)
                }
                override fun onCancelled(p0: DatabaseError) {

                }
            })
        }
        override fun getLayout(): Int {
            return R.layout.latest_messages_row
        }
    }
    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
                Log.d("LatestMessagesActivity", "Current User: ${currentUser?.username}")
            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
    private fun verifyUserLogin() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, SignUpActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.menu_new_message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this, SignUpActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

}
