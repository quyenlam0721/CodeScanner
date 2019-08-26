package com.example.codescanner;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.jakewharton.processphoenix.ProcessPhoenix;
import com.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    SurfaceView surfaceView;
    CameraSource cameraSource;
    EditText editText;
    BarcodeDetector barcodeDetector;
    List<String[]> csvContent;
    Integer position;
    Button button;
    Button buttonSendMail;
    String MaSV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        surfaceView = findViewById(R.id.camerapreview);
        Intent intent=getIntent();
        boolean permissionGranted=intent.getBooleanExtra("PermissionGranted",false);
        editText = findViewById(R.id.EditText);
        button = findViewById(R.id.btnInfo);
        buttonSendMail = findViewById(R.id.buttonSendMail);
        copyAssets(this);
        csvContent = readCSV();

        barcodeDetector = new BarcodeDetector.Builder(this).build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(640, 480)
                .setRequestedFps(60.0f)
                .setAutoFocusEnabled(true)
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                try {
                    cameraSource.start(holder);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barCode = detections.getDetectedItems();
                if (barCode.size() != 0) {
                    editText.post(new Runnable() {
                        @Override
                        public void run() {
                            //barCode.valueAt(0).displayValue;
                            editText.setText(barCode.valueAt(0).displayValue);
                            MaSV = editText.getText().toString();
                            if(CheckIn(csvContent)) {
                                String[] Student=csvContent.get(position);
                                Student[6]=GetNowDate();
                            }
                        }
                    });
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaSV = editText.getText().toString();;
                if(permissionGranted) {
                    if (CheckIn(csvContent)) {
                        Intent intent = new Intent(v.getContext(), info.class);
                            intent.putExtra("StudentA", csvContent.get(position));
                        startActivity(intent);
                    } else {
                        editText.setText(null);
                        editText.setHint("Mã SV không tồn tại!");
                    }
                }
                else
                {
                    Toast.makeText(v.getContext(),"Application can not run without permissions",Toast.LENGTH_SHORT).show();
                    finish();
                    Process.killProcess(Process.myPid());
                }
            }
        });
        buttonSendMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(getExternalFilesDir(null)+"/sv.csv");
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_SUBJECT, "Ket qua kiem dien");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"lytuongluan1628@gmail.com"});
                intent.putExtra(Intent.EXTRA_TEXT, GetNowDate());
                Uri CSV = FileProvider.getUriForFile(
                        (MainActivity.this),"com.example.codescanner.provider",file);
                intent.putExtra(Intent.EXTRA_STREAM,CSV);
                intent.setType("text/plain");
                startActivity(Intent.createChooser(intent, "Send mail"));
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        WriteCSV();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WriteCSV();
    }

    private List<String[]> readCSV(){
        List<String[]> csvLine = new ArrayList<>();
        String[] content = null;
        try {
            File CSVFile = new File(getExternalFilesDir(null)+"/sv.csv");
            BufferedReader br = new BufferedReader(new FileReader(CSVFile));
            String line;
            while((line = br.readLine()) != null){
                line = line.replaceAll("\"","");
                content = line.split(",");
                csvLine.add(content);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return csvLine;
    }
    //Copy RAW data
     public void copyAssets(Context context) {
        AssetManager assetManager = context.getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        if (files != null) for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);
                File outFile = new File(getExternalFilesDir(null), filename);
                if(!outFile.exists()) {
                    out = new FileOutputStream(outFile);
                    copyFile(in, out);
                }
            } catch(IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                        in = null;
                    } catch (IOException e) {

                    }
                }
                if (out != null) {
                    try {
                        out.flush();
                        out.close();
                        out = null;
                    } catch (IOException e) {

                    }
                }
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
    //Return StudentA
    private boolean CheckIn(List<String[]> result){

        for (int i = 0; i < result.size(); i++) {
            String[] rows = result.get(i);
            if (rows[0].equals(MaSV)) {
                position = i;
                return true;
            }
        }
        return false;
    }
    //Write to File
    private void WriteCSV(){
        String csv =getExternalFilesDir(null).getAbsolutePath()+"/sv.csv";
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(csv));
            for(String[] Student : csvContent)
            writer.writeNext(Student);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String GetNowDate() {
        SimpleDateFormat fmt = new SimpleDateFormat("hh:mm dd-MM-yyyy");
        Date c = Calendar.getInstance().getTime();
        return fmt.format(c);
    }

}
