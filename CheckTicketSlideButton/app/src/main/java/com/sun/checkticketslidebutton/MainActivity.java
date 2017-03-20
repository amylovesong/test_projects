package com.sun.checkticketslidebutton;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private CheckTicketSlideView checkTicketSlideView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkTicketSlideView = (CheckTicketSlideView) findViewById(R.id.check_ticket_slide_view);
        checkTicketSlideView.setChecked(false);

        findViewById(R.id.btn_click).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        checkTicketSlideView.startBackgroundIconAnimation();
    }
}
