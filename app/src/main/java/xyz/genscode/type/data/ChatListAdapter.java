package xyz.genscode.type.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

import xyz.genscode.type.ChatActivity;
import xyz.genscode.type.R;
import xyz.genscode.type.models.Chat;
import xyz.genscode.type.models.Message;
import xyz.genscode.type.models.User;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListView> {

    private Context context;
    private View view;
    private ArrayList<String> chats;
    private Chat chat;
    ChatListView chatListView;
    User user, companionUser;
    String chatName;

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
        databaseReference.child("chats").child(chatId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    chat = task.getResult().getValue(Chat.class);
                    if(chat != null){

                        //Название для чата
                        ArrayList<String> users = chat.getUsers();
                        for (int i = 0; i < users.size(); i++) {
                            String userId = users.get(i);
                            if(users.size() <= 2){
                                if(!userId.equals(user.getId())){
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
                                            }

                                            holder.tvName.setText(chatName);

                                            //Показываем чат
                                            holder.llRoot.setVisibility(View.VISIBLE);
                                        }
                                    });
                                }
                            }
                        }

                        //Последнее сообщение
                        databaseReference.child("chats").child(chatId).child("lastMessage").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                System.out.println("chatChanged");
                                Message lastMessage = dataSnapshot.getValue(Message.class);
                                if(lastMessage != null) {
                                    String lastMessageString = "";
                                    if(lastMessage.getUserId().equals(user.getId())) lastMessageString = context.getResources().getString(R.string.user_your) + ": ";
                                    if(lastMessage.getImageUrl() == null) holder.tvLastMessage.setText(lastMessageString + lastMessage.getText());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                //ERROR
                            }
                        });

                        holder.button.setOnClickListener(view -> {
                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.putExtra("chatId", chatId);
                            intent.putExtra("currentUserId", user.getId());
                            intent.putExtra("id", companionUser.getId());
                            intent.putExtra("name", chatName);
                            context.startActivity(intent);
                        });

                    }
                }
            }
        });
    }

    public void setChats(ArrayList<String> chats){
        if(chats.size() > 0){
            this.chats.clear();
            this.chats = chats;
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }


    public class ChatListView extends RecyclerView.ViewHolder {

        TextView tvName, tvLastMessage;
        TextView tvAvatarChar;
        View llAvatarBackground;
        View llRoot;
        Button button;

        public ChatListView(@NonNull View itemView) {
            super(itemView);

            button = view.findViewById(R.id.btChatList);
            tvName = view.findViewById(R.id.tvChatListUserName);
            tvLastMessage = view.findViewById(R.id.tvChatListLastMessage);
            tvAvatarChar = view.findViewById(R.id.tvChatListAvatarChar);
            llAvatarBackground = view.findViewById(R.id.llChatListAvatarBackground);
            llRoot = view.findViewById(R.id.llChatListRoot);

        }
    }
}
