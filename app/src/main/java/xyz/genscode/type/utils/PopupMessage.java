package xyz.genscode.type.utils;

import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import xyz.genscode.type.R;


public class PopupMessage extends AppCompatActivity {
    public View llPopupInclude, llPopupBackgroundDark, llPopupBackground, llPopupMessageEdit, llPopupMessageDelete;
    TextView tvPopupMessageEdit, tvPopupMessageDelete;
    public Button btPopupEdit, btPopupDelete;
    Handler handler;
    public boolean isShowed = false;
    public boolean isEditShowed = true;

    public PopupMessage(View llPopupInclude, View llPopupBackgroundDark, View llPopupBackground, View llPopupMessageEdit, View llPopupMessageDelete,
                        Button btPopupEdit, Button btPopupDelete){
        this.llPopupInclude = llPopupInclude;
        this.llPopupBackgroundDark = llPopupBackgroundDark;
        this.llPopupBackground = llPopupBackground;
        this.llPopupMessageDelete = llPopupMessageDelete;
        this.llPopupMessageEdit = llPopupMessageEdit;
        this.btPopupDelete = btPopupDelete;
        this.btPopupEdit = btPopupEdit;

        handler = new android.os.Handler();

        llPopupBackgroundDark.setOnClickListener(view -> {
            hide();
        });
    }

    public void show(){
        show(true);
    }

    public void show(boolean isEditAvailable){
        isShowed = true;

        if(!isEditAvailable){
            isEditShowed = false;
            llPopupMessageEdit.setVisibility(View.GONE);
        }else{
            isEditShowed = true;
            llPopupMessageEdit.setVisibility(View.VISIBLE);
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
