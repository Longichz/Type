package xyz.genscode.type.data;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsHandler {
    private static final SettingsHandler INSTANCE = new SettingsHandler();

    //Storage
    public static final String STORAGE_NAME = "settings";
    public static final String THEME = "theme";
    public static final String OPTIMIZATION_BLUR = "opt_blur";

    //Значения
    public static final int THEME_SYSTEM = 0;
    public static final int THEME_NIGHT = 1;
    public static final int THEME_DAY = 2;

    public int theme = 0;
    public boolean optimization_blur = true;

    private SettingsHandler(){}

    public void getSettings(Context context){ //Настройки загружаются единожды в MainActivity.onCreate()
        SharedPreferences settings = context.getSharedPreferences(STORAGE_NAME, MODE_PRIVATE);;

        theme = settings.getInt(THEME, THEME_SYSTEM);
        optimization_blur = settings.getBoolean(OPTIMIZATION_BLUR, true);
    }


    public static SettingsHandler getInstance(){
        return INSTANCE;
    }
}
