package xyz.genscode.type;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Fade;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


import jp.wasabeef.blurry.Blurry;
import xyz.genscode.type.data.MessageAdapter;
import xyz.genscode.type.interfaces.OnItemClickListener;
import xyz.genscode.type.models.Chat;
import xyz.genscode.type.models.Message;
import xyz.genscode.type.models.User;
import xyz.genscode.type.utils.PopupMessage;

public class ChatActivity extends AppCompatActivity implements OnItemClickListener {
    FirebaseDatabase database;
    DatabaseReference databaseReference;
    Chat chat;
    String chatId; String currentUserId; String userId;
    User user; User currentUser;
    View llSend; EditText etMessage; RecyclerView rvChat;
    View llLoading;View blurredView;
    Handler handler;

    static PopupMessage popupMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        overridePendingTransition(0, 0);

        getWindow().setEnterTransition(new Fade());
        getWindow().setExitTransition(new Fade());
        getWindow().setReenterTransition(new Fade());
        getWindow().setReturnTransition(new Fade());

        ViewGroup rootView = findViewById(android.R.id.content);
        Animation enterAnimation = AnimationUtils.loadAnimation(this, R.anim.jump);
        rootView.startAnimation(enterAnimation);

        handler = new Handler();

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

        TextView tvName = findViewById(R.id.tvChatName); tvName.setEnabled(false);
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

                        tvName.setEnabled(true);
                        tvName.setOnClickListener(view -> {
                            Intent profileIntent = new Intent(this, ProfileActivity.class);
                            profileIntent.putExtra("userId", user.getId());
                            profileIntent.putExtra("currentUser", currentUser);
                            profileIntent.putExtra("fromChat", true);
                            startActivity(profileIntent);
                        });
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

        createPopupMessage();

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

                        MessageAdapter adapter = new MessageAdapter(messagesAdapterList, this, currentUser, chatId, rvChat);
                        adapter.setClickListener(new OnItemClickListener() {
                            @Override
                            public void onItemClick(int position, Message message, View view) {
                                View llChatRoot = findViewById(R.id.llChatRoot);
                                View llChat = findViewById(R.id.llChat);
                                ImageView blurImageView = findViewById(R.id.blurImageView);

                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                                View view3 = getCurrentFocus();

                                if (view3 != null) {
                                    // Скрыть клавиатуру
                                    imm.hideSoftInputFromWindow(view3.getWindowToken(), 0);
                                }

                                handler.postDelayed((Runnable) () -> {
                                    view.setVisibility(View.INVISIBLE);
                                    Blurry.with(getApplicationContext())
                                            .radius(20)
                                            .capture(llChatRoot)
                                            .getAsync(bitmap -> {
                                                blurImageView.setImageBitmap(bitmap);
                                                blurImageView.setVisibility(View.VISIBLE);
                                                llChat.animate().alpha(0).setDuration(250).start();
                                                view.setVisibility(View.VISIBLE);
                                                popupMessage.show();
                                            });

                                    //Определяем позицию сообщения
                                    Rect rect = new Rect();
                                    view.getGlobalVisibleRect(rect);
                                    int absoluteY = rect.top;
                                    long messageTimestamp = message.getTimestamp();

                                    //Настраиваем копию выбранного сообщения
                                    LinearLayout llPopupMessageRoot = findViewById(R.id.llPopupMessageRoot);
                                    View llPopupMessage = findViewById(R.id.llPopupMessage);
                                    TextView tvPopupMessage = findViewById(R.id.tvPopupMessage);
                                    TextView tvPopupMessageTimestamp = findViewById(R.id.tvPopupMessageTimestamp);

                                    Drawable messageDayDrawable = getResources().getDrawable(R.drawable.message_day);
                                    Drawable messageNightDrawable = getResources().getDrawable(R.drawable.message_night);
                                    Drawable messageMineDrawable = getResources().getDrawable(R.drawable.message_mine);

                                    Date date = new Date(messageTimestamp);
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("kk:mm", Locale.getDefault());
                                    String formattedTime = simpleDateFormat.format(date)+"";
                                    String edit = "";
                                    String unread = "";
                                    if(message.isEdited()) edit = getApplicationContext().getResources().getString(R.string.message_edited) + " ";
                                    if(!message.isRead() && message.getUserId().equals(currentUser.getId())) unread = " " + getResources().getString(R.string.message_unread);

                                    tvPopupMessageTimestamp.setText(edit+formattedTime+unread);
                                    tvPopupMessage.setText(message.getText());

                                    if(!message.getUserId().equals(currentUser.getId())){
                                        llPopupMessageRoot.setGravity(Gravity.START);
                                        if(adapter.theme == adapter.THEME_NIGHT) {
                                            tvPopupMessage.setTextColor(getResources().getColor(R.color.grey_50));
                                            tvPopupMessageTimestamp.setTextColor(getResources().getColor(R.color.grey_00));
                                            llPopupMessage.setBackground(messageNightDrawable);
                                        }else{
                                            tvPopupMessage.setTextColor(getResources().getColor(R.color.grey_800));
                                            tvPopupMessageTimestamp.setTextColor(getResources().getColor(R.color.grey_500));
                                            llPopupMessage.setBackground(messageDayDrawable);
                                        }
                                    }else{
                                        llPopupMessageRoot.setGravity(Gravity.END);
                                        tvPopupMessage.setTextColor(getResources().getColor(R.color.white));
                                        tvPopupMessageTimestamp.setTextColor(getResources().getColor(R.color.grey_200));
                                        llPopupMessage.setBackground(messageMineDrawable);
                                    }

                                    llPopupMessageRoot.setY(absoluteY);

                                }, 25);


                                popupMessage.llPopupBackgroundDark.setOnClickListener(view1 -> {
                                    popupMessage.hide();

                                    blurImageView.setVisibility(View.INVISIBLE);
                                    llChat.animate().alpha(1).setDuration(250).start();
                                });

                                popupMessage.btPopupEdit.setOnClickListener(view12 -> {
                                    popupMessage.hide();

                                    blurImageView.setVisibility(View.INVISIBLE);
                                    llChat.animate().alpha(1).setDuration(250).start();
                                });

                                popupMessage.btPopupDelete.setOnClickListener(view12 -> {
                                    popupMessage.hide();

                                    blurImageView.setVisibility(View.INVISIBLE);
                                    llChat.animate().alpha(1).setDuration(250).start();
                                });
                            }
                        });

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
                                Message changedMessage = snapshot.getValue(Message.class);
                                adapter.changeMessage(changedMessage);
                            }

                            @Override
                            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                                Message removedMessage = snapshot.getValue(Message.class);
                                adapter.removeMessage(removedMessage);
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

                    databaseReference.child("chats").child(chatId).child("messages").push().setValue(message).addOnSuccessListener(unused -> {

                    });

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

            String key = databaseReference.child("chats").child(chatId).child("messages").push().getKey();
            message.setId(key);
            databaseReference.child("chats").child(chatId).child("unread").child(key).setValue(currentUserId, (error, ref) -> {
                if(error == null) {
                    databaseReference.child("chats").child(chatId).child("messages").child(key).setValue(message, (error1, ref1) -> {
                        if (error1 == null) {
                            llSend.setAlpha(1f);
                            llSend.setEnabled(true);
                        }
                    });
                }
            });

        }
    }

    private void createPopupMessage(){
        if(popupMessage == null){
            System.out.println("PopupMessage: Now class is constructing...");
            View llPopupInclude = findViewById(R.id.llPopupInclude);
            View llPopupDark = findViewById(R.id.llPopupDark);
            View llPopupBackground = findViewById(R.id.llPopupBackground);
            View llPopupMessageEdit = findViewById(R.id.llPopupMessageEdit);
            View llPopupMessageDelete = findViewById(R.id.llPopupMessageDelete);
            Button btPopupMessageEdit = findViewById(R.id.btPopupMessageEdit);
            Button btPopupMessageDelete = findViewById(R.id.btPopupMessageDelete);


            popupMessage = new PopupMessage(llPopupInclude, llPopupDark, llPopupBackground, llPopupMessageEdit, llPopupMessageDelete,
                    btPopupMessageEdit, btPopupMessageDelete);
        }else{
            System.out.println("PopupMessage: Now class is already constructed");
            System.out.println("PopupMessage: Removing object...");
            popupMessage = null;
            System.gc();
            createPopupMessage();
        }
    }

    @Override
    public void onBackPressed() {
        if(popupMessage.isShowed){
            View llChat = findViewById(R.id.llChat);
            ImageView blurImageView = findViewById(R.id.blurImageView);

            popupMessage.hide();

            blurImageView.setVisibility(View.INVISIBLE);
            llChat.animate().alpha(1).setDuration(250).start();
        }
        super.onBackPressed();
    }

    @Override
    public void onItemClick(int position, Message message, View view) {}
}