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

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import xyz.genscode.type.R;
import xyz.genscode.type.models.Message;
import xyz.genscode.type.models.User;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewClass> {

    List<Message> messages;
    Context context;
    ViewClass viewClass;
    User user;
    private static final int THEME_DAY = 0;
    private static final int THEME_NIGHT = 1;
    private int theme;

    public MessageAdapter(List<Message> messages, Context context, User user) {
        this.messages = messages;
        this.context = context;
        this.user = user;
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

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewClass holder, int position) {
        Message message = messages.get(position);
        String messageText = message.getText();
        String messageUserId = message.getUserId();
        long messageTimestamp = message.getTimestamp();

        Date date = new Date(messageTimestamp);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm", Locale.getDefault());
        String formattedTime = simpleDateFormat.format(date);

        holder.tvMessage.setText(messageText.trim());
        holder.tvTimestamp.setText(formattedTime);

        if(!messageUserId.equals(user.getId()) ){
            holder.llRoot.setGravity(Gravity.START);
            holder.tvMessage.setTextColor(R.color.grey_800);
            holder.tvTimestamp.setTextColor(R.color.grey_500);

            if(theme == THEME_NIGHT) {
                holder.llMessage.setBackground(holder.messageNightDrawable);
            }else{
                holder.llMessage.setBackground(holder.messageDayDrawable);
            }
        }


    }

    public void addMessage(Message message){
        Collections.reverse(messages);
        messages.add(message);
        Collections.reverse(messages);
        notifyDataSetChanged();
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
        Drawable messageDayDrawable, messageNightDrawable;


        public ViewClass(@NonNull View itemView) {
            super(itemView);
            llRoot = itemView.findViewById(R.id.llRoot);
            llMessage = itemView.findViewById(R.id.llMessage);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            messageDayDrawable = itemView.getResources().getDrawable(R.drawable.message_day);
            messageDayDrawable = itemView.getResources().getDrawable(R.drawable.message_night);
        }
    }
}
