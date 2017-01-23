package com.example.kidsalphabetsar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.example.kidsalphabetsar.ArTracker.ArScannerActivity;
import com.example.kidsalphabetsar.Util.OnClickShader;

public class MainActivity extends AppCompatActivity implements Animation.AnimationListener{

    private TextView textSpam;
    private OnClickShader mOnClickShader;
    private Button btnArActivity, btnDrawingActivity;
    private Animation animation;
    private boolean isArActivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        animation = AnimationUtils.loadAnimation(this, R.anim.back_to_front_animation);

        mOnClickShader = new OnClickShader();
        btnArActivity = (Button)findViewById(R.id.btnArActivity);
        btnDrawingActivity = (Button)findViewById(R.id.btnDrawingActivity);

        btnArActivity.setOnTouchListener(mOnClickShader);
        btnDrawingActivity.setOnTouchListener(mOnClickShader);

        textSpam = (TextView) findViewById(R.id.textSpam);
        SpannableStringBuilder spannableString7 = new SpannableStringBuilder("test");
        textSpam.setText(spannableString7, TextView.BufferType.SPANNABLE);

        animation.setAnimationListener(this);
    }

    public void menuClick(View view){
        if(view.getId() == R.id.btnArActivity){
            view.startAnimation(animation);
            isArActivity = true;
        }else if(view.getId() == R.id.btnDrawingActivity){
            isArActivity = false;
            view.startAnimation(animation);
        }
    }

    @Override
    public void onAnimationStart(Animation animation) {
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if(isArActivity)
            startActivity(new Intent(this, ArScannerActivity.class));
        else
            startActivity(new Intent(this, DrawingActivity.class));

    }

    @Override
    public void onAnimationRepeat(Animation animation) {
    }
}
