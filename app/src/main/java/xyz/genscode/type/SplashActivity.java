package xyz.genscode.type;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import xyz.genscode.type.models.User;

public class SplashActivity extends AppCompatActivity {

    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ImageView ivLogo = findViewById(R.id.ivLogo);
        TextView tvLogo = findViewById(R.id.tvLogo);

        FirebaseApp.initializeApp(/*context=*/ this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance());

        handler = new Handler();

        ivLogo.setScaleX(1.8f);
        ivLogo.setScaleY(1.8f);
        handler.postDelayed(() -> {
            ivLogo.animate().scaleX(1f).scaleY(1f).setDuration(100);

            handler.postDelayed(() -> {
                tvLogo.setVisibility(View.VISIBLE);

                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                if(firebaseUser != null){
                    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                    DatabaseReference databaseReference = firebaseDatabase.getReference().child("users");

                    databaseReference.child(firebaseUser.getUid()).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            User user = task.getResult().getValue(User.class);
                            if(user != null){
                                goToMainActivity(user);
                            }else{
                                goToSignInActivity();
                            }
                        } else {
                            //CONNECT ERROR
                        }
                    });

                    /*
                    Query query = databaseReference.orderByChild("id").equalTo(firebaseUser.getUid());

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                User user = dataSnapshot.getValue(User.class);
                                goToMainActivity(user);
                            } else {
                                goToSignInActivity();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            //ERROR CONECTION
                        }
                    });

                     */
                }else{
                    goToSignInActivity();
                }

            }, 50);
        }, 250);


        AnimationDrawable animationDrawable = (AnimationDrawable) ivLogo.getBackground();
        animationDrawable.setEnterFadeDuration(500);
        animationDrawable.setExitFadeDuration(1000);
        animationDrawable.start();
    }

    private void goToSignInActivity(){
        handler.postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, SignInActivity.class));
            super.finish();
        }, 1500);
    }

    private void goToMainActivity(User user){
        handler.postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            intent.putExtra("user", user);
            startActivity(intent);
            super.finish();
        }, 1500);
    }
}