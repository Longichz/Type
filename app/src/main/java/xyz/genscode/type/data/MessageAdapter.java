package xyz.genscode.type.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import xyz.genscode.type.R;
import xyz.genscode.type.interfaces.messages.OnItemClickListener;
import xyz.genscode.type.models.Message;
import xyz.genscode.type.models.User;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    OnItemClickListener clickListener;
    RecyclerView recyclerView;
    List<Message> messages;
    Context context;
    DateViewHolder dateViewHolder;
    User user;
    String chatId;
    static String messageDatePrevious;
    public static final int THEME_DAY = 0;
    public static final int THEME_NIGHT = 1;
    private static final int VIEW_MESSAGE = 0;
    private static final int VIEW_DATE = 1;

    public int theme;

    //На случай создания функционала выделения нескольких сообщений
    /*
    public boolean isSelectMode = false;
    ArrayList<Message> selectObjects = new ArrayList<Message>();
    ArrayList<View> selectObjectsView = new ArrayList<View>();
     */

    public void  setClickListener(OnItemClickListener clickListener){
        this.clickListener = clickListener;
    }

    public MessageAdapter(List<Message> messages, Context context, User user, String chatId, RecyclerView recyclerView) {
        messageDatePrevious = "";
        this.messages = messages;
        this.context = context;
        this.user = user;
        this.chatId = chatId;
        this.recyclerView = recyclerView;
        Collections.reverse(messages);
    }

    public int getItemViewType(int position){
        Message message = messages.get(position);
        if(message.isDate()){
            return VIEW_DATE;
        }else{
            return VIEW_MESSAGE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                theme = THEME_DAY;
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                theme = THEME_NIGHT;
                break;
        }

        if (viewType == VIEW_MESSAGE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
            return new ViewClass(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_date, parent, false);
            dateViewHolder = new DateViewHolder(view);
            return dateViewHolder;
        }

    }

    @SuppressLint({"ResourceAsColor", "SetTextI18n", "ClickableViewAccessibility"})
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Message message = messages.get(position);
        String messageText = message.getText();
        String messageUserId = message.getUserId();
        String messageId = message.getId() + "";
        long messageTimestamp = message.getTimestamp();
        long timestamp = System.currentTimeMillis();

        if(message.isDate()){ //Если это дата, добавляем её
            DateViewHolder dateViewHolder1 = (DateViewHolder) holder;

            //Дата сообщения
            Date date = new Date(messageTimestamp);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM", Locale.getDefault());
            SimpleDateFormat dayDateFormat = new SimpleDateFormat("dd", Locale.getDefault());
            String formattedTime = simpleDateFormat.format(date)+"";
            int formattedDay = Integer.parseInt(dayDateFormat.format(date));

            //Дата сейчас
            Date currentDate = new Date(timestamp);
            SimpleDateFormat currentDayDateFormat = new SimpleDateFormat("dd", Locale.getDefault());
            int currentFormattedDay = Integer.parseInt(currentDayDateFormat.format(currentDate));

            if(formattedDay == currentFormattedDay){ // Сегодня
                dateViewHolder1.mDateTextView.setText(context.getResources().getString(R.string.message_today));
            }else if(formattedDay == currentFormattedDay-1){ //Вчера
                dateViewHolder1.mDateTextView.setText(context.getResources().getString(R.string.message_tomorrow));
            }else{
                dateViewHolder1.mDateTextView.setText(formattedTime);
            }

            return;
        }

        ViewClass messageHolder = (ViewClass) holder;

        messageHolder.tvMessage.setText(messageText.trim());

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
        messageHolder.tvTimestamp.setText(edit+formattedTime+unread);

        if(!messageUserId.equals(user.getId())){ //Это сообщение отправлено собеседником?

            //Это сообщение еще не прочитано?
            if(!message.isRead()){
                //Читаем
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference databaseReference = database.getReference();
                databaseReference.child("chats")
                        .child(chatId)
                        .child("unread")
                        .child(messageId)
                        .setValue(null).addOnCompleteListener(task1 -> {
                            if(task1.isSuccessful()) {
                                message.setRead(true);
                                databaseReference.child("chats")
                                        .child(chatId)
                                        .child("messages")
                                        .child(messageId)
                                        .setValue(message);
                            }
                        });

            }

            //Сообщение собеседника
            messageHolder.llRoot.setGravity(Gravity.START);
            if(theme == THEME_NIGHT) {
                //В ночную тему
                messageHolder.tvMessage.setTextColor(ContextCompat.getColor(context, R.color.grey_50));
                messageHolder.tvTimestamp.setTextColor(ContextCompat.getColor(context, R.color.grey_500));
                messageHolder.llMessage.setBackground(messageHolder.messageNightDrawable);
            }else{
                //В светлую тему
                messageHolder.tvMessage.setTextColor(ContextCompat.getColor(context, R.color.grey_800));
                messageHolder.tvTimestamp.setTextColor(ContextCompat.getColor(context, R.color.grey_500));
                messageHolder.llMessage.setBackground(messageHolder.messageDayDrawable);
            }
        }else{
            //Свои сообщения
            messageHolder.llRoot.setGravity(Gravity.END);
            if(theme == THEME_NIGHT) {
                //В ночную тему
                messageHolder.tvMessage.setTextColor(ContextCompat.getColor(context, R.color.grey_50));
                messageHolder.tvTimestamp.setTextColor(ContextCompat.getColor(context, R.color.grey_200));
                messageHolder.llMessage.setBackground(messageHolder.messageMineNightDrawable);
            }else{
                //В светлую тему
                messageHolder.tvMessage.setTextColor(ContextCompat.getColor(context, R.color.white));
                messageHolder.tvTimestamp.setTextColor(ContextCompat.getColor(context, R.color.grey_200));
                messageHolder.llMessage.setBackground(messageHolder.messageMineDayDrawable);
            }

        }

        //Эффект нажатия на сообщение
        messageHolder.llMessage.setOnTouchListener((View view12, MotionEvent motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    view12.animate().alpha(0.8f).scaleX(0.99f).scaleY(0.99f).setDuration(35);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    view12.animate().alpha(1).scaleX(1).scaleY(1).setDuration(35);
                    break;
            }
            return false;
        });

        //Устанавливаем слушатель клика
        messageHolder.llMessage.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(position, message, messageHolder.llMessage);
            }
        });

    }

    public void addMessage(Message message){
        //Подготавливаем timestamp
        long messageTimestamp = message.getTimestamp();
        Date date = new Date(messageTimestamp);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String messageDate = simpleDateFormat.format(date);

        if(messageDate.equals(messageDatePrevious)){ //Совпадает ли дата сообщения с предыдущим
            //Добавляем обычное сообщение под той же датой

            Collections.reverse(messages);
            messages.add(message);
            Collections.reverse(messages);

            notifyItemInserted(messages.indexOf(message));

        }else{
            //Дата отличается, сообщение в другой день, добавляем дату и потом само сообщение

            Message dateMessage = new Message();
            dateMessage.setDate(true);
            dateMessage.setTimestamp(messageTimestamp);

            Collections.reverse(messages);
            messages.add(dateMessage);
            Collections.reverse(messages);
            notifyItemInserted(messages.indexOf(dateMessage));

            Collections.reverse(messages);
            messages.add(message);
            Collections.reverse(messages);
            notifyItemInserted(messages.indexOf(message));

        }

        recyclerView.scrollToPosition(0);

        messageDatePrevious = messageDate;
    }
    public void removeMessage(Message message){
        //Ищем сообщение в чате
        for (int i = 0; i < messages.size(); i++){
            if(message.getId().equals(messages.get(i).getId())){
                //Удаляем сообщение
                messages.remove(i);
                notifyItemRemoved(i);

                //Если удаленное сообщение было единственным за день, удаляем дату
                if(i != 0 &&
                        (messages.get(i).isDate() && messages.get(i-1).isDate())){ //Если подряд идут две даты, удаляем одну.
                    messages.remove(i);
                    notifyItemRemoved(i);
                }
                if (messages.get(0).isDate()) { //Если последнее сообщение дата - удаляем её
                    messages.remove(0);
                    notifyItemRemoved(0);
                    messageDatePrevious = "";
                }
                break;
            }
        }
    }

    public void changeMessage(Message message){
        //Ищем сообщение в чате
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

    public static class ViewClass extends RecyclerView.ViewHolder{
        View llMessage;
        TextView tvMessage;
        TextView tvTimestamp;
        LinearLayout llRoot;
        Drawable messageDayDrawable, messageNightDrawable, messageMineDayDrawable, messageMineNightDrawable;


        public ViewClass(@NonNull View itemView) {
            super(itemView);
            llRoot = itemView.findViewById(R.id.llRoot);
            llMessage = itemView.findViewById(R.id.llMessage);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            messageDayDrawable = ContextCompat.getDrawable(itemView.getContext(), R.drawable.message_day);
            messageNightDrawable = ContextCompat.getDrawable(itemView.getContext(), R.drawable.message_night);
            messageMineDayDrawable = ContextCompat.getDrawable(itemView.getContext(), R.drawable.message_mine_day);
            messageMineNightDrawable = ContextCompat.getDrawable(itemView.getContext(), R.drawable.message_mine_night);
        }
    }

    public static class DateViewHolder extends RecyclerView.ViewHolder {
        private final TextView mDateTextView;

        public DateViewHolder(View itemView) {
            super(itemView);
            mDateTextView = itemView.findViewById(R.id.tvDate);
        }
    }
}
