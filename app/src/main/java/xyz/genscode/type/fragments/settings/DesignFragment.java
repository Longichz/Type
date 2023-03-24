package xyz.genscode.type.fragments.settings;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import xyz.genscode.type.R;
import xyz.genscode.type.data.SettingsHandler;

public class DesignFragment extends Fragment {

    View view;
    Context context;

    SharedPreferences settings;
    SharedPreferences.Editor settingsEditor;

    private int currentTheme = 0;
    private boolean optimization_blur = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getContext();
        assert context!=null;

        //Загружаем Storage
        settings = context.getSharedPreferences(SettingsHandler.STORAGE_NAME, MODE_PRIVATE);
        settingsEditor = settings.edit();

        //Принимаем настройки
        currentTheme = SettingsHandler.getInstance().theme;
        optimization_blur = SettingsHandler.getInstance().optimization_blur;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_design, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Устанавливаем логику для всех блоков
        setThemeSettings();
        setOptimizationSettings();

        //Обновляем блоки в Ui
        updateThemeUi();
        updateOptimizationUi();
    }


    //Тема
    //Тема
    //Тема
    public void setThemeSettings(){
        Button btSystem = view.findViewById(R.id.btSettingsDesignThemeSystem);
        Button btNight = view.findViewById(R.id.btSettingsDesignThemeNight);
        Button btDay = view.findViewById(R.id.btSettingsDesignThemeDay);

        //Нажали кнопку системная версия
        btSystem.setOnClickListener(view1 -> {
            //Устанавливаем тему для приложения
            SettingsHandler.getInstance().theme = SettingsHandler.THEME_SYSTEM;

            updateThemeUi(); //Обновляем Ui (Выбора темы)

            //Сохраняем
            settingsEditor.putInt(SettingsHandler.THEME, SettingsHandler.getInstance().theme);
            settingsEditor.commit();

            setTheme(-1);
        });

        //Нажали кнопку ночная версия
        btNight.setOnClickListener(view1 -> {
            SettingsHandler.getInstance().theme = SettingsHandler.THEME_NIGHT;
            updateThemeUi();

            settingsEditor.putInt(SettingsHandler.THEME, SettingsHandler.getInstance().theme);
            settingsEditor.commit();

            setTheme(-1);
        });

        //Нажали кнопку дневная версия
        btDay.setOnClickListener(view1 -> {
            SettingsHandler.getInstance().theme = SettingsHandler.THEME_DAY;
            updateThemeUi();

            settingsEditor.putInt(SettingsHandler.THEME, SettingsHandler.getInstance().theme);
            settingsEditor.commit();

            setTheme(-1);
        });
    }

    public void updateThemeUi(){
        //Обновляем отображение темы в разделе тем
        currentTheme = SettingsHandler.getInstance().theme;

        View llSystemInd = view.findViewById(R.id.llSettingsDesignThemeSystemIndicator);
        View llNightInd = view.findViewById(R.id.llSettingsDesignThemeNightIndicator);
        View llDayInd = view.findViewById(R.id.llSettingsDesignThemeDayIndicator);

        llSystemInd.setVisibility(View.INVISIBLE);
        llNightInd.setVisibility(View.INVISIBLE);
        llDayInd.setVisibility(View.INVISIBLE);

        switch (currentTheme){
            case SettingsHandler.THEME_SYSTEM:
                llSystemInd.setVisibility(View.VISIBLE);
                break;
            case SettingsHandler.THEME_NIGHT:
                llNightInd.setVisibility(View.VISIBLE);
                break;
            case SettingsHandler.THEME_DAY:
                llDayInd.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void setTheme(int mode){
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



    //Оптимизация
    //Оптимизация
    //Оптимизация
    public void setOptimizationSettings(){
        Button btBlur = view.findViewById(R.id.btSettingsDesignOptimizationBlur);

        btBlur.setOnClickListener(view -> {
            //Переключаем блюр
            SettingsHandler.getInstance().optimization_blur = !SettingsHandler.getInstance().optimization_blur;

            updateOptimizationUi(); //Обновляем Ui (Выбора темы)

            //Сохраняем
            settingsEditor.putBoolean(SettingsHandler.OPTIMIZATION_BLUR, SettingsHandler.getInstance().optimization_blur);
            settingsEditor.commit();
        });

    }

    public void updateOptimizationUi(){
        //Обновляем отображение блюра в разделе оптимизации
        optimization_blur = SettingsHandler.getInstance().optimization_blur;

        View blurInd = view.findViewById(R.id.ivSettingsDesignOptimizationBlurIndicator);
        if(optimization_blur){
            blurInd.setVisibility(View.VISIBLE);
        }else{
            blurInd.setVisibility(View.INVISIBLE);
        }

    }
}
