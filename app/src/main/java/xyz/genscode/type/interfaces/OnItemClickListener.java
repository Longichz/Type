package xyz.genscode.type.interfaces;

import android.view.View;

import xyz.genscode.type.models.Message;

public interface OnItemClickListener {
    void onItemClick(int position, Message message, View view);
}
