package xyz.genscode.type.fragments;

import static xyz.genscode.type.fragments.SettingsFragment.ARG_USER;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import xyz.genscode.type.R;
import xyz.genscode.type.data.ChatListAdapter;
import xyz.genscode.type.models.User;

public class MessengerFragment extends Fragment {

    FirebaseDatabase database;
    DatabaseReference databaseReference;
    User mUser;

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
        return inflater.inflate(R.layout.fragment_messenger, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(mUser == null) return;

        TextView tvNoChats = view.findViewById(R.id.tvChatListNoChats);

        ArrayList<String> chats = new ArrayList<>();

        ChatListAdapter chatListAdapter = new ChatListAdapter(getContext(), chats, mUser);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(view.getContext());
        linearLayoutManager.setSmoothScrollbarEnabled(true);

        RecyclerView rvChatList = view.findViewById(R.id.rvChatList);
        rvChatList.setLayoutManager(linearLayoutManager);
        rvChatList.setAdapter(chatListAdapter);

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users");

        databaseRef.child(mUser.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("chats").getChildrenCount() > 0) {
                    tvNoChats.setVisibility(View.GONE);
                    for (DataSnapshot chatSnapshot : dataSnapshot.child("chats").getChildren()) {
                        String chatId = chatSnapshot.getValue(String.class);
                        if (chatId == null) {
                            //CHAT REMOVED
                        }
                        chatListAdapter.addChat(chatId);
                    }
                }else{
                    //NO CHATS
                    tvNoChats.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //ERROR
            }
        });



    }
}