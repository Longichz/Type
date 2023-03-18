package xyz.genscode.type.utils;

import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import xyz.genscode.type.R;


public class PopupChat extends AppCompatActivity {
    public View llPopupInclude, llPopupBackgroundDark, llPopupBackground, llPopupChatRead, llPopupChatDelete;
    public Button btPopupRead, btPopupDelete;
    Handler handler;
    public boolean isShowed = false;

    public PopupChat(View llPopupInclude, View llPopupBackgroundDark, View llPopupBackground, View llPopupChatRead, View llPopupChatDelete,
                     Button btPopupRead, Button btPopupDelete){
        this.llPopupInclude = llPopupInclude;
        this.llPopupBackgroundDark = llPopupBackgroundDark;
        this.llPopupBackground = llPopupBackground;
        this.llPopupChatDelete = llPopupChatDelete;
        this.llPopupChatRead = llPopupChatRead;
        this.btPopupDelete = btPopupDelete;
        this.btPopupRead = btPopupRead;

        handler = new Handler();

        llPopupBackgroundDark.setOnClickListener(view -> {
            hide();
        });
    }

    public void show(){
        show(true);
    }

    public void show(boolean isReadAvailable){
        isShowed = true;

        if(!isReadAvailable){
            llPopupChatRead.setVisibility(View.GONE);
        }else{
            llPopupChatRead.setVisibility(View.VISIBLE);
        }

        llPopupInclude.setVisibility(View.VISIBLE);

        Animation slideUpAnim = AnimationUtils.loadAnimation(llPopupBackground.getContext(), R.anim.slide_up);
        llPopupBackground.startAnimation(slideUpAnim);
    }

    public void hide(){
        isShowed = false;
        Animation slideDownAnim = AnimationUtils.loadAnimation(llPopupBackground.getContext(), R.anim.slide_down);
        llPopupBackground.startAnimation(slideDownAnim);
        llPopupInclude.setVisibility(View.INVISIBLE);
    }

}
