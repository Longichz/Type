package xyz.genscode.type.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Handler;

import xyz.genscode.type.R;
import xyz.genscode.type.models.Message;
import xyz.genscode.type.models.User;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewClass> {
    RecyclerView recyclerView;
    List<Message> messages;
    Context context;
    ViewClass viewClass;
    User user;
    String chatId;
    private static final int THEME_DAY = 0;
    private static final int THEME_NIGHT = 1;
    private int theme;

    public MessageAdapter(List<Message> messages, Context context, User user, String chatId, RecyclerView recyclerView) {
        this.messages = messages;
        this.context = context;
        this.user = user;
        this.chatId = chatId;
        this.recyclerView = recyclerView;
        Collections.reverse(messages);
    }

    @NonNull
    @Override
    public ViewClass onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message,parent,false);
        viewClass = new ViewClass(view);

        int currentNightMode = view.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                theme = THEME_DAY;
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                theme = THEME_NIGHT;
                break;
        }

        return viewClass;
    }

    @SuppressLint({"ResourceAsColor", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewClass holder, int position) {
        Message message = messages.get(position);
        String messageText = message.getText();
        String messageUserId = message.getUserId();
        String messageId = message.getId() + "";
        long messageTimestamp = message.getTimestamp();

        holder.tvMessage.setText(messageText.trim());


        //Время сообщения
        Date date = new Date(messageTimestamp);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("kk:mm", Locale.getDefault());
        String formattedTime = simpleDateFormat.format(date)+"";

        //Индикторы прочтения и редактирования
        String edit = "";
        String unread = "";
        if(message.isEdited()) edit = context.getResources().getString(R.string.message_edited) + " ";
        if(!message.isRead() && message.getUserId().equals(user.getId())) unread = " " + context.getResources().getString(R.string.message_unread);

        //Отображаем
        holder.tvTimestamp.setText(edit+formattedTime+unread);

        if(!messageUserId.equals(user.getId())){

            if(!message.isRead()){
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference databaseReference = database.getReference();
                databaseReference.child("chats")
                        .child(chatId)
                        .child("unread")
                        .child(messageId)
                        .setValue(null).addOnCompleteListener(task1 -> {
                            if(task1.isSuccessful()) {
                                message.setRead(true);
                                System.out.println("Deleting: " + messageId);
                                databaseReference.child("chats")
                                        .child(chatId)
                                        .child("messages")
                                        .child(messageId)
                                        .setValue(message).addOnCompleteListener(task -> {
                                            if(task.isSuccessful()) System.out.println("Changed to read: " + messageId);
                                        });
                            }
                        });

            }

            holder.llRoot.setGravity(Gravity.START);
            if(theme == THEME_NIGHT) {
                holder.tvMessage.setTextColor(context.getResources().getColor(R.color.grey_50));
                holder.tvTimestamp.setTextColor(context.getResources().getColor(R.color.grey_00));
                holder.llMessage.setBackground(holder.messageNightDrawable);
            }else{
                holder.tvMessage.setTextColor(context.getResources().getColor(R.color.grey_800));
                holder.tvTimestamp.setTextColor(context.getResources().getColor(R.color.grey_500));
                holder.llMessage.setBackground(holder.messageDayDrawable);
            }
        }else{
            holder.llRoot.setGravity(Gravity.END);
            holder.tvMessage.setTextColor(context.getResources().getColor(R.color.white));
            holder.tvTimestamp.setTextColor(context.getResources().getColor(R.color.grey_200));
            holder.llMessage.setBackground(holder.messageMineDrawable);
        }

    }

    public void addMessage(Message message){
        Collections.reverse(messages);
        messages.add(message);
        Collections.reverse(messages);
        notifyItemInserted(messages.indexOf(message));
        recyclerView.scrollToPosition(0);
    }
    public void removeMessage(Message message){
        for (int i = 0; i < messages.size(); i++){
            if(message.getId().equals(messages.get(i).getId())){
                messages.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }
    public void changeMessage(Message message){
        for (int i = 0; i < messages.size(); i++){
            if(message.getId().equals(messages.get(i).getId())){
                messages.set(i, message);
                notifyItemChanged(i);
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class ViewClass extends RecyclerView.ViewHolder{
        View llMessage;
        TextView tvMessage;
        TextView tvTimestamp;
        LinearLayout llRoot;
        Drawable messageDayDrawable, messageNightDrawable, messageMineDrawable;


        public ViewClass(@NonNull View itemView) {
            super(itemView);
            llRoot = itemView.findViewById(R.id.llRoot);
            llMessage = itemView.findViewById(R.id.llMessage);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            messageDayDrawable = itemView.getResources().getDrawable(R.drawable.message_day);
            messageNightDrawable = itemView.getResources().getDrawable(R.drawable.message_night);
            messageMineDrawable = itemView.getResources().getDrawable(R.drawable.message_mine);
        }
    }
}
