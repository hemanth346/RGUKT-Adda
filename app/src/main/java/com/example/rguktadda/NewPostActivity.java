package com.example.rguktadda;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {

    private Toolbar newPostToolbar;
    private ImageView pic;
    private EditText descriptionText;
    private Button newPostSubmit;
    private Uri postImageURI;
    private ProgressBar newPostProgressBar;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private String current_user_id;

    private Bitmap compressedImageFile;
    private Integer MAX_LENGTH = 200;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();


        /*firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firebaseFirestore.setFirestoreSettings(settings);*/


        newPostToolbar = findViewById(R.id.new_post_toolbar);
        setSupportActionBar(newPostToolbar);
        getSupportActionBar().setTitle("Add New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        current_user_id = mAuth.getCurrentUser().getUid();

        newPostProgressBar = findViewById(R.id.new_post_progressBar);
        pic = findViewById(R.id.new_post_image);
        descriptionText = findViewById(R.id.new_post_description);
        newPostSubmit = findViewById(R.id.new_post_submit);


        pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512,512)
                        .setAspectRatio(1,1)
                        .start(NewPostActivity.this);
            }
        });

        newPostSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String desc = descriptionText.getText().toString().trim();

                if(!TextUtils.isEmpty(desc)){


                    newPostProgressBar.setVisibility(View.VISIBLE);
                    final String randomName = UUID.randomUUID().toString();

                    File newImageFile = new File(postImageURI.getPath());

                    try{
                        compressedImageFile = new Compressor(NewPostActivity.this)
                                .setMaxHeight(720)
                                .setMaxWidth(720)
                                .setQuality(50)
                                .compressToBitmap(newImageFile);
                    }
                    catch (IOException ex){
                        ex.printStackTrace();
                    }


                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    compressedImageFile.compress(Bitmap.CompressFormat.JPEG,100,baos);
                    byte[] imageData = baos.toByteArray();


                    final StorageReference imagePath = storageReference.child("post_images").child(randomName+".jpg");
                    final UploadTask filePath = imagePath.putBytes(imageData);

                    Task<Uri> urlTask = filePath.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();

                            }

                            return imagePath.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                final Uri postImageUri = task.getResult();

                                File newThumbFile = new File(postImageURI.getPath());
                                Toast.makeText(NewPostActivity.this,"image uploaded suce",Toast.LENGTH_LONG);

                                try{
                                    compressedImageFile = new Compressor(NewPostActivity.this)
                                            .setMaxWidth(100)
                                            .setMaxHeight(100)
                                            .setQuality(1)
                                            .compressToBitmap(newThumbFile);
                                }
                                catch (IOException ex){
                                    ex.printStackTrace();
                                }

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                compressedImageFile.compress(Bitmap.CompressFormat.JPEG,100,baos);
                                byte[] thumbData = baos.toByteArray();

                                final StorageReference thumbPath = storageReference.child("/post_images/thumbs")
                                        .child(randomName+".jpg");
                                final UploadTask uploadTask = thumbPath.putBytes(thumbData);

                                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                    @Override
                                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                        if (!task.isSuccessful()) {
                                            throw task.getException();
                                        }

                                        // Continue with the task to get the download URL
                                        return thumbPath.getDownloadUrl();
                                    }
                                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            Uri thumbUri = task.getResult();


                                            String downloadthumbUri = thumbPath.getDownloadUrl().toString();
                                            Toast.makeText(NewPostActivity.this,"successfully uploaded the picture",Toast.LENGTH_LONG);
                                            String downloadURI = uploadTask.getResult().toString();
                                            //String downloadthumbUri = taskSnapshot..getDownloadUrl().toString();

                                            Map<String,Object> postMap = new HashMap<>();
                                            // Uri downloadUri = task.getResult();
                                            postMap.put("image_url", postImageUri.toString());
                                            postMap.put("image_thumb", thumbUri.toString());
                                            postMap.put("desc", desc);
                                            postMap.put("user_id", current_user_id);
                                            postMap.put("timestamp", FieldValue.serverTimestamp());

                                            firebaseFirestore.collection("Posts")
                                                    .add(postMap)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            Toast.makeText(NewPostActivity.this, "Post was added", Toast.LENGTH_LONG).show();
                                                            Intent mainIntent = new Intent(NewPostActivity.this, MainActivity.class);
                                                            startActivity(mainIntent);
                                                            finish();
                                                            Log.d("posts save", "DocumentSnapshot added with ID: " + documentReference.getId());
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.w("postsave", "Error adding document", e);
                                                        }
                                                    });



                                            newPostProgressBar.setVisibility(View.INVISIBLE);
                                        } else {

                                            Toast.makeText(NewPostActivity.this,"error occured",Toast.LENGTH_LONG);
                                            newPostProgressBar.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                });

                            } else {

                                newPostProgressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(NewPostActivity.this,"error occured while updating the photo",Toast.LENGTH_LONG);
                            }
                        }
                    });


/*

                    filePath.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                           // final  String downloadURI = filePath.get
                            if(task.isSuccessful()){
                                File newThumbFile = new File(postImageURI.getPath());
                                Toast.makeText(NewPostActivity.this,"image uploaded suce",Toast.LENGTH_LONG);

                                try{
                                    compressedImageFile = new Compressor(NewPostActivity.this)
                                            .setMaxWidth(100)
                                            .setMaxHeight(100)
                                            .setQuality(1)
                                            .compressToBitmap(newThumbFile);
                                }
                                catch (IOException ex){
                                    ex.printStackTrace();
                                }

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                compressedImageFile.compress(Bitmap.CompressFormat.JPEG,100,baos);
                                byte[] thumbData = baos.toByteArray();

                              */
/*  StorageReference filepath = storageReference.child("/post_images/thumbs")
                                        .child(randomName+".jpg");
                                //Uri File= Uri.fromFile(new File(thumbData));

                                filepath.putFile(thumbData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                                    {
                                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                Uri downloadUrl = uri;
                                                //Do what you want with the url
                                            }
        Toast.makeText(MtActivity.this, "Upload Done", Toast.LENGTH_LONG).show();
                                        }
                                    });*//*


                              final StorageReference thumbPath = storageReference.child("/post_images/thumbs")
                                      .child(randomName+".jpg");
                                    final UploadTask uploadTask = thumbPath.putBytes(thumbData);

                                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                    @Override
                                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                        if (!task.isSuccessful()) {
                                            throw task.getException();
                                        }

                                        // Continue with the task to get the download URL
                                        return thumbPath.getDownloadUrl();
                                    }
                                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            Uri thumbUri = task.getResult();


                                            String downloadthumbUri = thumbPath.getDownloadUrl().toString();
                                            Toast.makeText(NewPostActivity.this,"successfully uploaded the picture",Toast.LENGTH_LONG);
                                            String downloadURI = uploadTask.getResult().toString();
                                            //String downloadthumbUri = taskSnapshot..getDownloadUrl().toString();

                                            Map<String,Object> postMap = new HashMap<>();
                                            // Uri downloadUri = task.getResult();
                                            //postMap.put("image_url", imagePath.getDownloadUrl());
                                            postMap.put("image_thumb", thumbUri.toString());
                                            postMap.put("desc", desc);
                                            postMap.put("user_id", current_user_id);
                                            postMap.put("timestamp", FieldValue.serverTimestamp());

                                            firebaseFirestore.collection("Posts")
                                                    .add(postMap)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            Toast.makeText(NewPostActivity.this, "Post was added", Toast.LENGTH_LONG).show();
                                                            Intent mainIntent = new Intent(NewPostActivity.this, MainActivity.class);
                                                            startActivity(mainIntent);
                                                            finish();
                                                            Log.d("posts save", "DocumentSnapshot added with ID: " + documentReference.getId());
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.w("postsave", "Error adding document", e);
                                                        }
                                                    });



                                            newPostProgressBar.setVisibility(View.INVISIBLE);
                                        } else {

                                            Toast.makeText(NewPostActivity.this,e.getMessage(),Toast.LENGTH_LONG);
                                            newPostProgressBar.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                });

*/
/*

                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                        String downloadthumbUri = thumbPath.getDownloadUrl().toString();
                                        Toast.makeText(NewPostActivity.this,"successfully uploaded the picture",Toast.LENGTH_LONG);
                                        String downloadURI = uploadTask.getResult().toString();
                                        //String downloadthumbUri = taskSnapshot..getDownloadUrl().toString();

                                        Map<String,Object> postMap = new HashMap<>();
                                       // Uri downloadUri = task.getResult();
                                        postMap.put("image_url", imagePath.getDownloadUrl());
                                        postMap.put("image_thumb", downloadthumbUri);
                                        postMap.put("desc", desc);
                                        postMap.put("user_id", current_user_id);
                                        postMap.put("timestamp", FieldValue.serverTimestamp());

                                        *//*
*/
/*firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(NewPostActivity.this,"Post was added",Toast.LENGTH_LONG);
                                                    Intent mainIntent = new Intent(NewPostActivity.this,MainActivity.class);
                                                    startActivity(mainIntent);
                                                    finish();
                                                }
                                                newPostProgressBar.setVisibility(View.INVISIBLE);
                                            }
                                        });*//*
*/
/*
                                        firebaseFirestore.collection("Posts")
                                                .add(postMap)
                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {
                                                        Toast.makeText(NewPostActivity.this, "Post was added", Toast.LENGTH_LONG).show();
                                                        Intent mainIntent = new Intent(NewPostActivity.this, MainActivity.class);
                                                        startActivity(mainIntent);
                                                        finish();
                                                        Log.d("posts save", "DocumentSnapshot added with ID: " + documentReference.getId());
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w("postsave", "Error adding document", e);
                                                    }
                                                });



                                        *//*
*/
/*firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {

                                                if(task.isSuccessful()){

                                                    Toast.makeText(NewPostActivity.this, "Post was added", Toast.LENGTH_LONG).show();
                                                    Intent mainIntent = new Intent(NewPostActivity.this, MainActivity.class);
                                                    startActivity(mainIntent);
                                                    finish();

                                                } else {


                                                }

                                                newPostProgressBar.setVisibility(View.INVISIBLE);

                                            }
                                        });*//*
*/
/*
                                        newPostProgressBar.setVisibility(View.INVISIBLE);
                                    }

                                });
                                uploadTask.addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(NewPostActivity.this,e.getMessage(),Toast.LENGTH_LONG);
                                        newPostProgressBar.setVisibility(View.INVISIBLE);

                                    }
                                });*//*

                            }
                            else{
                                newPostProgressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(NewPostActivity.this,"error occured while updating the photo",Toast.LENGTH_LONG);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {


                            Toast.makeText(NewPostActivity.this,e.getMessage(),Toast.LENGTH_LONG);
                        }
                    });
*/

                    /*filePath.putFile(postImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){

                                File newImageURL = new File(postImageURI.getPath());

                                try {
                                    compressedImageFile = new Compressor(NewPostActivity.this)
                                            .setMaxHeight(200)
                                            .setMaxWidth(200)
                                            .setQuality(10)
                                            .compressToBitmap(newImageURL);


                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                final byte[] thumbData = baos.toByteArray();
                                final UploadTask uploadTask = storageReference.child("post_images/thumbs").child(randomName+".jpg").putBytes(thumbData);

                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                        Toast.makeText(NewPostActivity.this,"thumbnail uploaded successfully",Toast.LENGTH_LONG);

                                        String thumbURI = uploadTask.getResult().toString();
                                        String downloadURI = filePath.getDownloadUrl().toString();
                                        Map<String , Object> postMap = new HashMap<>();
                                        //postMap.put("image_url",downloadURI);
                                        postMap.put("desc",desc);
                                        postMap.put("shit","shit indeed");
                                        postMap.put("user_id",current_user_id);
                                        //postMap.put("thum_url",thumbURI);
                                        postMap.put("timestamp",FieldValue.serverTimestamp());
                                        // firebaseFirestore = FirebaseFirestore.getInstance();
                                        firebaseFirestore = FirebaseFirestore.getInstance();
                                        firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {

                                                if(task.isSuccessful()){
                                                    Toast.makeText(NewPostActivity.this,"post added successfully!!",Toast.LENGTH_LONG);
                                                    Intent mainIntent = new Intent(NewPostActivity.this, MainActivity.class);
                                                    startActivity(mainIntent);
                                                    finish();
                                                }else{

                                                }

                                                newPostProgressBar.setVisibility(View.INVISIBLE);
                                            }

                                        });


                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        String error = e.getMessage();
                                        Toast.makeText(NewPostActivity.this,error,Toast.LENGTH_LONG);

                                        //Toast.makeText(NewPostActivity.this,e,Toast.LENGTH_LONG).show();
                                    }
                                });

                            }else{
                                newPostProgressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(NewPostActivity.this, "no description ",Toast.LENGTH_LONG);
                            }
                            newPostProgressBar.setVisibility(View.INVISIBLE);
                        }
                    });*/
                }else{
                        newPostProgressBar.setVisibility(View.INVISIBLE);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                postImageURI = result.getUri();
                pic.setImageURI(postImageURI);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


}
