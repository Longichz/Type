package xyz.genscode.type.fragments.settings;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import xyz.genscode.type.R;

public class AboutFragment extends Fragment {

    View view;
    boolean isAboutDevVisible = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_about, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Переключение между 'О приложении' и 'О разработчике'
        View llAboutApp = view.findViewById(R.id.llSettingsAboutApp);
        View llAboutDev = view.findViewById(R.id.llSettingsAboutDev);
        TextView tvShowAboutDev = view.findViewById(R.id.tvShowAboutDev);

        tvShowAboutDev.setOnClickListener(view1 -> {
            if(!isAboutDevVisible){
                llAboutApp.setVisibility(View.GONE);
                llAboutDev.setVisibility(View.VISIBLE);
                tvShowAboutDev.setText(getResources().getString(R.string.app_hide_dev_info));

                isAboutDevVisible = true;
            }else{
                llAboutDev.setVisibility(View.GONE);
                llAboutApp.setVisibility(View.VISIBLE);
                tvShowAboutDev.setText(getResources().getString(R.string.app_show_dev_info));

                isAboutDevVisible = false;
            }
        });
    }
}