package com.example.codescanner;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

public class info extends AppCompatActivity {

    TextView txtID;
    TextView txtName;
    TextView txtDoB;
    TextView txtDepartment;
    TextView txtSYear;
    TextView txtDate;
    TextView txtClass;
    ImageView imageView;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        txtID=(TextView)findViewById(R.id.txtID);
        txtName=(TextView)findViewById(R.id.txtName);
        txtDoB=(TextView)findViewById(R.id.txtDoB);
        txtClass=(TextView)findViewById(R.id.txtClass);
        txtDepartment=(TextView)findViewById(R.id.txtDepartment);
        txtSYear=(TextView)findViewById(R.id.txtSYear);
        txtDate=(TextView)findViewById(R.id.txtDate);
        imageView=findViewById(R.id.imageView);

        Intent intent=getIntent();
        String[] StudentA=intent.getStringArrayExtra("StudentA");

        txtID.setText(StudentA[0]);
        txtName.setText(StudentA[1]);
        txtDoB.setText(StudentA[2]);
        txtClass.setText(StudentA[3]);
        txtDepartment.setText(StudentA[4]);
        txtSYear.setText(StudentA[5]);
        txtDate.setText(StudentA[6]);
        String ImagePath=new File(getExternalFilesDir(null).getAbsolutePath(),"/"+StudentA[0]+".jpg").getAbsolutePath();
        Bitmap bmp = BitmapFactory.decodeFile(ImagePath);
        imageView.setImageBitmap(bmp);

    }
}
