package xyz.genscode.type;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Fade;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import xyz.genscode.type.dialog.Toast;
import xyz.genscode.type.models.User;

public class ProfileActivity extends AppCompatActivity {

    User user;
    User currentUser;
    String userId, phone;
    View llPhone, llAbout, llAvatar, llLoading, llActions, llProfileNotRegistered, llCall, llType, llBack;
    TextView tvName, tvPhone, tvAbout, tvAvatar;
    Button btType, btCall;

    private boolean foundChat = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        overridePendingTransition(0, 0);

        getWindow().setEnterTransition(new Fade());
        getWindow().setExitTransition(new Fade());
        getWindow().setReenterTransition(new Fade());
        getWindow().setReturnTransition(new Fade());

        ViewGroup rootView = findViewById(android.R.id.content);
        Animation enterAnimation = AnimationUtils.loadAnimation(this, R.anim.jump);
        rootView.startAnimation(enterAnimation);

        Intent intent = getIntent();
        if(intent == null || (!intent.hasExtra("phone") && !intent.hasExtra("currentUser"))) finish();

        Handler handler = new Handler();

        Animation anim = new AlphaAnimation(0.5f, 1.0f); anim.setDuration(250); anim.setRepeatMode(Animation.REVERSE); anim.setRepeatCount(Animation.INFINITE);
        Animation anim2 = new AlphaAnimation(0.5f, 1.0f); anim2.setDuration(250); anim2.setRepeatMode(Animation.REVERSE); anim2.setRepeatCount(Animation.INFINITE);
        Animation anim3 = new AlphaAnimation(0.5f, 1.0f); anim3.setDuration(250); anim3.setRepeatMode(Animation.REVERSE); anim3.setRepeatCount(Animation.INFINITE);
        ImageView loading1 = findViewById(R.id.ivProfileLoading1); loading1.startAnimation(anim);
        ImageView loading2 = findViewById(R.id.ivProfileLoading2); handler.postDelayed(() -> loading2.startAnimation(anim2), 125);
        ImageView loading3 = findViewById(R.id.ivProfileLoading3); handler.postDelayed(() -> loading3.startAnimation(anim3), 250);

        llBack = findViewById(R.id.llProfileBack); llBack.setOnClickListener(view -> finish());
        llProfileNotRegistered = findViewById(R.id.llProfileNotRegistered);
        llLoading = findViewById(R.id.llProfileLoading);
        llActions = findViewById(R.id.llProfileActionsPanel);
        llAvatar = findViewById(R.id.llProfileAvatarBackground);
        llAbout = findViewById(R.id.llProfileAbout);
        llPhone = findViewById(R.id.llProfilePhone);
        tvName = findViewById(R.id.tvProfileName);
        tvPhone = findViewById(R.id.tvProfilePhone);
        tvAbout = findViewById(R.id.tvProfileAbout);
        tvAvatar = findViewById(R.id.tvProfileAvatarChar);
        btCall = findViewById(R.id.btProfileCall); btType = findViewById(R.id.btProfileType);
        llCall = findViewById(R.id.llProfileCall); llType = findViewById(R.id.llProfileType);

        llActions.setEnabled(false);
        btCall.setEnabled(false);
        btType.setEnabled(false);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference();

        //Получаем данные
        if (intent != null) {

            currentUser = (User) intent.getSerializableExtra("currentUser");

            if (intent.hasExtra("userId")) { //Предоставлен userId, ищем пользователя по userId
                userId = intent.getStringExtra("userId");
                databaseReference.child("users").child(userId).get().addOnCompleteListener(task -> {
                    llActions.setAlpha(1);
                    llActions.setEnabled(true);

                    if (task.isSuccessful()) {
                        user = task.getResult().getValue(User.class);
                        loadUser();
                    }
                });
            }

            if(intent.hasExtra("phone")) { //Предоставлен телефон, ищем пользователя по телефону
                phone = intent.getStringExtra("phone");

                GenericTypeIndicator<HashMap<String, User>> t = new GenericTypeIndicator<HashMap<String, User>>() {};

                Query query = databaseReference.child("users").orderByChild("phone").equalTo(phone);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        llActions.setAlpha(1);
                        llActions.setEnabled(true);

                        if (dataSnapshot.exists()) {
                            HashMap<String, User> hashMap = dataSnapshot.getValue(t);
                            for (Map.Entry<String, User> entry : hashMap.entrySet()) {
                                user = entry.getValue();
                            }
                            loadUser(); //Загружаем найденного пользователя
                        } else {
                            loadUnknownUser(); //Загружаем неизвестного пользователя
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Обрабатываем ошибку, если произошла
                    }
                });

            }

        }

        //Написать человеку
        btType.setOnClickListener(view -> {
            if (intent != null && intent.hasExtra("fromChat")) {
                finish();
                return;
            }

            btType.setEnabled(false);
            Intent intent1 = new Intent(getBaseContext(), ChatActivity.class);
            intent1.putExtra("id", user.getId());
            intent1.putExtra("currentUserId", currentUser.getId());
            intent1.putExtra("name", user.getName());

            // Проверяем создан ли уже чат с этим человеком
            databaseReference.child("users").child(currentUser.getId()).child("chats").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Получаем список всех чатов пользователя
                    ArrayList<String> chats = new ArrayList<>();
                    for (DataSnapshot chatSnapshot : dataSnapshot.getChildren()) {
                        String chatId = chatSnapshot.getKey();
                        chats.add(chatId);
                    }
                    // Проверяем каждый чат на наличие userId
                    if(chats.size() == 0){ //Чатов нету
                        startActivity(intent1);
                        finish();
                        return;
                    }
                    for (int i = 0; i < chats.size(); i++) {
                        String chatId = chats.get(i);
                        int finalI = i;
                        databaseReference.child("chats").child(chatId).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(!foundChat) {
                                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                        String userIdInChat = userSnapshot.getValue(String.class);
                                        System.out.println(chatId + ", " + userIdInChat + ":" + userId);
                                        if (userIdInChat != null && userIdInChat.equals(userId)) {
                                            // Нашли чат, содержащий userId
                                            foundChat = true;
                                            break;
                                        }
                                    }
                                    // Если userId не найден в текущем чате, то проверяем следующий чат
                                    System.out.println("chatFound: " + foundChat);
                                    System.out.println("chatII: " + finalI);
                                    if (foundChat) {
                                        // Нашли существующий чат, стартуем с chatId
                                        System.out.println("chatId: " + chatId);
                                        intent1.putExtra("chatId", chatId);
                                        startActivity(intent1);
                                        finish();
                                    }
                                }else if (finalI + 1 > chats.size()) {
                                    startActivity(intent1);
                                    finish();
                                }

                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) { btType.setEnabled(true); }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    btType.setEnabled(true);
                }
            });

        });

    }

    private void loadUnknownUser(){
        Intent intent = getIntent();
        if(intent.hasExtra("name")) {
            String name = intent.getStringExtra("name");
            tvName.setText(name);
            tvAvatar.setText(String.valueOf(name.toUpperCase().charAt(0)));
        }else{
            String name = getResources().getString(R.string.user_invalid);
            tvAvatar.setText(String.valueOf(name.toUpperCase().charAt(0)));
        }

        tvPhone.setText(phone);

        tvAvatar.setVisibility(View.VISIBLE);
        tvName.setVisibility(View.VISIBLE);
        llPhone.setVisibility(View.VISIBLE);
        llLoading.setVisibility(View.GONE);
        llProfileNotRegistered.setVisibility(View.VISIBLE);

        llType.setAlpha(0.6f);
        btCall.setEnabled(true);
    }

    private void loadUser(){
        if(user != null){
            userId = user.getId();
            String name = user.getName() + "";
            tvName.setText(name);
            tvPhone.setText(user.getPhone());
            tvAvatar.setText(String.valueOf(name.toUpperCase().charAt(0)));
            if(user.getInfo() != null) tvAbout.setText(user.getInfo());

            Drawable backgroundDrawable = llAvatar.getBackground();
            backgroundDrawable.setTint(Color.parseColor(user.getAvatarColor()));

            tvAvatar.setVisibility(View.VISIBLE);
            tvName.setVisibility(View.VISIBLE);
            llPhone.setVisibility(View.VISIBLE);
            llAbout.setVisibility(View.VISIBLE);
            llLoading.setVisibility(View.GONE);

            if(currentUser.getId().equals(user.getId())){
                llType.setAlpha(0.6f);
            }else{
                btType.setEnabled(true);
            }

            btCall.setEnabled(true);
        }else{
            loadUnknownUser();
        }
    }

}