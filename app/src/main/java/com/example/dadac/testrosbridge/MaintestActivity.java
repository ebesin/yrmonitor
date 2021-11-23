package com.example.dadac.testrosbridge;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.dadac.testrosbridge.RosBridgeActivity;
import com.dwayne.monitor.R;

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