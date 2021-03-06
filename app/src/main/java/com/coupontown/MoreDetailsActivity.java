package com.coupontown;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.coupontown.model.ItemOfferModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.like.LikeButton;

public class MoreDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_name, tv_cat, tv_item_desc, status;
    private ImageView circleImageView;
    private ImageView sharebutton;
    private LikeButton likeButton;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_details);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setOnClickListener(this);

        final ItemOfferModel itemdetails = getIntent().getParcelableExtra("itemdetails");

        Log.i("itemdetails", itemdetails.toString());


        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout linearLayout = findViewById(R.id.ll);

        View itemrow = inflater.inflate(R.layout.item_row, linearLayout, true);

        View contentmoredetails = inflater.inflate(R.layout.content_more_details, linearLayout, true);

        TextView name = itemrow.findViewById(R.id.tv_name);
        name.setText(itemdetails.getName());

        TextView category = itemrow.findViewById(R.id.tv_cat);
        category.setText(itemdetails.getCategory());


        TextView desc = itemrow.findViewById(R.id.tv_item_desc);
        desc.setVisibility(View.INVISIBLE);

        TextView textView = findViewById(R.id.moredetails_desc);
        textView.setText(itemdetails.getMoreDetails().getDetail_desc());


        final Button button = contentmoredetails.findViewById(R.id.button);
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        PackageManager packageManager = this.getPackageManager();
        //  final Intent intent = packageManager.getLaunchIntentForPackage(itemdetails.getMoreDetails().getAppurl());
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                button.setBackgroundResource(R.color.colorBlue);
                progressBar.setVisibility(View.VISIBLE);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        button.setText("1234567890");
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                }, 2000);
            }

        });


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        this.onBackPressed();
    }
}
