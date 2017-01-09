package com.ocr;

import android.app.Activity;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.widget.CompoundButton;

public class DefectReportActivity extends Activity{
    private InternetConnectionReceiver receiver;
    private CompoundButton mSwitchExteriorFluidLeaks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.defect_report_activity);

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = InternetConnectionReceiver.getInstance(getApplicationContext());
        registerReceiver(receiver, filter);
        receiver.setNeedToUpload(true);

        mSwitchExteriorFluidLeaks = (CompoundButton) findViewById(R.id.exterior_fluid_leaks);

    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
    }
}
