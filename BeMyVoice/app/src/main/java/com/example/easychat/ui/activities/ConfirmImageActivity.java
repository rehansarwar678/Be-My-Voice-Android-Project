package com.example.easychat.ui.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;

import com.example.easychat.R;

import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.easychat.interfaces.SendImageListener;
import com.example.easychat.model.ChatroomModel;
import com.example.easychat.utils.MessageType;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.UUID;

public class ConfirmImageActivity extends AppCompatActivity {
    public static SendImageListener sendImageListener;

    public static void getImageListener(SendImageListener listener) {
        sendImageListener = listener;
    }

    ChatroomModel chatroomModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_image);

        //I D S
        Button sendBtn = findViewById(R.id.sendBtn);
        ImageView backBtn = findViewById(R.id.backBtn);
        ImageView imageView = findViewById(R.id.confirmImageView);

        //G E T T I N G     I M A G E
        String receivedimagePath = getIntent().getStringExtra("imagePath");
        Glide.with(this)
                .load(receivedimagePath)
                .into(imageView);

        //C L I C K    L I S T E N E R S
        backBtn.setOnClickListener((v -> finish()));

        sendBtn.setOnClickListener((v -> {
            uploadImageToFirebase(receivedimagePath);
           /* Intent chatIntent = new Intent(ConfirmImageActivity.this, ChatActivity.class);
            String imagePath = getIntent().getStringExtra("imagePath");
            chatIntent.putExtra("imagePath", imagePath);
            startActivity(chatIntent);*/

        }));

    }

    private void uploadImageToFirebase(String imagePath) {
        Toast.makeText(this, "Uploading, please wait", Toast.LENGTH_SHORT).show();
        Log.d("TAG_BE_MY_VOICE", " " + imagePath);
        // Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("images");

        // Generate a unique name for the file
        String fileName = UUID.randomUUID().toString() + ".jpg"; // Assuming the image file is in JPEG format

        StorageReference imageRef = storageRef.child(fileName);
        Uri file = Uri.fromFile(new File(imagePath));

        // Upload the file with metadata
        UploadTask uploadTask = imageRef.putFile(file);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Image uploaded successfully
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // Get the download URL
                String downloadUrl = uri.toString();
                // You can now use the downloadUrl, for example, to send it in a message
                sendImageListener.sendImage(downloadUrl, MessageType.IMAGE.toString());
                Toast.makeText(this, "Send", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> {
                // Handle failure to get download URL
                Toast.makeText(this, "failed " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            // Handle upload failure
        });
    }

}