package com.example.uploadfile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Button selectFile;
    private Button uploud;
    private Button fetch;
    private TextView notif;
    private Uri pdfUri; //URL di local storage

    FirebaseStorage storage;//untuk menguploud file ex: pdf
    FirebaseDatabase database;//untuk menyimpan url file

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fetch = findViewById(R.id.fetchBtn);
        fetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RecyclerViewActivity.class));
            }
        });

        storage = FirebaseStorage.getInstance();//return object FirebaseStorage
        database = FirebaseDatabase.getInstance();//return object FirebaseDatabase

        selectFile = findViewById(R.id.selectBtn);
        uploud = findViewById(R.id.uploudBtn);
        notif = findViewById(R.id.notif_txtView);

        selectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED){
                    selectPdf();
                } else
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            9);
            }
        });

        uploud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pdfUri!=null)
                    uploadFile(pdfUri);
                else
                    Toast.makeText(MainActivity.this,"Select a File", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void uploadFile(Uri pdfUri){

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("Uploading file...");
        progressDialog.setProgress(0);
        progressDialog.show();

        final String fileName=System.currentTimeMillis()+".pdf";
        final String fileName1 = System.currentTimeMillis()+"";

        final StorageReference storageReference = storage.getReference();//return root path

        storageReference.child("Uploads").child(fileName).putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> url = taskSnapshot.getStorage().getDownloadUrl();
                url.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.e("storage download",  uri.toString());

                        DatabaseReference reference = database.getReference();

                        reference.child(fileName1).setValue(uri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful())
                                    Toast.makeText(MainActivity.this, "File is succesfully uploaded", Toast.LENGTH_LONG).show();
                                else
                                    Toast.makeText(MainActivity.this, "File is not succesfully uploaded hmm", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(MainActivity.this, "File is not succesfully uploaded", Toast.LENGTH_LONG).show();

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                //track progress uploud
                int currentProgress=(int) (100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                progressDialog.setProgress(currentProgress);
            }
        });

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==9 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            selectPdf();
        } else
            Toast.makeText(MainActivity.this, "Please provide permission", Toast.LENGTH_LONG).show();
    }

    private void selectPdf(){

        //to offer user to select a file using file manager
        // we will be using an Intent

        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);//untuk mengambil file

        startActivityForResult(intent, 86);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //cek apakah user sudah memilih file

        if (requestCode==86 && resultCode==RESULT_OK && data!=null){
            pdfUri = data.getData(); //return url selected file
            notif.setText("A file is selected: "+ data.getData().getLastPathSegment());
        } else
            Toast.makeText(MainActivity.this, "Please select a file!", Toast.LENGTH_LONG).show();

    }

}
