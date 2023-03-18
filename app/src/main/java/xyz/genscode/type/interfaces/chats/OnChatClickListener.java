package xyz.genscode.type.interfaces.chats;

import xyz.genscode.type.models.Chat;

public interface OnChatClickListener {
    void onChatClick(int position, Chat chat, String chatName, String companionUserId);
}
