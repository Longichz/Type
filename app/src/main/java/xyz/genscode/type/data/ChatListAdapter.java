package xyz.genscode.type.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.ArrayList;

import xyz.genscode.type.R;
import xyz.genscode.type.models.Chat;
import xyz.genscode.type.models.User;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListView> {

    private Context context;
    private View view;
    private ArrayList<String> chats;
    private Chat chat;
    ChatListView chatListView;
    User user;

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
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference("chats");
        databaseReference.child(chats.get(position)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    chat = task.getResult().getValue(Chat.class);
                    if(chat != null){

                    }
                }
            }
        });
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

        public ChatListView(@NonNull View itemView) {
            super(itemView);

            tvName = view.findViewById(R.id.tvChatListUserName);
            tvLastMessage = view.findViewById(R.id.tvChatListLastMessage);
            tvAvatarChar = view.findViewById(R.id.tvChatListAvatarChar);
            llAvatarBackground = view.findViewById(R.id.llChatListAvatarBackground);
            llRoot = view.findViewById(R.id.llChatListRoot);

        }
    }
}
