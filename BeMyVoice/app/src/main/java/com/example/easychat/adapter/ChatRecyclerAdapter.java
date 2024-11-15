package com.example.easychat.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.easychat.R;
import com.example.easychat.chatVoicePlayer.VoicePlayerView;
import com.example.easychat.model.ChatMessageModel;
import com.example.easychat.ui.activities.ChatActivity;
import com.example.easychat.ui.activities.ImportImagesActivity;
import com.example.easychat.utils.FirebaseUtil;
import com.example.easychat.utils.MessageType;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.util.Objects;

public class ChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatMessageModel, ChatRecyclerAdapter.ChatModelViewHolder> {
    Context context;
    Activity activity;

    public ChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatMessageModel> options, Context context, Activity activity) {
        super(options);
        this.context = context;
        this.activity = activity;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatModelViewHolder holder, int position, @NonNull ChatMessageModel model) {
        if (model.massageType != null) {
            if (model.massageType.equals(MessageType.TEXT.toString())) {
                holder.textMsgLayout.setVisibility(View.VISIBLE);
                holder.voiceMsgLayout.setVisibility(View.GONE);
                holder.imageMsgLayout.setVisibility(View.GONE);
                holder.cardViewLayout.setVisibility(View.GONE);
                if (model.getSenderId().equals(FirebaseUtil.currentUserId())) {
                    holder.leftChatLayout.setVisibility(View.GONE);
                    holder.rightChatLayout.setVisibility(View.VISIBLE);
                    holder.rightChatTextview.setText(model.getMessage());
                } else {
                    holder.rightChatLayout.setVisibility(View.GONE);
                    holder.leftChatLayout.setVisibility(View.VISIBLE);
                    holder.leftChatTextview.setText(model.getMessage());
                }
            } else if (model.massageType.equals(MessageType.VOICE.toString())) {
                holder.textMsgLayout.setVisibility(View.GONE);
                holder.voiceMsgLayout.setVisibility(View.VISIBLE);
                holder.imageMsgLayout.setVisibility(View.GONE);
                holder.cardViewLayout.setVisibility(View.GONE);
                if (model.getSenderId().equals(FirebaseUtil.currentUserId())) {
                    holder.voicePlayerViewSend.setVisibility(View.VISIBLE);
                    holder.voicePlayerViewReceive.setVisibility(View.GONE);
                    holder.voicePlayerViewSend.setAudio(model.getMessage());

                } else {
                    holder.voicePlayerViewSend.setVisibility(View.GONE);
                    holder.voicePlayerViewReceive.setVisibility(View.VISIBLE);
                    holder.voicePlayerViewReceive.setAudio(model.getMessage());

                }
            } else if (model.massageType.equals(MessageType.CARD.toString())) {
                holder.textMsgLayout.setVisibility(View.GONE);
                holder.voiceMsgLayout.setVisibility(View.GONE);
                holder.imageMsgLayout.setVisibility(View.GONE);
                holder.cardViewLayout.setVisibility(View.VISIBLE);

                String message = model.getMessage();
                Log.d("ChatRecyclerAdapter", "Message: " + message);
                String[] messageParts = message.split(",");
                for (String part : messageParts) {
                    Log.d("ChatRecyclerAdapter", "Message Part: " + part);
                }
                if (messageParts.length >= 3) {
                    String title = extractValue(messageParts[0]);
                    String description = extractValue(messageParts[1]);
                    String imageUrl = "https:" + extractValueLink(messageParts[2]);
                    Log.d("ChatRecyclerAdapter", "Title: " + title);
                    Log.d("ChatRecyclerAdapter", "Description: " + description);
                    Log.d("ChatRecyclerAdapter", "Image URL: " + imageUrl);

                    if (model.getSenderId().equals(FirebaseUtil.currentUserId())) {
                        holder.rightCardLayout.setVisibility(View.VISIBLE);
                        holder.leftCardLayout.setVisibility(View.GONE);

                        holder.itemNameSender.setText(title);
                        holder.itemDescSender.setText(description);
                        Glide.with(context).load(imageUrl).into(holder.itemImageSender);

                    } else {
                        holder.leftCardLayout.setVisibility(View.VISIBLE);
                        holder.rightCardLayout.setVisibility(View.GONE);

                        holder.itemNameReceiver.setText(title);
                        holder.itemDescReceiver.setText(description);
                        Glide.with(context).load(imageUrl).into(holder.itemImageReceiver);
                    }
                }
               /* if (model.getSenderId().equals(FirebaseUtil.currentUserId())) {
                    holder.rightCardLayout.setVisibility(View.VISIBLE);
                    holder.leftCardLayout.setVisibility(View.GONE);

                } else {
                    holder.leftCardLayout.setVisibility(View.VISIBLE);
                    holder.rightCardLayout.setVisibility(View.GONE);
                }*/
            } else if (model.massageType.equals(MessageType.IMAGE.toString())) {
                holder.textMsgLayout.setVisibility(View.GONE);
                holder.voiceMsgLayout.setVisibility(View.GONE);
                holder.imageMsgLayout.setVisibility(View.VISIBLE);
                holder.cardViewLayout.setVisibility(View.GONE);
                if (model.getSenderId().equals(FirebaseUtil.currentUserId())) {
                    holder.cardViewSend.setVisibility(View.VISIBLE);
                    holder.cardViewReceive.setVisibility(View.GONE);
                    holder.sendImage.setImageURI(Uri.parse(model.getMessage()));
                    Glide.with(context).load(model.getMessage()).into(holder.sendImage);
                } else {
                    holder.cardViewSend.setVisibility(View.GONE);
                    holder.cardViewReceive.setVisibility(View.VISIBLE);
                    holder.receiveImage.setImageURI(Uri.parse(model.getMessage()));
                    Glide.with(context).load(model.getMessage()).into(holder.receiveImage);
                }
            }
        }

        holder.leftChatSpeak.setOnClickListener(v -> {
            if (soundIconClickListener != null) {
                soundIconClickListener.onSoundIconClick(model.getMessage());
            }
        });
        holder.rightChatSpeak.setOnClickListener(v -> {
            if (soundIconClickListener != null) {
                soundIconClickListener.onSoundIconClick(model.getMessage());
            }
        });
        holder.leftSpeechToText.setOnClickListener(v->{
            if (soundIconClickListener!=null){
                soundIconClickListener.onSpeechToTextClick(model.getMessage());
            }
        });
        holder.rightSpeechToText.setOnClickListener(v->{
            if (soundIconClickListener!=null){
                soundIconClickListener.onSpeechToTextClick(model.getMessage());
            }
        });
    }

    private String extractValue(String part) {
        String[] keyValue = part.split(":");
        if (keyValue.length == 2) {
            return keyValue[1].trim();
        } else {
            return "";
        }
    }

    private String extractValueLink(String part) {
        String[] keyValue = part.split(":");
        if (keyValue.length == 3) {
            return keyValue[2].trim();
        } else {
            return "";
        }
    }


    @NonNull
    @Override
    public ChatModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_message_recycler_row, parent, false);
        return new ChatModelViewHolder(view, activity);
    }

    static class ChatModelViewHolder extends RecyclerView.ViewHolder {

        LinearLayout leftChatLayout, rightChatLayout, voiceMsgLayout, imageMsgLayout, cardViewLayout;
        TextView leftChatTextview, rightChatTextview, itemNameReceiver, itemNameSender, itemDescReceiver, itemDescSender;
        ImageView leftChatSpeak, rightChatSpeak, receiveImage, sendImage, itemImageReceiver, itemImageSender,leftSpeechToText,rightSpeechToText;
        RelativeLayout textMsgLayout;
        VoicePlayerView voicePlayerViewReceive, voicePlayerViewSend;

        CardView cardViewSend, cardViewReceive, leftCardLayout, rightCardLayout;


        public ChatModelViewHolder(@NonNull View itemView, Activity activity) {
            super(itemView);

            leftChatSpeak = itemView.findViewById(R.id.left_chat_speak);
            leftChatLayout = itemView.findViewById(R.id.left_chat_layout);
            rightChatSpeak = itemView.findViewById(R.id.right_chat_speak);
            imageMsgLayout = itemView.findViewById(R.id.image_msg_layout);
            rightChatLayout = itemView.findViewById(R.id.right_chat_layout);
            textMsgLayout = itemView.findViewById(R.id.text_message_layout);
            leftChatTextview = itemView.findViewById(R.id.left_chat_textview);
            voiceMsgLayout = itemView.findViewById(R.id.voice_message_layout);
            rightChatTextview = itemView.findViewById(R.id.right_chat_textview);
            voicePlayerViewReceive = itemView.findViewById(R.id.voicePlayerViewReceive);
            voicePlayerViewSend = itemView.findViewById(R.id.voicePlayerViewSend);
            receiveImage = itemView.findViewById(R.id.receiveImage);
            sendImage = itemView.findViewById(R.id.sendImage);

            cardViewSend = itemView.findViewById(R.id.cardViewSendImg);
            cardViewReceive = itemView.findViewById(R.id.cardViewReceiveImg);
            cardViewLayout = itemView.findViewById(R.id.card_msg_layout);
            leftCardLayout = itemView.findViewById(R.id.left_card_layout);
            rightCardLayout = itemView.findViewById(R.id.right_card_layout);
            itemNameReceiver = itemView.findViewById(R.id.itemDetailName);
            itemNameSender = itemView.findViewById(R.id.itemDetailName_sender);
            itemDescReceiver = itemView.findViewById(R.id.itemDetailDescription);
            itemDescSender = itemView.findViewById(R.id.itemDetailDescription_sender);
            itemImageReceiver = itemView.findViewById(R.id.itemImg);
            itemImageSender = itemView.findViewById(R.id.itemImg_sender);
            leftSpeechToText=itemView.findViewById(R.id.text_to_speech);
            rightSpeechToText=itemView.findViewById(R.id.text_to_speech_send);

            VoicePlayerView.getActivity(activity);
        }
    }

    public interface OnSoundIconClickListener {
        void onSoundIconClick(String message);
        void onSpeechToTextClick(String message);
    }

    private OnSoundIconClickListener soundIconClickListener;

    public void setSoundIconClickListener(OnSoundIconClickListener listener) {
        this.soundIconClickListener = listener;
    }

}

