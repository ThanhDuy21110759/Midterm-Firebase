package com.example.firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.google.firebase.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private final int PICK_IMAGE_REQUEST = 71;

    public static long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set up arrayList with Firebase
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        //Set up layout
        Button addPicture = findViewById(R.id.addPicture);
        Button removePicture = findViewById(R.id.removePicture);


        //Set up gridView
        GridView gridView = findViewById(R.id.gridView);
        PictureAdapter baseAdapter = new PictureAdapter(this, new ArrayList<>());
        gridView.setAdapter(baseAdapter);

        //Load images from firebase
        loadImagesFromFirebase(baseAdapter);

        // Catch uploadPicture event
        addPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        removePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Notice")
                        .setMessage("Remove all images ?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                removeAllImages();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }

    private void removeAllImages() {
        DatabaseReference imagesRef = databaseReference.child("images");
        imagesRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(MainActivity.this, "Remove Images successfully!!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Error: Remove Images Unsuccessfully", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                uploadImageToFirebase(imageUri);
            } else {    // API 16 {just only}
                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    for(int i = 0; i < clipData.getItemCount(); i++) {
                        Uri multiImageUri = clipData.getItemAt(i).getUri();
                        uploadImageToFirebase(multiImageUri);
                    }
                }
            }
        }
    }

    private void loadImagesFromFirebase(final PictureAdapter adapter) {
        startTime = System.currentTimeMillis();
        DatabaseReference imagesRef = databaseReference.child("images");
        imagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> imageUrls = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String imageUrl = snapshot.getValue(String.class);
                    if (imageUrl != null) {
                        imageUrls.add(imageUrl);
                    }
                }
                adapter.updateImages(imageUrls);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this,
                        "Failed to load images", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void uploadImageToFirebase(Uri imageUri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        StorageReference imageRef = storageRef.child("images/" + System.currentTimeMillis() + "." + getFileExtension(imageUri));

        uploadFile(imageUri, imageRef);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadFile(Uri fileUri, StorageReference fileRef) {
        fileRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileRef.getDownloadUrl()
                            .addOnSuccessListener(this::saveImageUriToDatabase);

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this,
                            "Failed to upload image", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveImageUriToDatabase(Uri uri) {
        long start=System.currentTimeMillis();
        DatabaseReference imagesRef = databaseReference.child("images");
        String imageKey = imagesRef.push().getKey();

        assert imageKey != null;
        imagesRef.child(imageKey)
                 .setValue(uri.toString());

        Toast.makeText(MainActivity.this,
                "Image uploaded successfully", Toast.LENGTH_SHORT).show();
        long end=System.currentTimeMillis();
        long totaltime = end-start;

        long seconds = totaltime / 1000;
        long milliseconds = totaltime % 1000;
        Toast.makeText(MainActivity.this,
                "Time to upload images: " + seconds + "." + milliseconds + " seconds", Toast.LENGTH_LONG).show();
    }

}