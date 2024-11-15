package com.example.easychat.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easychat.R;
import com.example.easychat.model.ChatroomModel;
import com.example.easychat.model.UserModel;
import com.example.easychat.ui.activities.ChatActivity;
import com.example.easychat.utils.AndroidUtil;
import com.example.easychat.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class RecentChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatroomModel, RecentChatRecyclerAdapter.ChatroomModelViewHolder> {

    Context context;

    public RecentChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatroomModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<ChatroomModel> chatroomList) {
        // Filter out items with null last messages
        List<ChatroomModel> filteredList = new ArrayList<>();
        for (ChatroomModel model : chatroomList) {
            if (model.getLastMessage() != null) {
                filteredList.add(model);
            }
        }

        Query newQuery = FirebaseUtil.allChatroomCollectionReference()
                .whereArrayContains("userIds", FirebaseUtil.currentUserId())
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatroomModel> updatedOptions =
                new FirestoreRecyclerOptions.Builder<ChatroomModel>()
                        .setQuery(newQuery, ChatroomModel.class)
                        .build();

        updateOptions(updatedOptions);
        notifyDataSetChanged();
    }


    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindViewHolder(@NonNull ChatroomModelViewHolder holder, int position, @NonNull ChatroomModel model) {
        if (model.getLastMessage() == null) {
            // Skip binding data for this item
            return;
        }
        FirebaseUtil.getOtherUserFromChatroom(model.getUserIds())
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean lastMessageSentByMe = model.getLastMessageSenderId().equals(FirebaseUtil.currentUserId());


                        UserModel otherUserModel = task.getResult().toObject(UserModel.class);

                        assert otherUserModel != null;
                        FirebaseUtil.getOtherProfilePicStorageRef(otherUserModel.getUserId()).getDownloadUrl()
                                .addOnCompleteListener(t -> {
                                    if (t.isSuccessful()) {
                                        Uri uri = t.getResult();
                                        AndroidUtil.setProfilePic(context, uri, holder.profilePic);
                                    }
                                });

                        holder.usernameText.setText(otherUserModel.getUsername());

                        if (lastMessageSentByMe)
                            if (model.getLastMessage().contains("/images")) {
                                holder.lastMessageText.setText("You : " + "Image");
                            } else if (model.getLastMessage().contains("/voices")) {
                                holder.lastMessageText.setText("You : " + "Voice Message");
                            } else {
                                holder.lastMessageText.setText("You : " + model.getLastMessage());
                            }
                        else if (model.getLastMessage().contains("/images")) {
                            holder.lastMessageText.setText("Image");
                        } else if (model.getLastMessage().contains("/voices")) {
                            holder.lastMessageText.setText("Voice Message");
                        } else {
                            holder.lastMessageText.setText(model.getLastMessage());
                        }
                        holder.lastMessageTime.setText(FirebaseUtil.timestampToString(model.getLastMessageTimestamp()));

                        holder.itemView.setOnClickListener(v -> {
                            //navigate to chat activity
                            Intent intent = new Intent(context, ChatActivity.class);
                            AndroidUtil.passUserModelAsIntent(intent, otherUserModel);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        });

                    }
                });
    }

    @NonNull
    @Override
    public ChatroomModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recent_chat_recycler_row, parent, false);
        return new ChatroomModelViewHolder(view);
    }

    static class ChatroomModelViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText;
        TextView lastMessageText;
        TextView lastMessageTime;
        ImageView profilePic;

        public ChatroomModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.user_name_text);
            lastMessageText = itemView.findViewById(R.id.last_message_text);
            lastMessageTime = itemView.findViewById(R.id.last_message_time_text);
            profilePic = itemView.findViewById(R.id.profile_pic_image_view);
        }
    }
}
