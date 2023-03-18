package xyz.genscode.type.fragments;

import static xyz.genscode.type.fragments.SettingsFragment.ARG_USER;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import xyz.genscode.type.ChatActivity;
import xyz.genscode.type.MainActivity;
import xyz.genscode.type.R;
import xyz.genscode.type.data.ChatListAdapter;
import xyz.genscode.type.models.Chat;
import xyz.genscode.type.models.Message;
import xyz.genscode.type.models.User;
import xyz.genscode.type.utils.PopupChat;

public class MessengerFragment extends Fragment {

    FirebaseDatabase database;
    DatabaseReference databaseReference;
    User mUser;
    View view;
    @SuppressLint("StaticFieldLeak")
    static PopupChat popupChat;

    private static final int RESULT_OK = -1;
    private static final int CHAT = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();

        if (getArguments() != null) {
            mUser = (User) getArguments().getSerializable(ARG_USER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_messenger, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(mUser == null) return;

        TextView tvNoChats = view.findViewById(R.id.tvChatListNoChats);

        ArrayList<String> chats = new ArrayList<>();

        RecyclerView rvChatList = view.findViewById(R.id.rvChatList);

        ChatListAdapter chatListAdapter = new ChatListAdapter(getContext(), chats, mUser);

        //Длинное нажатие на чат
        chatListAdapter.setLongClickListener((position, chat) -> {
            Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            if(vibrator.hasVibrator()){
                vibrator.vibrate(10);
            }

            if(chat.getUtilsUnread() > 0) {
                popupChat.show();
            }else{
                popupChat.show(false);
            }

            popupChat.llPopupBackgroundDark.setOnClickListener(view1 -> { //Скрытие
                popupChat.hide();
            });

            popupChat.btPopupRead.setOnClickListener(view1 -> {
                markRead(chat);
                popupChat.hide();
            });


            popupChat.btPopupDelete.setOnClickListener(view1 ->{
                popupChat.hide();
                ((MainActivity) getContext()).dialog.showMessageWithRedButton(
                        getResources().getString(R.string.chat_remove),
                        getResources().getString(R.string.chat_remove_content),
                        getResources().getString(R.string.chat_remove),
                        "def"
                );

                ((MainActivity) getContext()).dialog.getMessageButton1().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MainActivity) getContext()).dialog.getMessageButton1().setEnabled(false);
                        ((MainActivity) getContext()).dialog.getMessageButton2().setEnabled(false);

                        deleteChat(chat);
                    }
                });

            });

        });

        chatListAdapter.setClickListener((position, chat, chatName, companionUserId) -> {
            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra("chatId", chat.getChatId());
            intent.putExtra("currentUserId", mUser.getId());
            intent.putExtra("id", companionUserId);
            intent.putExtra("name", chatName);
            startActivityForResult(intent, CHAT);
        });


        //Инициализируем чаты
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(view.getContext());
        linearLayoutManager.setSmoothScrollbarEnabled(true);

        rvChatList.setLayoutManager(linearLayoutManager);
        rvChatList.setAdapter(chatListAdapter);

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users");

        databaseRef.child(mUser.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> chats = new ArrayList<>();
                if(dataSnapshot.child("chats").getChildrenCount() > 0) {
                    tvNoChats.setVisibility(View.GONE);
                    for (DataSnapshot chatSnapshot : dataSnapshot.child("chats").getChildren()) {
                        String chatId = chatSnapshot.getKey();
                        if (chatId != null) {
                            chats.add(chatId);
                        }
                    }
                    chatListAdapter.setChats(chats);
                }else{
                    //NO CHATS
                    chatListAdapter.setChats(chats);

                    tvNoChats.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //ERROR
                ((MainActivity) getContext()).dialog.showMessage(getResources().getString(R.string.chat_error_loading_header),
                        getResources().getString(R.string.chat_error_loading_content));
            }
        });

        createPopupChat();
    }

    private void markRead(Chat chat){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference();

        //Ищем все непрочитанные сообщения
        Query query = databaseReference.child("chats").child(chat.getChatId()).child("messages").orderByChild("read").endAt(false);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) { //Читаем все
                    Message message = childSnapshot.getValue(Message.class);
                    if (message != null) {

                        databaseReference.child("chats")
                                .child(chat.getChatId())
                                .child("unread")
                                .child(message.getId())
                                .setValue(null);

                        databaseReference.child("chats")
                                .child(chat.getChatId())
                                .child("messages")
                                .child(message.getId())
                                .setValue(message);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

    }

    private void deleteChat(Chat chat){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference();

        //Удаляем чат для всех пользователей
        ArrayList<String> users = chat.getUsers();
        for (int i = 0; i < users.size(); i++) {
            String userId = users.get(i);
            databaseReference.child("users").child(userId).child("chats").child(chat.getChatId()).setValue(null);
        }


        //Удаляем сам чат со всеми сообщениями
        databaseReference.child("chats").child(chat.getChatId()).setValue(null)
                .addOnCompleteListener(task -> ((MainActivity) getContext()).dialog.hideMessage());

    }

    private void createPopupChat(){ //Создаем popup
        if(popupChat == null){
            System.out.println("PopupChat: Now class is constructing...");
            View llPopupInclude = view.findViewById(R.id.llPopupChatInclude);
            View llPopupDark = view.findViewById(R.id.llPopupChatDark);
            View llPopupBackground = view.findViewById(R.id.llPopupChatBackground);
            View llPopupChatRead = view.findViewById(R.id.llPopupChatRead);
            View llPopupChatDelete = view.findViewById(R.id.llPopupChatDelete);
            Button btPopupChatRead = view.findViewById(R.id.btPopupChatRead);
            Button btPopupChatDelete = view.findViewById(R.id.btPopupChatDelete);

            popupChat = new PopupChat(llPopupInclude, llPopupDark, llPopupBackground, llPopupChatRead, llPopupChatDelete,
                    btPopupChatRead, btPopupChatDelete);
        }else{
            System.out.println("PopupChat: Now class is already constructed");
            System.out.println("PopupChat: Removing object...");
            popupChat = null;
            System.gc();
            createPopupChat();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Получаем результат переписки
        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case CHAT:
                    if (data != null && data.getStringExtra("data") != null) {
                        if(data.getStringExtra("data").equals("removed")){ //Чат удален
                            ((MainActivity) getContext()).dialog.showMessage(getResources().getString(R.string.chat_removed),
                                    getResources().getString(R.string.chat_removed_content));
                        }
                    }
            }
        }
    }
}