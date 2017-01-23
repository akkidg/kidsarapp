package com.example.kidsalphabetsar.Util;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by User on 26-Nov-16.
 */

public class OnClickShader implements View.OnTouchListener {

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                view.getBackground().setColorFilter(Color.parseColor("#70252525"), PorterDuff.Mode.SRC_ATOP);
                view.invalidate();

                /*
                * Uncomment below if set background As setImageResource()
                *
                * */

                /*ImageView image = (ImageView)view;
                if(image != null) {
                    image.getDrawable().setColorFilter(Color.parseColor("#70252525"), PorterDuff.Mode.SRC_ATOP);
                    image.invalidate();
                }*/

            break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                view.getBackground().clearColorFilter();
                view.invalidate();

                /*
                * Uncomment below if set background As setImageResource()
                *
                * */

                /*ImageView image = (ImageView)view;
                if(image != null) {
                    image.getDrawable().clearColorFilter();
                    image.invalidate();
                }*/

            break;
        }
        return false;
    }
}
