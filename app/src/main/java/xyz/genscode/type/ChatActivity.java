package xyz.genscode.type;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xyz.genscode.type.data.MessageAdapter;
import xyz.genscode.type.models.Chat;
import xyz.genscode.type.models.Message;
import xyz.genscode.type.models.User;
import xyz.genscode.type.utils.AvatarColors;

public class ChatActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference databaseReference;
    Chat chat;
    String chatId; String currentUserId; String userId;
    User user; User currentUser;
    View llSend; EditText etMessage; RecyclerView rvChat;
    View llLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Handler handler = new Handler();

        Animation anim = new AlphaAnimation(0.5f, 1.0f); anim.setDuration(250); anim.setRepeatMode(Animation.REVERSE); anim.setRepeatCount(Animation.INFINITE);
        Animation anim2 = new AlphaAnimation(0.5f, 1.0f); anim2.setDuration(250); anim2.setRepeatMode(Animation.REVERSE); anim2.setRepeatCount(Animation.INFINITE);
        Animation anim3 = new AlphaAnimation(0.5f, 1.0f); anim3.setDuration(250); anim3.setRepeatMode(Animation.REVERSE); anim3.setRepeatCount(Animation.INFINITE);
        ImageView loading1 = findViewById(R.id.ivChatLoading1); loading1.startAnimation(anim);
        ImageView loading2 = findViewById(R.id.ivChatLoading2); handler.postDelayed(() -> loading2.startAnimation(anim2), 125);
        ImageView loading3 = findViewById(R.id.ivChatLoading3); handler.postDelayed(() -> loading3.startAnimation(anim3), 250);
        llLoading = findViewById(R.id.llChatLoading);
        llLoading.setVisibility(View.VISIBLE);

        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();

        TextView tvName = findViewById(R.id.tvChatTopIndicator);
        View llBack = findViewById(R.id.llChatBack);
        llSend = findViewById(R.id.llChatSend);
        etMessage = findViewById(R.id.etChatMessage);
        rvChat = findViewById(R.id.rvChat);

        Intent intent = getIntent();

        if(!intent.hasExtra("currentUserId") || !intent.hasExtra("id")) finish();

        currentUserId = intent.getStringExtra("currentUserId");
        userId = intent.getStringExtra("id");

        llSend.setEnabled(false);
        llSend.setAlpha(0.6f);

        databaseReference.child("users").child(userId).get().addOnCompleteListener(task2 -> {
            if (task2.isSuccessful()) {
                user = task2.getResult().getValue(User.class);
                databaseReference.child("users").child(currentUserId).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        currentUser = task.getResult().getValue(User.class);
                        if(intent.hasExtra("chatId")) {
                            chatId = intent.getStringExtra("chatId");
                            initializeChat();
                        }else{
                            llLoading.setVisibility(View.INVISIBLE);
                            llSend.setEnabled(true);
                            llSend.setAlpha(1);
                        }
                    }
                });
            }
        });

        if(intent.hasExtra("name")){
            tvName.setText(intent.getStringExtra("name"));
        }

        llSend.setOnClickListener(view -> {
            sendMessage();
        });

        llBack.setOnClickListener(view -> {
            finish();
        });

    }

    private void initializeChat(){
        if(currentUser != null){
            databaseReference.child("chats").child(chatId).get().addOnCompleteListener(task1 -> {
                if (task1.isSuccessful()) {
                    llSend.setEnabled(true);
                    llSend.setAlpha(1);
                    chat = task1.getResult().getValue(Chat.class);
                    if(chat != null) {

                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                        linearLayoutManager.setReverseLayout(true);
                        linearLayoutManager.setSmoothScrollbarEnabled(true);

                        rvChat.setLayoutManager(linearLayoutManager);
                        List<Message> messagesAdapterList = new ArrayList<>();

                        MessageAdapter adapter = new MessageAdapter(messagesAdapterList, this, currentUser);
                        rvChat.setItemAnimator(new DefaultItemAnimator());
                        rvChat.setAdapter(adapter);

                        llLoading.setVisibility(View.INVISIBLE);

                        ChildEventListener messagesChildEventListener = new ChildEventListener() {
                            @Override
                            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                                Message newMessage = snapshot.getValue(Message.class);
                                adapter.addMessage(newMessage);

                            }

                            @Override
                            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                            }

                            @Override
                            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                            }

                            @Override
                            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        };

                        DatabaseReference messagesDatabaseReference = databaseReference.child("chats").child(chatId).child("messages");
                        messagesDatabaseReference.addChildEventListener(messagesChildEventListener);

                    }else{
                        llLoading.setVisibility(View.INVISIBLE);
                    }
                } else {
                    //CONNECT ERROR
                }
            });
        }
    }

    private void sendMessage(){
        Message message = new Message();
        message.setUserId(currentUserId);
        message.setText(etMessage.getText().toString().trim());
        message.setTimestamp(System.currentTimeMillis());

        etMessage.setText("");
        llSend.setAlpha(0.5f);
        llSend.setEnabled(false);

        if(chat == null){
            ArrayList<String> users = new ArrayList<>();
            users.add(currentUserId);
            users.add(userId);

            Chat chat = new Chat();
            chat.setLastMessage(message);
            chat.setUsers(users);

            databaseReference.child("chats").push().setValue(chat, (error, ref) -> {
                if (error == null) {
                    chatId = ref.getKey();
                    chat.setChatId(chatId);

                    databaseReference.child("chats").child(chatId).child("messages").push().setValue(message);

                    currentUser.addChat(chatId);
                    user.addChat(chatId);
                    databaseReference.child("users").child(userId).setValue(user);
                    databaseReference.child("users").child(currentUserId).setValue(currentUser, (error1, ref1) -> {
                        if (error1 == null) {

                        }
                    });

                    initializeChat();
                }
            });
        }else{
            chat.setLastMessage(message);
            databaseReference.child("chats").child(chatId).child("lastMessage").setValue(message);
            databaseReference.child("chats").child(chatId).child("messages").push().setValue(message, (error1, ref1) -> {
                if (error1 == null) {
                    llSend.setAlpha(1f);
                    llSend.setEnabled(true);
                }
            });
        }
    }
}