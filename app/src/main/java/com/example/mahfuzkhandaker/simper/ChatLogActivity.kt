package com.example.mahfuzkhandaker.simper

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatLogActivity : AppCompatActivity() {
    companion object {
        val TAG = "ChatLog"
    }
    val adapter = GroupAdapter<ViewHolder>()

    var toUser: User ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerView_chat_log.adapter = adapter

        //  supportActionBar?.title = "Chat"
        //  val username = intent.getStringExtra(NewMessageActivity.USER_KEY)
        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser?.username

     //   performDummyData()
        listenForMessages()

        send_button_chat_log.setOnClickListener {
            Log.d(TAG, "Attemp to chat messaging....")
            functionSendMessage()
        }
    }
    private fun listenForMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
      //  val reference = FirebaseDatabase.getInstance().getReference("/messages")
        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        reference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)

                if (chatMessage != null) {
                    Log.d(TAG, chatMessage.text)
                    
                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {

                        val currentUser = LatestMessagesActivity.currentUser ?: return
                        adapter.add(ChatFromItem(chatMessage.text, currentUser))
                    } else {
                        adapter.add(ChatToItem(chatMessage.text, toUser!!))
                    }
                }
                recyclerView_chat_log.scrollToPosition(adapter.itemCount -1)
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })
    }


    class ChatMessage(val id: String, val text: String, val fromId: String, val toId: String, val timestamp: Long) {
        constructor(): this("", "", "", "", -1)
    }

    private fun functionSendMessage(){
        // how we send messages with firebase??
        val text = editText_chat_log.text.toString()

        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user.uid
        if(fromId == null) return

       // val ref = FirebaseDatabase.getInstance().getReference("/messages").push()
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val toRef = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        val chatMessage = ChatMessage(ref.key!!, text, fromId, toId, System.currentTimeMillis() / 1000)

        ref.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully we saved our chat message: ${ref.key}")
                editText_chat_log.text.clear()
                recyclerView_chat_log.scrollToPosition(adapter.itemCount -1)
            }
        toRef.setValue(chatMessage)

        val latestMessagesRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")

        latestMessagesRef.setValue(chatMessage)


        val toLatestMessagesRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")

        toLatestMessagesRef.setValue(chatMessage)
    }
}
class ChatFromItem(val text: String, val user: User) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textView_chat_from_row.text = text
        // How to load image here?
        Picasso.get().load(user.profilePicUrl).into(viewHolder.itemView.imageView_chat_from_row)
    }
    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}
class ChatToItem(val text: String, val user: User) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textView_chat_to_row.text = text

        // How we load user image in here?
        val uri = user.profilePicUrl
        val targetImageUrl = viewHolder.itemView.imageView_chat_to_row
        Picasso.get().load(uri).into(targetImageUrl)

    }
    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}

