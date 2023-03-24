package xyz.genscode.type.dialog;

import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import xyz.genscode.type.R;

public class Toast extends AppCompatActivity {

    public static final int TIME_QUICK = 4000;
    public static final int TIME_LONG = 6000;

    View llIncludeToast;
    View llToast;
    TextView tvToastText;
    Handler handler;

    public int toastActive = 0;
    int queueCount = 0;
    int queueValue = 0;

    public Toast(View llIncludeToast, View llToast, TextView tvToastText){
        this.llIncludeToast = llIncludeToast;
        this.llToast = llToast;
        this.tvToastText = tvToastText;
        handler = new android.os.Handler();
    }

    public void show(String content){
        show(content, TIME_QUICK);
    }

    //Показываем toast
    public void show(String content, int timeMillis){
        //Добавляем в очередь
        queueCount++;
        int num = queueCount;
        Thread queue = new Thread(() -> {
            while (true){
                if(toastActive != 1){ //Ждем пока предыдущее toast будет скрыто, перед тем как показать новое
                    if((queueValue+1) == num){
                        runOnUiThread(() -> {
                            showPriority(content, timeMillis);
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

    private void showPriority(String content, int timeMillis){
        toastActive = 1;

        tvToastText.setText(content);

        llIncludeToast.setVisibility(View.VISIBLE);

        Animation slideUpAnim = AnimationUtils.loadAnimation(llToast.getContext(), R.anim.slide_up);
        llToast.startAnimation(slideUpAnim);

        handler.postDelayed((Runnable) this::hide, timeMillis);
    }

    public void hide(){
        toastActive = 0;

        Animation slideDownAnim = AnimationUtils.loadAnimation(llToast.getContext(), R.anim.slide_down);
        llToast.startAnimation(slideDownAnim);

        llIncludeToast.setVisibility(View.INVISIBLE);
    }
}
