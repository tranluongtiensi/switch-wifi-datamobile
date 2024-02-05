package org.dop.switchwifiandmobiledata;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final int WIFI_REQUEST_CODE = 1;
    private WifiManager wifiManager;
    private ConnectivityManager connectivityManager;
    private boolean isWifiOn;
    private boolean isDataMobileOn = false;
    private ImageButton wifiImgBtn, dataMobileImgBtn;
    private TextView wifiStatus, mobiledataStatus;
    private TextView nameWifiDialog;
    private String SSID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiImgBtn = findViewById(R.id.wifi_imgbtn);
        dataMobileImgBtn = findViewById(R.id.datamobile_imgbtn);
        wifiStatus = findViewById(R.id.wifi_status);
        mobiledataStatus = findViewById(R.id.mobile_data_status);


        checkPermissionRequest();
    }

    public void checkPermissionRequest() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, WIFI_REQUEST_CODE);
        }
        checkNetworkEnable();
    }

    public void checkNetworkEnable() {


        try {
            wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

            Class cmClass = Class.forName(connectivityManager.getClass().getName());
            Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true); // Make the method callable
            // get the setting for "mobile data"
            isDataMobileOn = (Boolean) method.invoke(connectivityManager);

            //get wifi status
            isWifiOn = wifiManager.isWifiEnabled();
        } catch (Exception e) {
            Log.e("Error", "checkWifiEnable: ", e);
        }
        // xử lí mạng dữ liệu UI
        updateWifiUi();

        // wifi UI
        updateMobileDataMobile();

        wifiImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isWifiOn) {
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    SSID = wifiInfo.getSSID();
                    showDialog();


                }
            }
        });
    }

    public void showDialog() {
        LinearLayout dialogContainer = findViewById(R.id.dialog_container);
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.wifi_dialog, dialogContainer);

        nameWifiDialog = view.findViewById(R.id.wifi_name_dialog);


        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setView(view);
        final AlertDialog alertDialog = builder.create();

//        if (alertDialog.getWindow() != null) {
//            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
//        }
        alertDialog.show();
        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);

                if (success) {
                    scanSuccess();
                } else {
                    scanFailure();
                }
            }
        };


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        wifiManager.startScan();
        registerReceiver(wifiScanReceiver,intentFilter);
    }



    private void scanSuccess() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            List<ScanResult> Result = wifiManager.getScanResults();
            StringBuilder listWifi = new StringBuilder();
            for (ScanResult result: Result){
                String SSID = result.SSID;
                listWifi.append(SSID).append("\n");

            }
            Toast.makeText(this, listWifi, Toast.LENGTH_SHORT).show();
            nameWifiDialog.setText(listWifi.toString());
        }
    }
    private void scanFailure(){
        Toast.makeText(this, "Scan failure", Toast.LENGTH_SHORT).show();
    }


    public void updateWifiUi(){
        dataMobileImgBtn.setImageResource(isDataMobileOn ? R.drawable.mobile_data2 : R.drawable.mobile_data2_slash);
        mobiledataStatus.setText(isDataMobileOn ? "bật" : "tắt");
        mobiledataStatus.setTextColor(ContextCompat.getColor(this, isDataMobileOn ? R.color.green : R.color.red));
    }
    public void updateMobileDataMobile(){
        wifiImgBtn.setImageResource(isWifiOn ? R.drawable.wifi2 : R.drawable.wifi2_slash);
        wifiStatus.setText(isWifiOn ? "bật" : "tắt");
        wifiStatus.setTextColor(ContextCompat.getColor(this, isWifiOn ? R.color.green : R.color.red));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == WIFI_REQUEST_CODE){
            Toast.makeText(this, "permission granted", Toast.LENGTH_SHORT).show();
            checkNetworkEnable();
        }
    }
}