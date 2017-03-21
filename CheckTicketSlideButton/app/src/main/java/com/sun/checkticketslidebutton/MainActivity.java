package com.sun.checkticketslidebutton;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, CheckTicketSlideView.OnCheckStateChangedListener {
    private CheckTicketSlideView checkTicketSlideView;
    private CheckTicketSlideView checkTicketSlideView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkTicketSlideView = (CheckTicketSlideView) findViewById(R.id.check_ticket_slide_view);
        checkTicketSlideView.setChecked(false);
        checkTicketSlideView.setPhoneNumber("5660");
        checkTicketSlideView.setTag("5660");
        checkTicketSlideView.setOnCheckStateChangedListener(this);

        checkTicketSlideView1 = ((CheckTicketSlideView)findViewById(R.id.check_ticket_slide_view1));
        checkTicketSlideView1.setChecked(true);
        checkTicketSlideView1.setPhoneNumber("3432");
        checkTicketSlideView1.setTag("3432");
        checkTicketSlideView1.setOnCheckStateChangedListener(this);

//        findViewById(R.id.btn_click).setOnClickListener(this);
    }

    @Override
    public void onCheckStateChanged(CheckTicketSlideView view, boolean checked) {
        Log.e("MainActivity", "onCheckStateChanged view.getTag(): " + view.getTag() + " checked: " + checked);
    }

    @Override
    public void onClick(View v) {
        checkTicketSlideView.startBackgroundInfoViewAnimation();
    }
}
