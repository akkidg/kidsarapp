package com.example.kidsalphabetsar;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.kidsalphabetsar.Util.CircularTextView;
import com.example.kidsalphabetsar.Util.DashedLetterSpan;
import com.example.kidsalphabetsar.Util.DrawView;
import com.example.kidsalphabetsar.Util.OnClickShader;

import static com.example.kidsalphabetsar.R.id.textView;

public class DrawingActivity extends AppCompatActivity {

    Button btn_clear_canvas, btn_left, btn_right;
    GridView gridview;
    int grid_position;
    DrawView d;
    ImageButton btnUpper, btnLower;
    //ImageView imgLetter;

    String[] itemColors = {"#2D98CC","#1BA66E","#7D4493","#F55F0c","#D11517","#3A579B","#D71C5E","#FF5A34","#00C7FF","#A58BD5","#4A2F77","#9C56B8","#095DAF","#50ABF1","#0C86BF","#FF7100","#7A003C","#D71C5E","#1DB39D","#3A3E47","#D71C5E","#3A579B","#00C7FF","#9C56B8"};

    String[] alphabets,alphabets1;

    private DashedLetterSpan mDashedLetterSpan;
    private SpannableStringBuilder mSpannableStringBuilder;
    private TextView dottedImg;
    private CircularTextView mTextViewY, mTextViewZ;
    private boolean isSmallSet = false;
    private Grid_Adapter grid_adapter;
    private Typeface typeface;
    private OnClickShader onClickShader;

    private CircularTextView previousTextView;
    private Animation mAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

        mAnimation = AnimationUtils.loadAnimation(this, R.anim.back_to_front_animation);

        onClickShader = new OnClickShader();

        previousTextView = null;

        typeface = Typeface.createFromAsset(getAssets(),"fonts/kidsfont.ttf");
        d = (DrawView) findViewById(R.id.signature_canvas);
        mTextViewY = (CircularTextView) findViewById(R.id.textY);
        mTextViewZ = (CircularTextView) findViewById(R.id.textZ);
        btn_clear_canvas = (Button) findViewById(R.id.clear_canvas_btn);
        btnUpper = (ImageButton) findViewById(R.id.btnUpper);
        btnLower = (ImageButton) findViewById(R.id.btnLower);

        mTextViewY.setTypeface(typeface);
        mTextViewZ.setTypeface(typeface);

        mDashedLetterSpan = new DashedLetterSpan(4);

        btn_left = (Button) findViewById(R.id.btn_left);
        btn_right = (Button) findViewById(R.id.btn_right);
        gridview = (GridView) findViewById(R.id.gridview);

        btn_left.setOnTouchListener(onClickShader);
        btn_right.setOnTouchListener(onClickShader);
        btn_clear_canvas.setOnTouchListener(onClickShader);
        btnLower.setOnTouchListener(onClickShader);
        btnUpper.setOnTouchListener(onClickShader);
        //set_img=(ImageView)findViewById(R.id.img_set_letter);
        /*imgLetter=(ImageView)findViewById(R.id.img_letter);
        imgLetter.setImageResource(a_big_list[0]);*/

        alphabets = new String[]{"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X"};
        alphabets1 = new String[]{"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};

        mTextViewY.setText("Y");
        mTextViewY.setStrokeColor("#000000");
        mTextViewY.setSolidColor("#D11517");

        mTextViewZ.setText("Z");
        mTextViewZ.setStrokeColor("#000000");
        mTextViewZ.setSolidColor("#1CA66E");

        dottedImg=(TextView)findViewById(R.id.dotted_img);
        setDashedLetter(alphabets[0]);

        grid_adapter = new Grid_Adapter(getApplicationContext(), alphabets, itemColors);
        gridview.setAdapter(grid_adapter);
        btn_left.setVisibility(View.INVISIBLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            btnUpper.setBackground(getResources().getDrawable(R.drawable.btnupperselected));
        }else{
            btnUpper.setBackgroundDrawable(getResources().getDrawable(R.drawable.btnupperselected));
        }

        /*View view = (View) grid_adapter.getItem(0);
        CircularTextView circularTextView = (CircularTextView) view.findViewById(R.id.textView);
        circularTextView.setStrokeWidth(1);*/

    }

    public void Big_Letter(View view) {

        view.startAnimation(mAnimation);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(getResources().getDrawable(R.drawable.btnupperselected));
            btnLower.setBackground(getResources().getDrawable(R.drawable.btnlower));
        }else{
            view.setBackgroundDrawable(getResources().getDrawable(R.drawable.btnupperselected));
            btnLower.setBackgroundDrawable(getResources().getDrawable(R.drawable.btnlower));
        }

        isSmallSet = false;
        d.clear();
        d.invalidate();
        //imgLetter.setImageResource(a_big_list[0]);
        alphabets = new String[]{"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X"};
        alphabets1 = new String[]{"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
        //grid_position=grid_position-1;

        mTextViewY.setText("Y");
        mTextViewZ.setText("Z");

        setDashedLetter(alphabets1[grid_position]);
        grid_adapter.updateDataSet(alphabets);
        grid_adapter.notifyDataSetChanged();
        //gridview.setAdapter(new Grid_Adapter(getApplicationContext(), alphabets, itemColors));
    }

    public void Small_Letter(View view) {

        view.startAnimation(mAnimation);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(getResources().getDrawable(R.drawable.btnlowerselected));
            btnUpper.setBackground(getResources().getDrawable(R.drawable.btnupper));
        }else{
            view.setBackgroundDrawable(getResources().getDrawable(R.drawable.btnlowerselected));
            btnUpper.setBackgroundDrawable(getResources().getDrawable(R.drawable.btnupper));
        }

        isSmallSet = true;
        d.clear();
        d.invalidate();
        //imgLetter.setImageResource(a_small_list[0]);
        alphabets = new String[]{"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x"};
        alphabets1 = new String[]{"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
        //grid_position=grid_position-1;
        mTextViewY.setText("y");
        mTextViewZ.setText("z");

        setDashedLetter(alphabets1[grid_position]);
        grid_adapter.updateDataSet(alphabets);
        grid_adapter.notifyDataSetChanged();
        //gridview.setAdapter(new Grid_Adapter(getApplicationContext(), alphabets, itemColors));
    }

    public void remainLetterClick(View view){
        btn_left.setVisibility(View.VISIBLE);
        if(view.getId() == R.id.textY){
            grid_position = 24;
            btn_right.setVisibility(View.VISIBLE);
            String letter = (isSmallSet) ? "y" : "Y";
            setDashedLetter(letter);
        }else if(view.getId() == R.id.textZ){
            grid_position = 25;
            btn_right.setVisibility(View.GONE);
            String letter = (isSmallSet) ? "z" : "Z";
            setDashedLetter(letter);
        }
        view.startAnimation(mAnimation);
        highlightGridItem((CircularTextView) view);
    }

    public void clear_canvas(View view) {
        view.startAnimation(mAnimation);
        // d.eraser_method();
        d.clear();
        d.invalidate();
    }

    public class Grid_Adapter extends BaseAdapter {

        Context context;
        String [] alphabets;
        String [] itemColors;
        private LayoutInflater inflater=null;

        public Grid_Adapter(Context mainActivity, String[] alphabets,String[] itemColors) {
            // TODO Auto-generated constructor stub
            context=mainActivity;
            this.alphabets = alphabets;
            this.itemColors=itemColors;
            inflater = ( LayoutInflater )context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void updateDataSet(String[] alphabets){
            this.alphabets = alphabets;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return alphabets.length;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        public class Holder
        {
            CircularTextView textView;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub\
            final Holder holder=new Holder();
            View rowView;
            rowView = inflater.inflate(R.layout.small_grid_item_english_alphabets, null);
            holder.textView=(CircularTextView) rowView.findViewById(textView);
            holder.textView.setTypeface(typeface);

            holder.textView.setText(alphabets[position]);
            holder.textView.setStrokeColor("#000000");
            holder.textView.setSolidColor(itemColors[position]);

            holder.textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    d.clear();
                    d.invalidate();
                    if (position == 0) {
                        btn_left.setVisibility(View.INVISIBLE);
                        btn_right.setVisibility(View.VISIBLE);
                    }else {
                        btn_left.setVisibility(View.VISIBLE);
                        btn_right.setVisibility(View.VISIBLE);
                    }

                    holder.textView.startAnimation(mAnimation);

                    highlightGridItem(holder.textView);

                    /*if(previousTextView != null){
                        previousTextView.setStrokeWidth(0);
                        previousTextView = holder.textView;
                        previousTextView.setStrokeWidth(1);
                    }else{
                        previousTextView = holder.textView;
                        previousTextView.setStrokeWidth(1);
                    }*/

                    grid_position = position;
                    // System.out.println("Grid Position Adapter " + grid_position);

                    //set_img.setImageResource(image[position]);
                    //imgLetter.setImageResource(image[position]);
                    setDashedLetter(alphabets[position]);
                }
            });

            return rowView;
        }
    }

    private void highlightGridItem(CircularTextView currentTextView) {
        if(previousTextView != null){
            previousTextView.setStrokeWidth(0);
            previousTextView = currentTextView;
            previousTextView.setStrokeWidth(3);
        }else{
            previousTextView = currentTextView;
            previousTextView.setStrokeWidth(3);
        }
    }

    public void click_right(View view) {
        grid_position=grid_position+1;
        if(grid_position < 25) {
            btn_right.setVisibility(View.VISIBLE);
        }else{
            btn_right.setVisibility(View.GONE);
        }

        if(grid_position > 0){
            btn_left.setVisibility(View.VISIBLE);
        }
        d.clear();
        d.invalidate();

        // System.out.println("Grid Position " + grid_position);
        //set_img.setImageResource(Effect1[grid_position]);
        //imgLetter.setImageResource(Effect1[grid_position]);

        setDashedLetter(alphabets1[grid_position]);
        /*if (grid_position==Effect1.length-1)
        {

            btn_right.setVisibility(View.INVISIBLE);
            //Toast.makeText(getApplicationContext(),"stop",Toast.LENGTH_SHORT).show();
        }*/

    }

    public void click_left(View view) {
        grid_position=grid_position-1;
        btn_right.setVisibility(View.VISIBLE);
        d.clear();
        d.invalidate();
        //  System.out.println("Grid Position " + grid_position);
        //set_img.setImageResource(Effect1[grid_position]);
        //imgLetter.setImageResource(Effect1[grid_position]);

        setDashedLetter(alphabets1[grid_position]);

        if (grid_position==0)
        {
            btn_left.setVisibility(View.INVISIBLE);
        }
    }

    private void setDashedLetter(String letter){
        mSpannableStringBuilder = new SpannableStringBuilder(letter);
        mSpannableStringBuilder.setSpan(mDashedLetterSpan,0,letter.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        dottedImg.setText(mSpannableStringBuilder, TextView.BufferType.SPANNABLE);
        dottedImg.setTextSize(180);
        dottedImg.setGravity(Gravity.CENTER);
        //dottedImg.setPadding(0,0,0,100);
    }

}
