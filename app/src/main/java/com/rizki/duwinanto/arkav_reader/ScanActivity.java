package com.rizki.duwinanto.arkav_reader;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.Result;

import java.sql.Date;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;

public class ScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{
    private ZXingScannerView mScannerView;
    private static final int REQUEST_CAMERA = 1;
    private static int cameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
    private DatabaseReference databaseStartupVisit;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);
        //if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M){
            if (checkPermission()){
                Toast.makeText(getApplicationContext(), "Permission already Granted", Toast.LENGTH_LONG).show();
            } else {
                requestPermission();
            }
        //}
        databaseStartupVisit = FirebaseDatabase.getInstance().getReference("StartupVisit");

        mAuth = FirebaseAuth.getInstance();
    }

    private boolean checkPermission(){
        return (ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
    }

    @Override
    public void onResume() {
        super.onResume();

        //if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M){
            if(checkPermission()){
                if (mScannerView == null){
                    mScannerView = new ZXingScannerView(this);
                    setContentView(mScannerView);
                }
                mScannerView.setResultHandler(this);
                mScannerView.startCamera();
            }
        //}
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        mScannerView.stopCamera();
    }

    public void onRequsetPermissionResult(int requestCode, String permissions[], int[] grantResults){
        switch(requestCode){
            case REQUEST_CAMERA:
                if (grantResults.length > 0) {
                boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if(cameraAccepted){
                    Toast.makeText(getApplicationContext(),"Permission Granted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(),"Permission Denied", Toast.LENGTH_LONG).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        if (shouldShowRequestPermissionRationale(CAMERA)){
                            showMessageOKCancel("Test", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which){
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                                        requestPermissions(new String[]{CAMERA}, REQUEST_CAMERA);
                                    }
                                }
                            });
                            return;
                        }
                    }
                }
                }
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener){
        new android.support.v7.app.AlertDialog.Builder(ScanActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Back", okListener)
                .create()
                .show();
    }

    @Override
    public void handleResult(Result rawResult){
        final String mResult = rawResult.getText();
        Log.d("Arkav-QR", rawResult.getText());
        Log.d("Arkav-QR", rawResult.getBarcodeFormat().toString());


        addQRCode(mResult);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scan Result");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mScannerView.resumeCameraPreview(ScanActivity.this);
            }
        });
        builder.setNeutralButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            }
        });
        builder.setMessage(rawResult.getText());
        AlertDialog alertQR = builder.create();
        alertQR.show();
    }

    public void addQRCode(String result){
        String startupName = mAuth.getCurrentUser().getDisplayName();
        Log.d("ScanActivity", "Add QR Code");
        databaseStartupVisit.child(result).setValue(startupName);
    }
}
