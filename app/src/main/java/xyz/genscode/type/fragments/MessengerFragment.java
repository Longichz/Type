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

import com.google.firebase.database.ChildEventListener;
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
import xyz.genscode.type.dialog.Toast;
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
                vibrator.vibrate(3);
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

                ((MainActivity) getContext()).dialog.getMessageButton1().setOnClickListener(view2 -> {
                    ((MainActivity) getContext()).dialog.getMessageButton1().setEnabled(false);
                    ((MainActivity) getContext()).dialog.getMessageButton2().setEnabled(false);

                    deleteChat(chat);
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

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(mUser.getId()).child("chats");

        //Сортируем наши чаты по timestamp (нужно только для первой загрузки)
        Query query = databaseRef.orderByValue();

        //Слушатель чата

        ChildEventListener chatsChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { //добавляем
                chatListAdapter.addChat(snapshot.getKey());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { //изменяем
                /*
                    Так как у чата единственное поле timestamp - при изменении этого поля timestamp может только увеличиться
                    и новый timestamp в любом случае будет больше всех других timestamp,
                    поэтому при изменении, сразу поднимаем чат вверх.
                    * Но при удалении сообщения, чат не опустится вниз.
                 */
                chatListAdapter.upChat(snapshot.getKey());
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) { //удаляем
                chatListAdapter.removeChat(snapshot.getKey());
            }

            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                assert getContext() != null;
                ((MainActivity) getContext()).toast.show(getResources().getString(R.string.chat_error_loading));

            }
        };
        query.addChildEventListener(chatsChildEventListener);

        //Слушатель чата для определения есть ли вообще чаты
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getChildrenCount() > 0){
                    tvNoChats.setVisibility(View.GONE);
                }else{
                    tvNoChats.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                assert getContext() != null;
                ((MainActivity) getContext()).toast.show(getResources().getString(R.string.chat_error_loading));
            }
        });

        //Слушатель кнопки начать диалог
        View llStartDialog = view.findViewById(R.id.llStartDialogButton);
        llStartDialog.setOnClickListener(view12 -> {
            assert getContext() != null;
            ((MainActivity) getContext()).navigate("contacts");
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
                assert getContext() != null;
                ((MainActivity) getContext()).toast.show(getResources().getString(R.string.chat_error_loading));
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

        assert getContext() != null;
        databaseReference.child("chats").child(chat.getChatId()).setValue(null)
                .addOnCompleteListener(task -> ((MainActivity) getContext()).dialog.hideMessage());

    }

    private void createPopupChat(){ //Создаем popup
        View llPopupInclude = view.findViewById(R.id.llPopupChatInclude);
        View llPopupDark = view.findViewById(R.id.llPopupChatDark);
        View llPopupBackground = view.findViewById(R.id.llPopupChatBackground);
        View llPopupChatRead = view.findViewById(R.id.llPopupChatRead);
        View llPopupChatDelete = view.findViewById(R.id.llPopupChatDelete);
        Button btPopupChatRead = view.findViewById(R.id.btPopupChatRead);
        Button btPopupChatDelete = view.findViewById(R.id.btPopupChatDelete);

        popupChat = new PopupChat(llPopupInclude, llPopupDark, llPopupBackground, llPopupChatRead, llPopupChatDelete,
                btPopupChatRead, btPopupChatDelete);
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
                            assert getContext() != null;
                            ((MainActivity) getContext()).toast.show(getResources().getString(R.string.chat_removed_content), Toast.TIME_LONG);
                        }
                    }
            }
        }
    }
}