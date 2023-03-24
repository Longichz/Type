package xyz.genscode.type.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import xyz.genscode.type.MainActivity;
import xyz.genscode.type.R;
import xyz.genscode.type.interfaces.chats.OnChatClickListener;
import xyz.genscode.type.interfaces.chats.OnChatLongClickListener;
import xyz.genscode.type.models.Chat;
import xyz.genscode.type.models.Message;
import xyz.genscode.type.models.User;
import xyz.genscode.type.utils.AvatarColors;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListView> {

    private final Context context;
    private View view;
    private ArrayList<String> chats;
    private Chat chat;
    ChatListView chatListView;
    User user, companionUser;
    String chatName;
    OnChatLongClickListener longClickListener;
    OnChatClickListener clickListener;

    public void setLongClickListener(OnChatLongClickListener longClickListener){
        this.longClickListener = longClickListener;
    }

    public void setClickListener(OnChatClickListener clickListener){
        this.clickListener = clickListener;
    }

    public ChatListAdapter(Context context, ArrayList<String> chats, User user) {
        this.chats = chats;
        this.context = context;
        this.user = user;
    }

    @NonNull
    @Override
    public ChatListView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        chatListView = new ChatListView(view);
        return chatListView;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListView holder, int position) {
        holder.llRoot.setVisibility(View.GONE);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference();

        String chatId = chats.get(position);
        databaseReference.child("chats").child(chatId).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                chat = task.getResult().getValue(Chat.class);
                Chat chat1 = task.getResult().getValue(Chat.class);
                if(chat != null && chat1 != null){
                    chat1.setChatId(chatId);

                    //Название для чата
                    ArrayList<String> users = chat.getUsers();
                    for (int i = 0; i < users.size(); i++) {
                        String userId = users.get(i);
                        if(users.size() <= 2){
                            if(!userId.equals(user.getId())){ //Берем для названия чата имя собеседника
                                databaseReference.child("users").child(userId).get().addOnCompleteListener(task1 -> {
                                    if(task1.isSuccessful()){
                                        companionUser = task1.getResult().getValue(User.class);
                                        chatName = view.getResources().getString(R.string.user_removed);
                                        if(companionUser != null) {
                                            chatName = companionUser.getName();

                                            //Аватар
                                            Drawable avatarDrawable = holder.llAvatarBackground.getBackground();
                                            avatarDrawable.setTint(Color.parseColor(companionUser.getAvatarColor()));
                                            holder.tvAvatarChar.setText(String.valueOf(chatName.toUpperCase().charAt(0)));

                                            String companionUserId = companionUser.getId();
                                            String name = chatName;
                                            holder.button.setOnClickListener(view -> {
                                                if(clickListener != null){
                                                    clickListener.onChatClick(holder.getAdapterPosition(), chat1, name, companionUserId);
                                                }
                                            });

                                        }

                                        holder.tvName.setText(chatName);

                                        GenericTypeIndicator<HashMap<String, String>> t = new GenericTypeIndicator<HashMap<String, String>>() {};
                                        databaseReference.child("chats").child(chatId).child("unread").addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                HashMap<String, String> unread = snapshot.getValue(t);
                                                if(unread != null) {
                                                    int i = 0;

                                                    for (Map.Entry<String, String> entry : unread.entrySet()) {
                                                        if(!entry.getValue().equals(user.getId())){
                                                            i++;
                                                        }
                                                    }

                                                    if(i > 0){
                                                        holder.tvUnread.setText(String.valueOf(i));
                                                        holder.llUnread.setVisibility(View.VISIBLE);
                                                        chat1.setUtilsUnread(i);
                                                    }else{
                                                        holder.llUnread.setVisibility(View.GONE);
                                                        chat1.setUtilsUnread(0);
                                                    }
                                                }else{
                                                    holder.llUnread.setVisibility(View.GONE);
                                                    chat1.setUtilsUnread(0);
                                                }

                                                holder.button.setOnLongClickListener(view -> {
                                                    if (longClickListener != null) {
                                                        longClickListener.onChatLongClick(holder.getAdapterPosition(), chat1);
                                                    }
                                                    return true;
                                                });

                                                //Показываем чат
                                                holder.llRoot.setVisibility(View.VISIBLE);

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                //ERROR
                                                ((MainActivity) context).toast.show(context.getResources().getString(R.string.chat_error_loading));
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    }

                    //Последнее сообщение
                    databaseReference.child("chats").child(chatId).child("lastMessage").addValueEventListener(new ValueEventListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            Message lastMessage = dataSnapshot.getValue(Message.class);
                            if(lastMessage != null) {
                                String lastMessageString = "";
                                if(lastMessage.getUserId().equals("removed_message")){
                                    holder.tvLastMessage.setText(context.getResources().getString(R.string.message_removed));
                                    return;
                                }
                                if(lastMessage.getUserId().equals(user.getId())) lastMessageString = context.getResources().getString(R.string.user_your) + ": ";
                                if(lastMessage.getImageUrl() == null) holder.tvLastMessage.setText(lastMessageString + lastMessage.getText());
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            //ERROR
                            ((MainActivity) context).toast.show(context.getResources().getString(R.string.chat_error_loading));
                        }
                    });

                }else{
                    //Чата не существует
                    String chatName = context.getResources().getString(R.string.user_removed);

                    Drawable avatarDrawable = holder.llAvatarBackground.getBackground();
                    avatarDrawable.setTint(Color.parseColor(AvatarColors.getRandomAvatarColor()));
                    holder.tvAvatarChar.setText(String.valueOf(chatName.toUpperCase().charAt(0)));
                    holder.tvName.setText(chatName);
                    holder.llRoot.setVisibility(View.VISIBLE);
                    holder.llRoot.setAlpha(0.5f);
                }
            }
        });
    }

    public void addChat(String chatId){
        Collections.reverse(chats);
        chats.add(chatId);
        Collections.reverse(chats);

        notifyItemInserted(chats.indexOf(chatId));
    }

    public void removeChat(String chatId){
        int index = chats.indexOf(chatId);
        chats.remove(chatId);

        notifyItemRemoved(index);
    }

    public void upChat(String chatId){
        int index = chats.indexOf(chatId);
        chats.remove(chatId);

        Collections.reverse(chats);
        chats.add(chatId);
        Collections.reverse(chats);

        notifyItemMoved(index, 0);
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }


    public class ChatListView extends RecyclerView.ViewHolder {

        TextView tvName, tvLastMessage, tvUnread;
        TextView tvAvatarChar;
        View llAvatarBackground, llUnread;
        View llRoot;
        Button button;

        public ChatListView(@NonNull View itemView) {
            super(itemView);

            button = view.findViewById(R.id.btChatList);
            tvName = view.findViewById(R.id.tvChatListUserName);
            tvLastMessage = view.findViewById(R.id.tvChatListLastMessage);
            tvAvatarChar = view.findViewById(R.id.tvChatListAvatarChar);
            tvUnread = view.findViewById(R.id.tvChatListUnreadCount);
            llAvatarBackground = view.findViewById(R.id.llChatListAvatarBackground);
            llUnread = view.findViewById(R.id.llChatListUnread);
            llRoot = view.findViewById(R.id.llChatListRoot);

        }
    }
}
