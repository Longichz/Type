package xyz.genscode.type;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
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

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    long time;
    Handler handler;
    static boolean isActivityCreated = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        time = System.currentTimeMillis();

        ImageView ivLogo = findViewById(R.id.ivLogo);
        TextView tvLogo = findViewById(R.id.tvLogo);

        handler = new Handler();

        ivLogo.setScaleX(1.8f);
        ivLogo.setScaleY(1.8f);
        handler.postDelayed(() -> {
            ivLogo.animate().scaleX(1f).scaleY(1f).setDuration(100);
            handler.postDelayed(() -> tvLogo.setVisibility(View.VISIBLE), 50);
        }, 250);

        AnimationDrawable animationDrawable = (AnimationDrawable) ivLogo.getBackground();
        animationDrawable.setEnterFadeDuration(500);
        animationDrawable.setExitFadeDuration(1000);
        animationDrawable.start();

        if(!isActivityCreated) {

            isActivityCreated = true;

            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

            if (firebaseUser != null) {
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference databaseReference = firebaseDatabase.getReference().child("users");

                databaseReference.child(firebaseUser.getUid()).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        User user = task.getResult().getValue(User.class);
                        if (user != null) {
                            goToMainActivity(user);
                        } else {
                            goToSignInActivity();
                        }
                    } else {
                        //CONNECT ERROR
                        isActivityCreated = false;
                        handler.postDelayed((Runnable) this::recreate, 2000);
                    }
                });

            } else {
                goToSignInActivity();
            }
        }
    }

    private void goToSignInActivity(){
        startActivity(new Intent(SplashActivity.this, SignInActivity.class));
        super.finish();
    }

    private void goToMainActivity(User user){
        System.out.println("Splash time: "+(System.currentTimeMillis()-time));
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
        super.finish();
    }
}