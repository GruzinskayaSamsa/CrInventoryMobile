package com.example.storageappmobile;

import android.app.Activity;
import android.os.Bundle;

public class ObjectInfo extends Activity {

    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.object_info);

        Bundle arguments = getIntent().getExtras();
        String data = arguments.getString("data");

    }

}
