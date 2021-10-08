package com.dwayne.monitor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MaintestActivity extends AppCompatActivity {

    private Button DC_Button_JumpToRos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DC_Button_JumpToRos = (Button) findViewById(R.id.DC_Button_JumpToRos);
    }


    public void JumpToActivity(View view) {
        Intent myIntentRos = new Intent(MaintestActivity.this, RosBridgeActivity.class);
        startActivity(myIntentRos);
    }
}
//