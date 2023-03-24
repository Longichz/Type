package xyz.genscode.type;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import xyz.genscode.type.fragments.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    //SettingsActivity содержит в себе все настройки которые находятся во фрагментах fragments/settings/
    //в SettingsActivity доступна навигация по этим настройкам - naviate(SettignsFragment.SETTINGS_ABOUT) и т.д.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Intent intent = getIntent();
        if(!intent.hasExtra("settings")) finish();

        int settings = intent.getIntExtra("settings", 0);

        navigate(settings);

        //Назад
        findViewById(R.id.llSettingsBack).setOnClickListener(view -> finish());
    }

    public void navigate(int settings){
        TextView tvHeader = findViewById(R.id.tvSettingsTopIndicator) ;
        FragmentContainerView fcvAbout = findViewById(R.id.fcvAbout);
        FragmentContainerView fcvDesign = findViewById(R.id.fcvDesign);

        switch (settings){
            case SettingsFragment.SETTINGS_ABOUT:
                fcvAbout.setVisibility(View.VISIBLE);
                tvHeader.setText(getResources().getString(R.string.settings_app));
                break;
            case SettingsFragment.SETTINGS_DESIGN:
                fcvDesign.setVisibility(View.VISIBLE);
                tvHeader.setText(getResources().getString(R.string.design_header));
                break;
            default:
                break;
        }
    }
}