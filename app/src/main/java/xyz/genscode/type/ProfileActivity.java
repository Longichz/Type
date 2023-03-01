package xyz.genscode.type;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import xyz.genscode.type.models.Chat;
import xyz.genscode.type.models.User;

public class ProfileActivity extends AppCompatActivity {

    Context context;
    User user;
    User currentUser;
    String userId, phone;
    View llPhone, llAbout, llAvatar, llLoading, llActions, llProfileNotRegistered, llCall, llType, llBack;
    TextView tvName, tvPhone, tvAbout, tvAvatar;
    Button btType, btCall;

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

        context = getApplicationContext();

        Intent intent = getIntent();
        if(intent == null || (!intent.hasExtra("phone") && !intent.hasExtra("currentUser"))) finish();

        Handler handler = new Handler();

        Animation anim = new AlphaAnimation(0.5f, 1.0f); anim.setDuration(250); anim.setRepeatMode(Animation.REVERSE); anim.setRepeatCount(Animation.INFINITE);
        Animation anim2 = new AlphaAnimation(0.5f, 1.0f); anim2.setDuration(250); anim2.setRepeatMode(Animation.REVERSE); anim2.setRepeatCount(Animation.INFINITE);
        Animation anim3 = new AlphaAnimation(0.5f, 1.0f); anim3.setDuration(250); anim3.setRepeatMode(Animation.REVERSE); anim3.setRepeatCount(Animation.INFINITE);
        ImageView loading1 = findViewById(R.id.ivProfileLoading1); loading1.startAnimation(anim);
        ImageView loading2 = findViewById(R.id.ivProfileLoading2); handler.postDelayed(() -> loading2.startAnimation(anim2), 125);
        ImageView loading3 = findViewById(R.id.ivProfileLoading3); handler.postDelayed(() -> loading3.startAnimation(anim3), 250);

        llBack = findViewById(R.id.llProfileBack);
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

        currentUser = (User) intent.getSerializableExtra("currentUser");

        if(intent.hasExtra("userId")) {
            databaseReference.child("users").child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(task.isSuccessful()){
                        user = task.getResult().getValue(User.class);
                        if(user != null){
                            userId = user.getId();

                            tvName.setText(user.getName());
                            tvPhone.setText(user.getPhone());
                            tvAvatar.setText(String.valueOf(user.getName().toUpperCase().charAt(0)));
                            if(user.getInfo() != null) tvAbout.setText(user.getInfo());

                            Drawable backgroundDrawable = llAvatar.getBackground();
                            backgroundDrawable.setTint(Color.parseColor(user.getAvatarColor()));

                            llActions.setAlpha(1);
                            llActions.setEnabled(true);
                            btCall.setEnabled(true);
                            btType.setEnabled(true);
                            tvName.setVisibility(View.VISIBLE);
                            llPhone.setVisibility(View.VISIBLE);
                            llAbout.setVisibility(View.VISIBLE);
                            llLoading.setVisibility(View.GONE);
                        }
                    }
                }
            });
        }

        if(intent.hasExtra("phone")) {
            phone = intent.getStringExtra("phone");

            GenericTypeIndicator<HashMap<String, User>> t = new GenericTypeIndicator<HashMap<String, User>>() {};

            Query query = databaseReference.child("users").orderByChild("phone").equalTo(phone);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    llActions.setAlpha(1);
                    llActions.setEnabled(true);

                    if (dataSnapshot.exists()) {
                        HashMap<String, User> hashMap = dataSnapshot.getValue(t);
                        for (Map.Entry<String, User> entry : hashMap.entrySet()) {
                            user = entry.getValue();
                        }

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
                        }
                    } else {
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
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Обрабатываем ошибку, если произошла
                }
            });

            llBack.setOnClickListener(view -> finish());

            btType.setOnClickListener(view -> {
                btType.setEnabled(false);
                Intent intent1 = new Intent(getBaseContext(), ChatActivity.class);
                intent1.putExtra("id", user.getId());
                intent1.putExtra("currentUserId", currentUser.getId());
                intent1.putExtra("name", user.getName());

                databaseReference.child("users").child(currentUser.getId()).child("chats").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Получаем список всех чатов пользователя
                        ArrayList<String> chats = new ArrayList<>();
                        for (DataSnapshot chatSnapshot : dataSnapshot.getChildren()) {
                            String chatId = chatSnapshot.getValue(String.class);
                            chats.add(chatId);
                        }
                        // Проверяем каждый чат на наличие userId

                        for (String chatId : chats) {
                            databaseReference.child("chats").child(chatId).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    // Проверяем каждого пользователя в чате на наличие userId
                                    boolean foundChat = false;
                                    boolean foundUser = false;
                                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                        String userIdInChat = userSnapshot.getValue(String.class);
                                        System.out.println(userIdInChat + " = " + userId);
                                        if (userIdInChat.equals(userId)) {
                                            // Нашли чат, содержащий userId
                                            foundChat = true;
                                            foundUser = true;

                                            System.out.println(chatId);
                                            intent1.putExtra("chatId", chatId);
                                            startActivity(intent1);
                                            break;
                                        }
                                    }
                                    // Если userId не найден в текущем чате, то проверяем следующий чат
                                    if (!foundUser && !foundChat && chatId.equals(chats.get(chats.size() - 1))) {
                                        // Не нашли ни один чат, содержащий userId, стартуем без chatId
                                        startActivity(intent1);
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // Обработка ошибок
                                    btType.setEnabled(true);
                                }
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



    }

}