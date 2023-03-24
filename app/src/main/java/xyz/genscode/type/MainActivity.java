package xyz.genscode.type;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.transition.Fade;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import xyz.genscode.type.data.SettingsHandler;
import xyz.genscode.type.dialog.Dialog;
import xyz.genscode.type.dialog.Toast;
import xyz.genscode.type.fragments.ContactsFragment;
import xyz.genscode.type.fragments.MessengerFragment;
import xyz.genscode.type.fragments.SettingsFragment;
import xyz.genscode.type.models.User;

public class MainActivity extends AppCompatActivity {

    public Dialog dialog;
    public Toast toast;

    public static final int MESSENGER_FRAGMENT = 0;
    public static final int CONTACTS_FRAGMENT = 1;
    public static final int SETTINGS_FRAGMENT = 2;
    boolean contacts_initialized = false;
    boolean messenger_initialized = false;
    boolean settings_initialized = false;
    User user;
    TextView tvTopIndicator;
    Button btBottomMessenger; ImageView ivBottomMessenger;
    Button btBottomContacts; ImageView ivBottomContacts;
    Button btBottomSettings; ImageView ivBottomSettings;
    View llBottomIndicator; int screenWidth;
    View fcvMessenger; View fcvContacts; View fcvSettings; static int currentFragment;
    ValueAnimator animatorXMessenger; ValueAnimator animatorXContacts; ValueAnimator animatorXSettings;

    static boolean hello = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        overridePendingTransition(0, 0);

        getWindow().setEnterTransition(new Fade());
        getWindow().setExitTransition(new Fade());
        getWindow().setReenterTransition(new Fade());
        getWindow().setReturnTransition(new Fade());

        ViewGroup rootView = findViewById(android.R.id.content);
        Animation enterAnimation = AnimationUtils.loadAnimation(this, R.anim.jump);
        rootView.startAnimation(enterAnimation);

        Intent intent = getIntent();
        if(intent == null) finish();

        if (intent != null) {
            user = (User) intent.getSerializableExtra("user");
        }

        //Подгружаем настройки
        SettingsHandler.getInstance().getSettings(getApplicationContext());

        createDialog();
        createToast();

        tvTopIndicator = findViewById(R.id.tvTopIndicator);
        btBottomMessenger = findViewById(R.id.btMessenger);
        btBottomContacts = findViewById(R.id.btContacts);
        btBottomSettings = findViewById(R.id.btSettings);
        ivBottomMessenger = findViewById(R.id.ivMessenger);
        ivBottomContacts = findViewById(R.id.ivContacts);
        ivBottomSettings = findViewById(R.id.ivSettings);
        llBottomIndicator = findViewById(R.id.llBottomIndicator);

        btBottomMessenger.setOnClickListener(view -> navigate("messenger"));
        btBottomContacts.setOnClickListener(view -> navigate("contacts"));
        btBottomSettings.setOnClickListener(view -> navigate("settings"));

        fcvMessenger = findViewById(R.id.fcvMessenger);
        fcvContacts = findViewById(R.id.fcvContacts);
        fcvSettings = findViewById(R.id.fcvSettings);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;

        initializeSettings();
        initializeMessenger();
        switch (currentFragment){
            case CONTACTS_FRAGMENT:
                navigate("contacts");
                break;
            case SETTINGS_FRAGMENT:
                navigate("settings");
                break;
            default:
                navigate("messenger");
                break;
        }

        setThemeForActivity(-1);

        //Приветствие
        if(!hello) {
            hello = true;
            toast.show(getResources().getString(R.string.hello) + " " + user.getName() + "! 👋");
        }

    }

    public void navigate(String menu){
        if(menu.equals("messenger")){

            if(animatorXMessenger != null){
                if(animatorXMessenger.isRunning()) return;
            }

            int startX = (int) llBottomIndicator.getX();
            int endX = (int) (screenWidth * 0.128f);
            animatorXMessenger = ValueAnimator.ofInt(startX, endX); animatorXMessenger.setDuration(250);
            animatorXMessenger.addUpdateListener(valueAnimator -> llBottomIndicator.setX((int) valueAnimator.getAnimatedValue()));
            animatorXMessenger.start();

            fcvContacts.setVisibility(View.INVISIBLE); fcvSettings.setVisibility(View.INVISIBLE); fcvMessenger.setVisibility(View.VISIBLE);

            ivBottomMessenger.setColorFilter(ContextCompat.getColor(this, R.color.type3));
            ivBottomContacts.setColorFilter(ContextCompat.getColor(this, R.color.grey_300));
            ivBottomSettings.setColorFilter(ContextCompat.getColor(this, R.color.grey_300));
            tvTopIndicator.setText(getResources().getString(R.string.messenger));

            currentFragment = MESSENGER_FRAGMENT;

            System.out.println("Navigator: navigate to Messenger");

        }
        if(menu.equals("contacts")){

            if(animatorXContacts != null){
                if(animatorXContacts.isRunning()) return;
            }

            int startX = (int) llBottomIndicator.getX();
            int endX = (int) (screenWidth * 0.427f);
            animatorXContacts = ValueAnimator.ofInt(startX, endX); animatorXContacts.setDuration(250);
            animatorXContacts.addUpdateListener(valueAnimator -> llBottomIndicator.setX((int) valueAnimator.getAnimatedValue()));
            animatorXContacts.start();

            fcvMessenger.setVisibility(View.INVISIBLE); fcvSettings.setVisibility(View.INVISIBLE); fcvContacts.setVisibility(View.VISIBLE);

            initializeContacts();

            ivBottomMessenger.setColorFilter(ContextCompat.getColor(this, R.color.grey_300));
            ivBottomContacts.setColorFilter(ContextCompat.getColor(this, R.color.type3));
            ivBottomSettings.setColorFilter(ContextCompat.getColor(this, R.color.grey_300));
            tvTopIndicator.setText(getResources().getString(R.string.contacts));

            currentFragment = CONTACTS_FRAGMENT;

            System.out.println("Navigator: navigate to Contacts");

        }
        if(menu.equals("settings")){

            if(animatorXSettings != null){
                if(animatorXSettings.isRunning()) return;
            }

            int startX = (int) llBottomIndicator.getX();
            int endX = (int) (screenWidth * 0.731f);

            animatorXSettings = ValueAnimator.ofInt(startX, endX); animatorXSettings.setDuration(250);
            animatorXSettings.addUpdateListener(valueAnimator -> llBottomIndicator.setX((int) valueAnimator.getAnimatedValue()));
            animatorXSettings.start();

            fcvMessenger.setVisibility(View.INVISIBLE); fcvContacts.setVisibility(View.INVISIBLE); fcvSettings.setVisibility(View.VISIBLE);

            ivBottomMessenger.setColorFilter(ContextCompat.getColor(this, R.color.grey_300));
            ivBottomContacts.setColorFilter(ContextCompat.getColor(this, R.color.grey_300));
            ivBottomSettings.setColorFilter(ContextCompat.getColor(this, R.color.type3));
            tvTopIndicator.setText(getResources().getString(R.string.settings));

            currentFragment = SETTINGS_FRAGMENT;

            System.out.println("Navigator: navigate to Settings");

        }
    }

    public void initializeContacts(){
        if(!contacts_initialized) {
            contacts_initialized = true;
            ContactsFragment fragment = new ContactsFragment();
            Bundle args = new Bundle();
            args.putSerializable(SettingsFragment.ARG_USER, user);
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fcvContacts, fragment)
                    .commit();
        }
    }

    public void initializeMessenger(){
        if(!messenger_initialized) {
            messenger_initialized = true;
            MessengerFragment fragment = new MessengerFragment();
            Bundle args = new Bundle();
            args.putSerializable(SettingsFragment.ARG_USER, user);
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fcvMessenger, fragment)
                    .commit();
        }
    }

    public void initializeSettings(){
        if(!settings_initialized) {
            settings_initialized = true;
            SettingsFragment fragment = new SettingsFragment();
            Bundle args = new Bundle();
            args.putSerializable(SettingsFragment.ARG_USER, user);
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fcvSettings, fragment)
                    .commit();
        }
    }

    public void setThemeForActivity(int mode){
        int theme;
        if(mode == -1) { //Если передаем -1, тема ставится автоматически по настройкам
            theme = SettingsHandler.getInstance().theme;
        }else{
            theme = mode;
        }

        switch (theme) {
            case SettingsHandler.THEME_SYSTEM:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case SettingsHandler.THEME_NIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case SettingsHandler.THEME_DAY:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if(dialog.messageActive == 1){
            dialog.hideMessage();
            return;
        }
        super.onBackPressed();
    }

    private void createToast(){
        View llIncludeToast = findViewById(R.id.llIncludeToast);
        View llToast = findViewById(R.id.llToast);
        TextView tvToastText = findViewById(R.id.tvToastText);
        toast = new Toast(llIncludeToast, llToast, tvToastText);
    }

    private void createDialog(){
        TextView messageHeader = findViewById(R.id.message_tvHeader);
        TextView messageContent = findViewById(R.id.message_tvContent);
        Button messageButton1 = findViewById(R.id.message_bt);
        Button messageButton2 = findViewById(R.id.message_bt2);
        View messageInclude = findViewById(R.id.llIncludeMessage);
        View messageBody = findViewById(R.id.message_body);

        dialog = new Dialog(messageHeader, messageContent, messageButton1, messageButton2, messageInclude, messageBody);
    }

}