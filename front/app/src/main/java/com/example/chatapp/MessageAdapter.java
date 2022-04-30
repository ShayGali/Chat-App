package com.example.chatapp;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The adapter of the messages of the recycler view
 */
public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_MESSAGE_SENT = 0;
    private static final int TYPE_MESSAGE_RECEIVED = 1;
    private static final int TYPE_IMG_SENT = 2;
    private static final int TYPE_IMG_RECEIVED = 3;


    private LayoutInflater inflater;
    private List<JSONObject> messages;

    public MessageAdapter(LayoutInflater inflater) {
        this.inflater = inflater;
        messages = new ArrayList<>();
    }

    // What will be the layout of the message
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case TYPE_MESSAGE_SENT:
                view = inflater.inflate(R.layout.item_sent_message, parent, false);
                return new SentMessageHolder(view);
            case TYPE_MESSAGE_RECEIVED:
                view = inflater.inflate(R.layout.item_received_message, parent, false);
                return new ReceivedMessageHolder(view);
            case TYPE_IMG_SENT:
                view = inflater.inflate(R.layout.item_sent_img, parent, false);
                return new SentImageHolder(view);
            case TYPE_IMG_RECEIVED:
                view = inflater.inflate(R.layout.item_received_img, parent, false);
                return new ReceivedImageHolder(view);
        }

        return null;
    }

    // The data that will be in the layout
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        JSONObject message = messages.get(position);
        try {
            if (message.getBoolean("isSent")) {
                if (message.has("message")) {
                    SentMessageHolder sentMessageHolder = (SentMessageHolder) holder;
                    sentMessageHolder.messageTxt.setText(message.getString("message"));
                } else if (message.has("image")) {
                    SentImageHolder sentImageHolder = (SentImageHolder) holder;
                    Bitmap image = getBitmapFromString(message.getString("image"));
                    sentImageHolder.imageView.setImageBitmap(image);
                }
            } else {
                if (message.has("message")) {
                    ReceivedMessageHolder receivedMessageHolder = (ReceivedMessageHolder) holder;
                    receivedMessageHolder.messageTxt.setText(message.getString("message"));
                    receivedMessageHolder.name.setText(message.getString("name"));
                } else if (message.has("image")) {
                    ReceivedImageHolder receivedImageHolder = (ReceivedImageHolder) holder;
                    Bitmap image = getBitmapFromString(message.getString("image"));
                    receivedImageHolder.imageView.setImageBitmap(image);
                    receivedImageHolder.name.setText(message.getString("name"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Convert string to Bitmap, for the images
     *
     * @param image string image in base64 format the get from the JSONObject
     * @return Bitmap of the image
     */
    private Bitmap getBitmapFromString(String image) {
        byte[] bytes = Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // The type of the view
    @Override
    public int getItemViewType(int position) {
        JSONObject message = messages.get(position);
        try {
            if (message.getBoolean("isSent")) {
                if (message.has("message"))
                    return TYPE_MESSAGE_SENT;
                else if (message.has("image")) return TYPE_IMG_SENT;
            } else {
                if (message.has("message"))
                    return TYPE_MESSAGE_RECEIVED;
                else if (message.has("image")) return TYPE_IMG_RECEIVED;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return -1;
    }


    /**
     * Add a message to the list
     *
     * @param jsonObject
     */
    @SuppressLint("NotifyDataSetChanged")
    public void addMessage(JSONObject jsonObject) {
        messages.add(jsonObject);
        notifyDataSetChanged();
    }

    /*
    The classes below are the classes that declare what data is in each message,
    and find the Views with the ID.
     */
    private static class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageTxt;

        public SentMessageHolder(@NonNull View itemView) {
            super(itemView);
            messageTxt = itemView.findViewById(R.id.sent_text);
        }
    }

    private static class SentImageHolder extends RecyclerView.ViewHolder {

        ImageView imageView;


        public SentImageHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }

    private static class ReceivedMessageHolder extends RecyclerView.ViewHolder {

        TextView name, messageTxt;


        public ReceivedMessageHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name_txt);
            messageTxt = itemView.findViewById(R.id.sent_text);
        }
    }

    private static class ReceivedImageHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView name;

        public ReceivedImageHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            name = itemView.findViewById(R.id.name_txt);
        }

    }
}
