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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

import java.util.ArrayList;
import java.util.HashMap;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;

public class ScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{
    private ZXingScannerView mScannerView;
    private static final int REQUEST_CAMERA = 1;
    private static int cameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
    private DatabaseReference databaseStartupEntry;
    private String startupName;
    private FirebaseAuth mAuth;
    private static final Integer LENGTH_QR = 5;

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
        databaseStartupEntry = FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        startupName = mAuth.getCurrentUser().getDisplayName();
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

        if (mResult.length() == LENGTH_QR) {
            addQRCode(mResult);
            Toast.makeText(getApplicationContext(), mResult + " has been scanned!", Toast.LENGTH_LONG).show();
            mScannerView.resumeCameraPreview(ScanActivity.this);
        } else {
            Toast.makeText(getApplicationContext(), "This is not Arkavidia QR!!!", Toast.LENGTH_LONG).show();
            mScannerView.resumeCameraPreview(ScanActivity.this);
        }
    }

    public void addQRCode(final String result){

        final DatabaseReference entryRef= databaseStartupEntry.child("entry");
        final DatabaseReference startupRef = databaseStartupEntry.child("startup");

        entryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if (!dataSnapshot.child(result).exists()) {
                        ArrayList<String> startup = new ArrayList<>();
                        startup.add(startupName);
                        entryRef.child(result).setValue(new Entry(result, 1, startup,false));
                    } else {
                        Entry entry = dataSnapshot.child(result).getValue(Entry.class);
                        ArrayList<String> startup = entry.getStartup();
                        if (startup != null) {
                            if (!startup.contains(startupName)) {
                                startup.add(startupName);
                                entry.setStartup(startup);
                                Integer count = entry.getCount();
                                count++;
                                entry.setCount(count);
                                entryRef.child(result).setValue(entry);
                            }
                        } else {
                            startup = new ArrayList<String>();
                            startup.add(startupName);
                            Integer count = 1;
                            entry.setStartup(startup);
                            entry.setCount(count);
                            entryRef.child(result).setValue(entry);
                        }
                    }
                } catch (NullPointerException e){
                    Toast.makeText(getApplicationContext(), "Sorry Unstable Connection with "+result, Toast.LENGTH_SHORT).show();
                    if (dataSnapshot.child(result).exists()){
                        Entry entry = dataSnapshot.child(result).getValue(Entry.class);
                        ArrayList<String> startup = entry.getStartup();
                        if (startup != null){
                            Integer count = entry.getCount();
                            count = entry.getStartup().size();
                            entry.setCount(count);
                            entryRef.child(result).setValue(entry);
                        } else {
                            entryRef.child(result).removeValue();
                        }
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        startupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    Integer countStartup = 0;
                    if (!dataSnapshot.child(startupName).exists()) {
                        ArrayList<String> entry = new ArrayList<>();
                        entry.add(result);
                        startupRef.child(startupName).setValue(new Startup(startupName, 1, entry, getZone()));
                    } else {
                        Startup startup = dataSnapshot.child(startupName).getValue(Startup.class);
                        ArrayList<String> entry = startup.getEntry();
                        if (!entry.contains(result)) {
                            entry.add(result);
                            startup.setEntry(entry);
                            countStartup = entry.size();
                            startup.setCount(countStartup);
                            startupRef.child(startupName).setValue(startup);
                        }
                    }
                } catch (NullPointerException e){
                    Toast.makeText(getApplicationContext(), "Sorry Unstable Connection with "+result, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Log.d("ScanActivity", "Add QR Code");
    }

    public Integer getZone(){
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("GITS Indonesia", 1);
        map.put("Uang Teman", 1);
        map.put("Rentuff", 1);
        map.put("Cyberlabs", 1);
        map.put("Qlapa", 1);
        map.put("Prelo", 1);
        map.put("NoLimit", 2);
        map.put("DycodeX", 2);
        map.put("NIU Corp", 2);
        map.put("Meridian", 2);
        map.put("IDCloudHost", 2);
        map.put("Inkubator IT", 3);
        map.put("Kudo", 3);
        map.put("LAPI Divusi", 3);
        map.put("Scola", 3);
        map.put("Goers", 3);
        map.put("Dewaweb", 4);
        map.put("Amartha", 4);
        map.put("BNI", 4);
        map.put("Mandala", 5);
        map.put("Tripi", 5);
        map.put("Emago", 5);
        map.put("Cicil", 5);
        map.put("Techlab", 6);
        map.put("Atom", 6);
        map.put("My-3D", 6);
        map.put("Populix", 6);
        map.put("Noompang", 7);
        map.put("terasindonesia", 7);
        map.put("Jojonomic", 7);
        map.put("Beiergo", 7);
        map.put("Biops", 7);
        map.put("Bukalapak", 8);
        map.put("Telkomsel", 8);
        map.put("Agate", 9);
        return map.get(startupName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_scan, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId() == R.id.exit_scanner){
            finish();
            startActivity(new Intent(this, HomeActivity.class));
        } else if (item.getItemId() == R.id.status_scanner) {
            final DatabaseReference ref = databaseStartupEntry.child("startup");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.child(startupName).exists()){
                        Toast.makeText(getApplicationContext(),"You haven't scanned anything!", Toast.LENGTH_LONG).show();
                    } else {
                        Startup startup = dataSnapshot.child(startupName).getValue(Startup.class);
                        Integer count = startup.getCount();
                        AlertDialog.Builder builder = new AlertDialog.Builder(ScanActivity.this);
                        builder.setTitle("Status "+ startupName);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mScannerView.resumeCameraPreview(ScanActivity.this);
                            }
                        });
                        builder.setMessage(startupName + " has scanned " + count + " people.");
                        AlertDialog alert1 = builder.create();
                        alert1.show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(intent);
        }
        return true;
    }

}
