package com.example.easychat.ui.activities;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.example.easychat.R;
import com.example.easychat.adapter.BasicNeedsAdapter;
import com.example.easychat.adapter.BasicNeedsDetailAdapter;
import com.example.easychat.chatVoicePlayer.VoicePlayerView;
import com.example.easychat.database.AppDatabase;
import com.example.easychat.database.ItemDetailEntity;
import com.example.easychat.database.ItemEntity;
import com.example.easychat.interfaces.OnFileUploadListener;
import com.example.easychat.interfaces.SendImageListener;
import com.example.easychat.adapter.ChatRecyclerAdapter;
import com.example.easychat.interfaces.VoiceToTextListener;
import com.example.easychat.model.ChatMessageModel;
import com.example.easychat.model.ChatroomModel;
import com.example.easychat.model.UserModel;
import com.example.easychat.recorder.AudioRecorder;
import com.example.easychat.utils.AndroidUtil;
import com.example.easychat.utils.FilePathUtils;
import com.example.easychat.utils.FirebaseUtil;
import com.example.easychat.utils.MessageType;
import com.example.easychat.utils.VoiceToTextConverter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity implements ChatRecyclerAdapter.OnSoundIconClickListener, OnFileUploadListener, SendImageListener {
    UserModel otherUser;
    String chatroomId;
    ChatroomModel chatroomModel;
    ChatRecyclerAdapter adapter;
    BasicNeedsAdapter basicNeedAdapter;
    BasicNeedsDetailAdapter basicNeedDetailAdapter;
    EditText messageInput;
    ImageButton sendMessageBtn;
    ImageButton imageSendBtn;
    ImageButton backBtn;
    TextView otherUsername;
    RecyclerView recyclerView;
    ImageView imageView;
    TextToSpeech textToSpeech;
    AudioRecorder audioRecorder;
    File recordFile;
    RecordView recordView;
    LinearLayout msgLayout;
    RecordButton recordButton;
    private List<ItemEntity> itemList;
    private List<ItemDetailEntity> itemDetailEntityList;
    private AppDatabase appDatabase;
    int basicCatId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //U S E R    M O D E L
        otherUser = AndroidUtil.getUserModelFromIntent(getIntent());
        chatroomId = FirebaseUtil.getChatroomId(FirebaseUtil.currentUserId(), otherUser.getUserId());
        ConfirmImageActivity.getImageListener(this);

        //I D s
        backBtn = findViewById(R.id.back_btn);
        msgLayout = findViewById(R.id.msg_layout);
        recordView = findViewById(R.id.record_view);
        recordButton = findViewById(R.id.record_button);
        imageSendBtn = findViewById(R.id.image_send_btn);
        otherUsername = findViewById(R.id.other_username);
        messageInput = findViewById(R.id.chat_message_input);
        sendMessageBtn = findViewById(R.id.message_send_btn);
        recyclerView = findViewById(R.id.chat_recycler_view);
        imageView = findViewById(R.id.profile_pic_image_view);

        // Check for internet connectivity
        if (!isConnectedToInternet()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }

        //V O I C E    M S G
        audioRecorder = new AudioRecorder();
        recordButton.setRecordView(recordView);
        recordButton.setOnRecordClickListener(v -> Log.d("Be_My_Voice", "onClick"));
        recordView.setCancelBounds(8);
        recordView.setSmallMicColor(Color.parseColor("#c2185b"));
        recordView.setLessThanSecondAllowed(false);
        recordView.setSlideToCancelText("Slide To Cancel");
        recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                msgLayout.setVisibility(View.GONE);
                recordView.setVisibility(View.VISIBLE);
                File musicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
                recordFile = new File(musicDirectory, UUID.randomUUID().toString() + ".mp3");
                try {
                    audioRecorder.start(recordFile.getPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("Be_My_Voice", "onStart");
            }

            @Override
            public void onCancel() {
                stopRecording(true);
                Log.d("Be_My_Voice", "onCancel");
            }

            @Override
            public void onFinish(long recordTime, boolean limitReached) {
                stopRecording(false);
                String time = getHumanTimeText(recordTime);
                Log.d("Be_My_Voice", "onFinish , Recoding time " + time + " File saved at " + recordFile.getPath());
                uploadVoiceFile(recordFile.getPath(), new OnFileUploadListener() {
                    @Override
                    public void onUploadSuccess(String downloadUrl) {
                        Toast.makeText(ChatActivity.this, "Upload Success", Toast.LENGTH_SHORT).show();
                        sendMessageToUser(downloadUrl, MessageType.VOICE.toString());
                    }

                    @Override
                    public void onUploadFailure() {
                        Toast.makeText(ChatActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onLessThanSecond() {
                stopRecording(true);
                Log.d("Be_My_Voice", "OnLessThanSecond");
            }

            @Override
            public void onLock() {
                Log.d("Be_My_Voice", "onLock");
            }
        });
        recordView.setOnBasketAnimationEndListener(() -> Log.d("Be_My_Voice", "Basket Animation Finished"));
        recordView.setRecordPermissionHandler(() -> {
            boolean recordPermissionAvailable = ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.RECORD_AUDIO) == PERMISSION_GRANTED;
            if (recordPermissionAvailable) {
                return true;
            }
            ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
            return false;
        });

        //T E X T     T O     S P E E C H
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.getDefault());

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TextToSpeech", "Language not supported");
                }
            } else {
                Log.e("TextToSpeech", "Initialization failed");
            }
        });

        //F I R E B A S E
        FirebaseUtil.getOtherProfilePicStorageRef(otherUser.getUserId()).getDownloadUrl().addOnCompleteListener(t -> {
            if (t.isSuccessful()) {
                Uri uri = t.getResult();
                AndroidUtil.setProfilePic(this, uri, imageView);
            }
        });

        backBtn.setOnClickListener((v) -> finish());
        imageSendBtn.setOnClickListener((v ->
        {
            Dialog dialog = new Dialog(this);
            @SuppressLint("InflateParams") View contentView = LayoutInflater.from(this).inflate(R.layout.select_media_layout, null);
            dialog.setContentView(contentView);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            Objects.requireNonNull(dialog.getWindow()).setLayout(layoutParams.width, layoutParams.height);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            ImageView selectGallery = dialog.findViewById(R.id.gallery);
            ImageView selectLibrary = dialog.findViewById(R.id.media_gallery);
            View decorView = dialog.getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            decorView.setPadding(16, 0, 16, 0);
            dialog.setCanceledOnTouchOutside(true);
            selectGallery.setOnClickListener(view -> {
                dialog.dismiss();
                startActivity(new Intent(ChatActivity.this, ImportImagesActivity.class));
            });
            selectLibrary.setOnClickListener(view -> {
                dialog.dismiss();
                showMediaLibraryDialog();
            });
            dialog.show();

        }));
        otherUsername.setText(otherUser.getUsername());
        sendMessageBtn.setOnClickListener((v -> {
            String message = messageInput.getText().toString().trim();
            if (message.isEmpty()) return;
            sendMessageToUser(message, MessageType.TEXT.toString());
        }));
        getOrCreateChatroomModel();
        setupChatRecyclerView();
    }

    private void uploadImageToFirebase(String imagePath, Dialog dialog, String title, String desc) {
        String path = FilePathUtils.getRealPathFromUri(this, Uri.parse(imagePath));
        // Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("images");

        String fileName = UUID.randomUUID().toString() + ".jpg";

        StorageReference imageRef = storageRef.child(path);
        Uri file = Uri.fromFile(new File(path));

        // Upload the file with metadata
        UploadTask uploadTask = imageRef.putFile(file);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Image uploaded successfully
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // Get the download URL
                String downloadUrl = uri.toString();
                // You can now use the downloadUrl, for example, to send it in a message
                sendImage("Title: " + title + ",Description: " + desc + ",Image: " + downloadUrl + " ,", MessageType.CARD.toString());
                Toast.makeText(this, "Send", Toast.LENGTH_SHORT).show();
                dialog.dismiss();

            }).addOnFailureListener(e -> {
                // Handle failure to get download URL
                Toast.makeText(this, "failed " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                finish();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "failed-- " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void showMediaLibraryDialog() {
        Dialog dialog = new Dialog(this);
        @SuppressLint("InflateParams") View contentView = LayoutInflater.from(this).inflate(R.layout.dialog_media_library, null);
        dialog.setContentView(contentView);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        Objects.requireNonNull(dialog.getWindow()).setLayout(layoutParams.width, layoutParams.height);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        RecyclerView rv = dialog.findViewById(R.id.list_of_cat);
        View decorView = dialog.getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        decorView.setPadding(16, 0, 16, 0);
        dialog.setCanceledOnTouchOutside(true);
        LinearLayoutManager layoutManager = new GridLayoutManager(this, 2);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        rv.setLayoutManager(layoutManager);
        itemList = new ArrayList<>();
        appDatabase = AppDatabase.getDatabase(this);
        AsyncTask.execute(() -> runOnUiThread(this::loadItemsFromDatabase));
        basicNeedAdapter = new BasicNeedsAdapter(itemList);
        rv.setAdapter(basicNeedAdapter);
        basicNeedAdapter.setOnItemClickListener(position -> {
            dialog.dismiss();
            openDetailDialog(itemList.get(position).itemName, itemList.get(position).id);
        });
        dialog.show();
    }

    private void openDetailDialog(String itemName, int id) {
        Dialog dialog = new Dialog(this);
        @SuppressLint("InflateParams") View contentView = LayoutInflater.from(this).inflate(R.layout.dialog_media_library, null);
        dialog.setContentView(contentView);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        Objects.requireNonNull(dialog.getWindow()).setLayout(layoutParams.width, layoutParams.height);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        RecyclerView rv = dialog.findViewById(R.id.list_of_cat);
        View decorView = dialog.getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        decorView.setPadding(16, 0, 16, 0);
        dialog.setCanceledOnTouchOutside(true);
        LinearLayoutManager layoutManager = new GridLayoutManager(this, 2);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        itemDetailEntityList = new ArrayList<>();
        rv.setLayoutManager(layoutManager);
        basicCatId = id;
        appDatabase = AppDatabase.getDatabase(this);
        AsyncTask.execute(() -> runOnUiThread(this::loadItemsDetailFromDatabase));
        basicNeedDetailAdapter = new BasicNeedsDetailAdapter(itemDetailEntityList);
        rv.setAdapter(basicNeedDetailAdapter);
        basicNeedDetailAdapter.setOnItemClickListener(position -> {
            ItemDetailEntity selectedItem = itemDetailEntityList.get(position);

            // Assuming you have the image path stored in selectedItem.getImagePath()
            String imagePath = selectedItem.imageUri;

            if (imagePath != null && !imagePath.isEmpty()) {
                uploadImageToFirebase(imagePath, dialog, selectedItem.itemName, selectedItem.itemDescription);
            }
        });
        dialog.show();
    }

    private void uploadImage(String imagePath, OnFileUploadListener listener) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("images");
        String fileName = UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageRef.child(fileName);
        Uri file = Uri.fromFile(new File(imagePath));
        UploadTask uploadTask = imageRef.putFile(file);

        uploadTask.addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    listener.onUploadSuccess(downloadUrl);
                }).addOnFailureListener(e -> listener.onUploadFailure()))
                .addOnFailureListener(e -> listener.onUploadFailure());
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadItemsDetailFromDatabase() {
        new LoadItemsDetailTask().execute();
    }



    @SuppressLint("StaticFieldLeak")
    private class LoadItemsDetailTask extends AsyncTask<Void, Void, List<ItemDetailEntity>> {
        @Override
        protected List<ItemDetailEntity> doInBackground(Void... voids) {
            return appDatabase.itemDao().getSubItem(basicCatId);
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        protected void onPostExecute(List<ItemDetailEntity> result) {
            itemDetailEntityList.clear();
            itemDetailEntityList.addAll(result);
            basicNeedDetailAdapter.notifyDataSetChanged();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadItemsFromDatabase() {
        new LoadItemsTask().execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadItemsTask extends AsyncTask<Void, Void, List<ItemEntity>> {
        @Override
        protected List<ItemEntity> doInBackground(Void... voids) {
            return appDatabase.itemDao().getAllItems();
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        protected void onPostExecute(List<ItemEntity> result) {
            itemList.clear();
            itemList.addAll(result);
            basicNeedAdapter.notifyDataSetChanged();
        }
    }

    private boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }

        return false;
    }

    private void stopRecording(boolean deleteFile) {
        msgLayout.setVisibility(View.VISIBLE);
        recordView.setVisibility(View.GONE);
        audioRecorder.stop();
        if (recordFile != null && deleteFile) {
            recordFile.delete();
        }
    }

    @SuppressLint("DefaultLocale")
    private String getHumanTimeText(long milliseconds) {
        return String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(milliseconds), TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }

    void setupChatRecyclerView() {
        Query query = FirebaseUtil.getChatroomMessageReference(chatroomId).orderBy("timestamp", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<ChatMessageModel> options = new FirestoreRecyclerOptions.Builder<ChatMessageModel>().setQuery(query, ChatMessageModel.class).build();
        adapter = new ChatRecyclerAdapter(options, getApplicationContext(), this);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        recyclerView.setLayoutManager(manager);
        adapter.setSoundIconClickListener(this);
        recyclerView.setAdapter(adapter);
        adapter.startListening();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.smoothScrollToPosition(0);
            }
        });
    }

    void sendMessageToUser(String message, String type) {
        chatroomModel.setLastMessageTimestamp(Timestamp.now());
        chatroomModel.setLastMessageSenderId(FirebaseUtil.currentUserId());
        chatroomModel.setLastMessage(message);
        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);
        ChatMessageModel chatMessageModel = new ChatMessageModel(type, message, FirebaseUtil.currentUserId(), Timestamp.now());
        FirebaseUtil.getChatroomMessageReference(chatroomId).add(chatMessageModel).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                messageInput.setText("");
                sendNotification(message);
            }
        });
    }

    void getOrCreateChatroomModel() {
        FirebaseUtil.getChatroomReference(chatroomId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                chatroomModel = task.getResult().toObject(ChatroomModel.class);
                if (chatroomModel == null) {
                    //first time chat
                    chatroomModel = new ChatroomModel(chatroomId, Arrays.asList(FirebaseUtil.currentUserId(), otherUser.getUserId()), Timestamp.now(), "");
                    FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);
                }
            }
        });
    }

    @Override
    public void onUploadSuccess(String downloadUrl) {
    }

    @Override
    public void onUploadFailure() {
    }

    void uploadVoiceFile(String filePath, OnFileUploadListener listener) {
        Toast.makeText(this, "Uploading, Please wait", Toast.LENGTH_SHORT).show();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("voices");
        String fileName = UUID.randomUUID().toString() + ".mp3";
        StorageReference voiceRef = storageRef.child(fileName);
        Uri file = Uri.fromFile(new File(filePath));
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("audio/mp3").build();
        UploadTask uploadTask = voiceRef.putFile(file, metadata);
        uploadTask.addOnSuccessListener(taskSnapshot -> voiceRef.getDownloadUrl().addOnSuccessListener(uri -> {
            String downloadUrl = uri.toString();
            listener.onUploadSuccess(downloadUrl);
        }).addOnFailureListener(e -> listener.onUploadFailure())).addOnFailureListener(e -> listener.onUploadFailure());
    }

    void sendNotification(String message) {
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                UserModel currentUser = task.getResult().toObject(UserModel.class);
                try {
                    JSONObject jsonObject = new JSONObject();
                    JSONObject notificationObj = new JSONObject();
                    assert currentUser != null;
                    notificationObj.put("title", currentUser.getUsername());
                    notificationObj.put("body", message);
                    JSONObject dataObj = new JSONObject();
                    dataObj.put("userId", currentUser.getUserId());
                    jsonObject.put("notification", notificationObj);
                    jsonObject.put("data", dataObj);
                    jsonObject.put("to", otherUser.getFcmToken());
                    callApi(jsonObject);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    void callApi(JSONObject jsonObject) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        String url = "https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder().url(url).post(body).header("Authorization", "Bearer AAAAblus5Hs:APA91bF7zX6BxdSHcR57Zf1RyC_PgYxIYUNnb_nOO9SC6LMxrOTqBGi0euaK6kNAu9FNi_qWsqL0NWZD4s3xNKWkrg0429mb6i_Mguo2jeiy6BXnIwANyrlPDcd3wVmb8p6otrSc4PMd").build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
            }
        });

    }

    @Override
    public void onSoundIconClick(String message) {
        textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @Override
    public void onSpeechToTextClick(String message) {
        Dialog dialog = new Dialog(this);
        @SuppressLint("InflateParams") View contentView = LayoutInflater.from(this).inflate(R.layout.dialog_speech_to_text, null);
        dialog.setContentView(contentView);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        Objects.requireNonNull(dialog.getWindow()).setLayout(layoutParams.width, layoutParams.height);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            VoicePlayerView voiceMessage = dialog.findViewById(R.id.voice_message);
            TextView voiceText = dialog.findViewById(R.id.voice_text);
            View decorView = dialog.getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            decorView.setPadding(16, 0, 16, 0);
            voiceMessage.setAudio(message);
        Log.d("de_voice", "onSpeechToTextClick: "+message);
       VoiceToTextConverter voiceToTextConverter=new VoiceToTextConverter();
       voiceToTextConverter.startSpeechRecognition(this, new VoiceToTextListener() {
           @Override
           public void onConvert(String text) {
               voiceText.setText(text);
           }
       });
      /*  if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 10);
        } else {
            Toast.makeText(this, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show();
        }*/
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//                    txvResult.setText(result.get(0));
                    Toast.makeText(this, ""+result.get(0), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
    @Override
    public void sendImage(String message, String type) {
        sendMessageToUser(message, type);
    }
}

