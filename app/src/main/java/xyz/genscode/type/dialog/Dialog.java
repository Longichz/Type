package xyz.genscode.type.dialog;


import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import xyz.genscode.type.R;

@SuppressLint("StaticFieldLeak")
public class Dialog extends AppCompatActivity{
    Handler handler;
    TextView messageHeader;
    TextView messageContent;
    Button messageButton1;
    Button messageButton2;
    View messageInclude;
    View messageBody;

    public int messageActive = 0;
    int queueCount = 0;
    int queueValue = 0;

    public Button getMessageButton1(){
        return messageButton1;
    }
    public Button getMessageButton2(){
        return messageButton2;
    }

    public Dialog(TextView messageHeaderObj, TextView messageContentObj, Button messageButtonObj, Button messageButton2Obj, View messageIncludeObj, View messageBodyObj){
        System.out.println("Message: Class constructed");

        handler = new android.os.Handler();
        messageHeader = messageHeaderObj;
        messageContent = messageContentObj;
        messageButton1 = messageButtonObj;
        messageButton2 = messageButton2Obj;
        messageInclude = messageIncludeObj;
        messageBody = messageBodyObj;

        messageButton1.setOnClickListener(view -> hideMessage());
        messageButton2.setOnClickListener(view -> hideMessage());
    }

    public void showMessage(String header, String content, String bt1, String bt2){
        showMessage(header, content, bt1, bt2, false);
    }
    public void showMessage(String header, String content, String bt1){
        showMessage(header, content, bt1, "", false);
    }

    public void showMessage(String header, String content){
        showMessage(header, content, "def", "", false);
    }

    public void showMessageWithRedButton(String header, String content){
        showMessage(header, content, "def", "", true);
    }

    public void showMessageWithRedButton(String header, String content, String bt1){
        showMessage(header, content, bt1, "", true);
    }
    public void showMessageWithRedButton(String header, String content, String bt1, String bt2){
        showMessage(header, content, bt1, bt2, true);
    }

    private void showMessage(String header, String content, String bt1, String bt2, boolean isRedButton){
        queueCount++;
        int num = queueCount;
        Thread queue = new Thread(() -> {
            while (true){
                if(messageActive != 1){
                    if((queueValue+1) == num){
                        System.out.println("Message: show "+num+", value "+queueValue);
                        runOnUiThread(() -> {
                            showMessagePriority(header, content, bt1, bt2, isRedButton);
                            queueValue++;
                        });
                        break;
                    }
                }

                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
        queue.start();
    }


    public void showMessagePriority(String header, String content, String bt1, String bt2, boolean isRedButton){
        messageActive = 1;

        messageButton1.setText(R.string.message_bt);
        messageButton2.setText(R.string.message_bt2);
        if(!bt1.equals("def")) messageButton1.setText(bt1);
        if(!bt2.equals("def")) messageButton2.setText(bt2);

        RippleDrawable rippleDrawableBt1 = (RippleDrawable) messageButton1.getBackground();
        if(isRedButton){
            rippleDrawableBt1.setColor(ColorStateList.valueOf(ContextCompat.getColor(messageButton1.getContext(), R.color.red_ripple)));
            messageButton1.setTextColor(ContextCompat.getColor(messageButton1.getContext(), R.color.red));
            messageButton1.setBackground((Drawable) rippleDrawableBt1);
        }else{
            rippleDrawableBt1.setColor(ColorStateList.valueOf(ContextCompat.getColor(messageButton1.getContext(), R.color.grey_00)));
            messageButton1.setTextColor(ContextCompat.getColor(messageButton1.getContext(), R.color.type1));
            messageButton1.setBackground((Drawable) rippleDrawableBt1);
        }

        messageButton1.setVisibility(View.VISIBLE);
        messageButton2.setVisibility(View.VISIBLE);
        if(bt2.equals(""))  messageButton2.setVisibility(View.INVISIBLE);

        messageButton1.setEnabled(true);
        messageButton2.setEnabled(true);
        messageHeader.setText(header);
        messageContent.setText(content);

        Animation anim = AnimationUtils.loadAnimation(messageBody.getContext(), R.anim.alpha_in);
        Animation anim2 = AnimationUtils.loadAnimation(messageBody.getContext(), R.anim.jump_in_scaled);
        Animation anim3 = AnimationUtils.loadAnimation(messageBody.getContext(), R.anim.jump_in_scaled_unscale);
        messageInclude.setVisibility(View.VISIBLE);
        messageInclude.startAnimation(anim);
        messageBody.startAnimation(anim2);
        handler.postDelayed(() -> messageBody.startAnimation(anim3), 90);
    }

    public void hideMessage(){

        messageButton1.setOnClickListener(view -> hideMessage());

        messageButton1.setEnabled(false);
        messageButton2.setEnabled(false);

        Animation anim = AnimationUtils.loadAnimation(messageBody.getContext(), R.anim.alpha_out_250);
        Animation anim2 = AnimationUtils.loadAnimation(messageBody.getContext(), R.anim.jump_out_scaled);
        Animation anim3 = AnimationUtils.loadAnimation(messageBody.getContext(), R.anim.jump_out_unscaled);
        messageInclude.startAnimation(anim);
        messageBody.startAnimation(anim2);
        handler.postDelayed(() -> messageBody.startAnimation(anim3), 90);
        handler.postDelayed(() -> {messageInclude.setVisibility(View.INVISIBLE); messageActive = 0;}, 250);

    }
}

