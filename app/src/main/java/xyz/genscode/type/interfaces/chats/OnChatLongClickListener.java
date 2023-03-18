package xyz.genscode.type.interfaces.chats;

import android.graphics.drawable.Drawable;
import android.view.View;

import xyz.genscode.type.models.Chat;


public interface OnChatLongClickListener {
    void onChatLongClick(int position, Chat chat);
}
