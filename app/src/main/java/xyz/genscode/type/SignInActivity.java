package xyz.genscode.type;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.transition.Fade;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import xyz.genscode.type.models.User;
import xyz.genscode.type.utils.AvatarColors;

public class SignInActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    Handler handler;
    PhoneNumberFormattingTextWatcher phoneNumberFormattingTextWatcher;
    TextWatcher textWatcher;
    EditText etPhone;
    boolean receiveCode = false;
    String phone;
    HashMap<String, Long> sendPhones = new HashMap<>();
    View authMenu;
    private String mVerificationId;


    FirebaseDatabase database;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        overridePendingTransition(0, 0);

        getWindow().setEnterTransition(new Fade());
        getWindow().setExitTransition(new Fade());
        getWindow().setReenterTransition(new Fade());
        getWindow().setReturnTransition(new Fade());

        ViewGroup rootView = findViewById(android.R.id.content);
        Animation enterAnimation = AnimationUtils.loadAnimation(this, R.anim.jump);
        rootView.startAnimation(enterAnimation);

        handler = new Handler();

        Animation anim = new AlphaAnimation(0.5f, 1.0f); anim.setDuration(250); anim.setRepeatMode(Animation.REVERSE); anim.setRepeatCount(Animation.INFINITE);
        Animation anim2 = new AlphaAnimation(0.5f, 1.0f); anim2.setDuration(250); anim2.setRepeatMode(Animation.REVERSE); anim2.setRepeatCount(Animation.INFINITE);
        Animation anim3 = new AlphaAnimation(0.5f, 1.0f); anim3.setDuration(250); anim3.setRepeatMode(Animation.REVERSE); anim3.setRepeatCount(Animation.INFINITE);
        ImageView loading1 = findViewById(R.id.ivLoading1); loading1.startAnimation(anim);
        ImageView loading2 = findViewById(R.id.ivLoading2); handler.postDelayed(() -> loading2.startAnimation(anim2), 125);
        ImageView loading3 = findViewById(R.id.ivLoading3); handler.postDelayed(() -> loading3.startAnimation(anim3), 250);

        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference().child("users");
        mAuth = FirebaseAuth.getInstance();
        mAuth.setLanguageCode("ru");

        authMenu = findViewById(R.id.llAuthMenu);

        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if(firebaseUser != null){
            finalAuth(firebaseUser);
        }

        ImageView ivLogo = findViewById(R.id.ivLogo);

        AnimationDrawable animationDrawable = (AnimationDrawable) ivLogo.getBackground();
        animationDrawable.setEnterFadeDuration(500);
        animationDrawable.setExitFadeDuration(1000);
        animationDrawable.start();

        etPhone = findViewById(R.id.etPhone);
        Button btGetSms = findViewById(R.id.btGetSms);

        phoneNumberFormattingTextWatcher = new PhoneNumberFormattingTextWatcher();
        textWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override public void afterTextChanged(Editable editable) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(receiveCode){
                    if(charSequence.length() == 6){
                        if(mVerificationId != null) {
                            auth();
                        }else{
                            editPhoneNumber();
                        }
                    }
                }else {
                    if (charSequence.length() >= 10) {
                        btGetSms.setEnabled(true);
                        btGetSms.animate().alpha(1).setDuration(100);
                    } else {
                        btGetSms.setEnabled(false);
                        btGetSms.animate().alpha(0.6f).setDuration(100);
                    }
                }
            }
        };

        networkState();

    }

    private void auth(){
        signInWithPhoneAuthCredential(PhoneAuthProvider.getCredential(mVerificationId, etPhone.getText().toString()));
    }

    private void receiveCode(){
        receiveCode = true;

        Button btGetSms = findViewById(R.id.btGetSms);

        TextView tvPolicyPrivacy = findViewById(R.id.tvPolicyPrivacy);
        TextView tvSignIn = findViewById(R.id.tvSignIn);
        TextView tvCountryCode = findViewById(R.id.tvCountryCode);

        etPhone.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6) {}});
        etPhone.removeTextChangedListener(phoneNumberFormattingTextWatcher);
        etPhone.setText("");

        tvCountryCode.setVisibility(View.GONE);
        btGetSms.setVisibility(View.GONE);
        tvPolicyPrivacy.setVisibility(View.GONE);

        tvSignIn.setText(getResources().getString(R.string.type_sms));
    }

    private void editPhoneNumber(){
        receiveCode = false;

        authMenu.setVisibility(View.VISIBLE);

        ImageView ivEditPhoneNumber = findViewById(R.id.ivEditPhoneNumber);
        Button btGetSms = findViewById(R.id.btGetSms);

        TextView tvSignIn = findViewById(R.id.tvSignIn);
        TextView tvCountryCode = findViewById(R.id.tvCountryCode);

        etPhone.setHint(getResources().getString(R.string.phone));
        etPhone.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15) {}});
        etPhone.addTextChangedListener(phoneNumberFormattingTextWatcher);
        if(phone != null) {
            etPhone.setText(phone);
            btGetSms.setEnabled(true);
            btGetSms.animate().alpha(1).setDuration(100);
        }else{
            etPhone.setText("");
            btGetSms.setEnabled(false);
            btGetSms.animate().alpha(0.6f).setDuration(100);
        }
        etPhone.setSelection(etPhone.length());
        etPhone.setEnabled(true);
        etPhone.addTextChangedListener(textWatcher);
        etPhone.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_CORRECT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_CLASS_PHONE);
        etPhone.setVisibility(View.VISIBLE);
        View llPhone = findViewById(R.id.llPhone);
        llPhone.setVisibility(View.VISIBLE);
        btGetSms.setText(getResources().getString(R.string.get_sms));


        tvCountryCode.setVisibility(View.VISIBLE);
        btGetSms.setVisibility(View.VISIBLE);
        ivEditPhoneNumber.setVisibility(View.GONE);

        tvSignIn.setText(getResources().getString(R.string.sign_in));

        ivEditPhoneNumber.setOnClickListener(view -> editPhoneNumber());

        btGetSms.setOnClickListener(view -> {
            phone = etPhone.getText().toString().replaceAll("\\D", "");
            sendCode(phone);
            receiveCode();
        });
    }

    private void editName(){
        authMenu.setVisibility(View.VISIBLE);
        authMenu.animate().alpha(1).setDuration(250);

        receiveCode = false;

        ImageView ivEditPhoneNumber = findViewById(R.id.ivEditPhoneNumber);
        Button btGetSms = findViewById(R.id.btGetSms);

        TextView tvPolicyPrivacy = findViewById(R.id.tvPolicyPrivacy);
        TextView tvSignIn = findViewById(R.id.tvSignIn);
        TextView tvCountryCode = findViewById(R.id.tvCountryCode);

        etPhone.setHint(getResources().getString(R.string.type_name_et));
        etPhone.setFilters(new InputFilter[]{new InputFilter.LengthFilter(32) {}});
        etPhone.removeTextChangedListener(phoneNumberFormattingTextWatcher);
        etPhone.setText("");
        etPhone.setEnabled(true);
        etPhone.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_CORRECT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        etPhone.removeTextChangedListener(textWatcher);

        btGetSms.setEnabled(true);
        btGetSms.setText(getResources().getString(R.string.apply));
        btGetSms.animate().alpha(1).setDuration(100);

        btGetSms.setVisibility(View.VISIBLE);
        ivEditPhoneNumber.setVisibility(View.VISIBLE);
        tvPolicyPrivacy.setVisibility(View.GONE);
        tvCountryCode.setVisibility(View.GONE);



        tvSignIn.setText(getResources().getString(R.string.type_name));

        ivEditPhoneNumber.setOnClickListener(view -> {
            editPhoneNumber();
            sendPhones.clear();
        });

        btGetSms.setOnClickListener(view -> {
            if(etPhone.getText().length() < 1){
                tvPolicyPrivacy.setText(getResources().getString(R.string.invalid_name));
                tvPolicyPrivacy.setTextColor(getResources().getColor(R.color.red));
                tvPolicyPrivacy.setVisibility(View.VISIBLE);
                Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                if(vibrator.hasVibrator()){
                    vibrator.vibrate(20);
                    handler.postDelayed((Runnable) () -> {
                        vibrator.vibrate(20);
                    }, 150);
                }
            }else{
                btGetSms.setEnabled(false);

                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                assert firebaseUser != null;

                User user = new User();
                user.setId(firebaseUser.getUid());
                user.setPhone(firebaseUser.getPhoneNumber());
                user.setName(etPhone.getText().toString().trim());
                user.setAvatarColor(AvatarColors.getRandomAvatarColor());

                databaseReference.child(firebaseUser.getUid()).setValue(user).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        goToMainActivity(user);
                    }else{
                        //ERROR CONNECTION
                    }
                });
            }
        });
    }

    private void waitingForNetwork(){
        ImageView ivEditPhoneNumber = findViewById(R.id.ivEditPhoneNumber);
        Button btGetSms = findViewById(R.id.btGetSms);
        TextView tvSignIn = findViewById(R.id.tvSignIn);
        ivEditPhoneNumber.setVisibility(View.GONE);
        btGetSms.setVisibility(View.GONE);

        View llPhone = findViewById(R.id.llPhone);
        llPhone.setVisibility(View.GONE);

        tvSignIn.setText(getResources().getString(R.string.waiting_for_network));
    }

    @SuppressWarnings("ConstantConditions")
    private void sendCode(String phn){
        if(sendPhones.containsKey(phn)){
            if(System.currentTimeMillis() - sendPhones.get(phn) > 60000){
                sendPhones.clear();
                sendPhones.put(phn, System.currentTimeMillis());
                sendCode(phn, "+7");
            }else{
                etPhone.setEnabled(true);
                etPhone.setHint(getResources().getString(R.string.type_sms_et));
                ImageView ivEditPhoneNumber = findViewById(R.id.ivEditPhoneNumber);
                ivEditPhoneNumber.setVisibility(View.VISIBLE);
            }
        }else{
            sendPhones.clear();
            sendPhones.put(phn, System.currentTimeMillis());
            sendCode(phn, "+7");
        }
    }

    private void sendCode(String phn, String code){

        etPhone.setEnabled(false);
        etPhone.setHint(getResources().getString(R.string.loading));

        String phone = code + phn;

        PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

                TextView tvPolicyPrivacy = findViewById(R.id.tvPolicyPrivacy);
                tvPolicyPrivacy.setVisibility(View.VISIBLE);

                Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                if(vibrator.hasVibrator()){
                    vibrator.vibrate(20);
                    handler.postDelayed(() -> {
                        vibrator.vibrate(20);
                    }, 150);
                }

                editPhoneNumber();
                sendPhones.clear();

            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;

                etPhone.setEnabled(true);
                etPhone.setHint(getResources().getString(R.string.type_sms_et));
                etPhone.requestFocus();
                InputMethodManager imm = (InputMethodManager)
                        getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(etPhone, InputMethodManager.SHOW_IMPLICIT);
                ImageView ivEditPhoneNumber = findViewById(R.id.ivEditPhoneNumber);
                ivEditPhoneNumber.setVisibility(View.VISIBLE);
            }

        };

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phone)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        TextView tvPolicyPrivacy = findViewById(R.id.tvPolicyPrivacy);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();
                        authMenu.animate().alpha(0).setDuration(250);
                        etPhone.setEnabled(false);

                        assert user != null;
                        finalAuth(user);
                    } else {

                        Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                        if(vibrator.hasVibrator()){
                            vibrator.vibrate(20);
                            handler.postDelayed((Runnable) () -> {
                                vibrator.vibrate(20);
                            }, 150);
                        }

                        tvPolicyPrivacy.setVisibility(View.VISIBLE);
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            tvPolicyPrivacy.setText(getResources().getString(R.string.invalid_code));
                            tvPolicyPrivacy.setTextColor(getResources().getColor(R.color.red));
                            etPhone.setText("");
                        }else{
                            tvPolicyPrivacy.setText(getResources().getString(R.string.error_auth));
                            tvPolicyPrivacy.setTextColor(getResources().getColor(R.color.red));
                        }
                    }
                });
    }

    private void finalAuth(FirebaseUser firebaseUser) {
        TextView tvPolicyPrivacy = findViewById(R.id.tvPolicyPrivacy);
        tvPolicyPrivacy.setVisibility(View.GONE);

        databaseReference.child(firebaseUser.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                User user = task.getResult().getValue(User.class);
                if(user != null){
                    goToMainActivity(user);
                }else{
                    editName();
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
                    editName();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //ERROR CONECTION
            }
        });

         */

    }

    private void goToMainActivity(User user){
        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
        super.finish();
    }

    @Override
    public void onBackPressed() {
        if(receiveCode){
            editPhoneNumber();
            return;
        }
        super.onBackPressed();
    }

    public void networkState(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        cm.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                runOnUiThread(() -> {
                    editPhoneNumber();
                });
            }

            @Override
            public void onLost(Network network) {
                runOnUiThread(() -> {
                    waitingForNetwork();
                });

            }

        });
    }
}