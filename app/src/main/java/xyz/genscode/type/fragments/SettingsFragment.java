package xyz.genscode.type.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import xyz.genscode.type.EditActivity;
import xyz.genscode.type.MainActivity;
import xyz.genscode.type.ProfileActivity;
import xyz.genscode.type.R;
import xyz.genscode.type.SettingsActivity;
import xyz.genscode.type.SignInActivity;
import xyz.genscode.type.models.User;


public class SettingsFragment extends Fragment {
    public static final int SETTINGS_ABOUT = 0;
    public static final int SETTINGS_DESIGN = 1;

    public static final String ARG_USER = "arg_user";
    private static final int EDIT_NAME = 1;
    private static final int EDIT_INFO = 2;
    private static final int RESULT_OK = -1;
    private User mUser;
    View view;

    Button btSignOut; Button btEditName; Button btEditInfo; Button btProfile;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUser = (User) getArguments().getSerializable(ARG_USER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupUser();
        setSettingsButtons();
    }

    private void setupUser(){
        TextView tvUserName = view.findViewById(R.id.tvSettingsUserName);
        TextView tvUserStatus = view.findViewById(R.id.tvSettingsUserStatus);
        TextView tvAvatar = view.findViewById(R.id.tvSettingsAvatar);
        TextView tvEditInfoHint = view.findViewById(R.id.tvSettingsHintEditInfo);
        Button btPhone = view.findViewById(R.id.btSettingsPhone);
        View avatarBackground = view.findViewById(R.id.llSettingsAvatarBackground);

        btSignOut = view.findViewById(R.id.btSettingsSignOut);
        btEditName = view.findViewById(R.id.btSettingsName);
        btEditInfo = view.findViewById(R.id.btSettingsInfo);
        btProfile = view.findViewById(R.id.btSettingsProfile);

        if(mUser != null){
            try {
                btPhone.setText(mUser.getPhone());
                if(mUser.getInfo() != null){
                    btEditInfo.setText(mUser.getInfo());
                    tvEditInfoHint.setText(getResources().getString(R.string.settings_change));
                }else{
                    btEditInfo.setText(getResources().getString(R.string.settings_info));
                    tvEditInfoHint.setText(getResources().getString(R.string.settings_add));
                }

                tvUserName.setText(mUser.getName()); btEditName.setText(mUser.getName());
                tvUserStatus.setText("В сети");
                tvAvatar.setText(String.valueOf(mUser.getName().toUpperCase().charAt(0)));
                Drawable backgroundDrawable = avatarBackground.getBackground();
                backgroundDrawable.setTint(Color.parseColor(mUser.getAvatarColor()));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case EDIT_NAME:
                    if (data != null && data.getStringExtra("data") != null) {
                        User user = mUser;
                        user.setName(data.getStringExtra("data").trim());
                        updateUser(user);
                    }
                    break;
                case EDIT_INFO:
                    if (data != null && data.getStringExtra("data") != null) {
                        User user = mUser;
                        if(data.getStringExtra("data").equals("")){
                            user.setInfo(null);
                        }else{
                            user.setInfo(data.getStringExtra("data").trim());
                        }

                        updateUser(user);
                    }
                    break;
            }
        }
    }

    private void updateUser(User user){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference().child("users");

        databaseReference.child(user.getId()).setValue(user).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                mUser = user;
                setupUser();
            }else{
                assert getContext() != null;
                ((MainActivity) getContext()).toast.show(getResources().getString(R.string.error));
            }
        });
    }

    private void setSettingsButtons(){
        //Выйти из аккаунта
        btSignOut.setOnClickListener(view1 -> signOut());

        //Переход к своему профилю
        btProfile.setOnClickListener(view1 ->{
            Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
            profileIntent.putExtra("userId", mUser.getId());
            profileIntent.putExtra("currentUser", mUser);
            startActivity(profileIntent);
        });

        //Изменить О себе
        btEditInfo.setOnClickListener(view1 ->{
            Intent intent = new Intent(getContext(), EditActivity.class);
            intent.putExtra("header", getResources().getString(R.string.settings_info));
            intent.putExtra("allowNull", true);
            intent.putExtra("editValue", mUser.getInfo());
            startActivityForResult(intent, EDIT_INFO);
        });

        //Изменить Имя
        btEditName.setOnClickListener(view1 ->{
            Intent intent = new Intent(getContext(), EditActivity.class);
            intent.putExtra("editValue", mUser.getName());
            startActivityForResult(intent, EDIT_NAME);
        });

        //Настройки:
        Intent settingsIntent = new Intent(getContext(), SettingsActivity.class);

        //Оформление
        Button btDesign = view.findViewById(R.id.btSettingsDesign);
        btDesign.setOnClickListener(view1 -> {
            settingsIntent.putExtra("settings", SETTINGS_DESIGN);
            startActivity(settingsIntent);
        });

        //О приложении
        Button btAbout = view.findViewById(R.id.btSettingsAbout);
        btAbout.setOnClickListener(view1 -> {
            settingsIntent.putExtra("settings", SETTINGS_ABOUT);
            startActivity(settingsIntent);
        });
    }

    public void signOut(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getContext(), SignInActivity.class);
        startActivity(intent);
    }

}