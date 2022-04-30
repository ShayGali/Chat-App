package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ChatActivity extends AppCompatActivity implements TextWatcher {

    private String name; // the name of the user

    private WebSocket webSocket; // the socket
    private static final String SERVER_PATH = "ws://echo.websocket.org"; // the url of the server  //demo echo socket - ws://echo.websocket.org

    private EditText messageEdit;  // the message input
    private View sendBtn, pickImgBtn; // the btn of sending message and image

    private RecyclerView recyclerView; // the recyclerView of the messages
    private MessageAdapter messageAdapter; // the recyclerView adapter

    private static final int IMAGE_REQUEST_ID = 1; // for getting image from the gallery


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        name = getIntent().getStringExtra("name"); //get name from the intent
        initializeSocketConnection();

    }

    /**
     * initialize the socket connection
     */
    private void initializeSocketConnection() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder() //okhttp3
                .url(SERVER_PATH).build();
        webSocket = client.newWebSocket(request, new SocketListener());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    // check if the message edit text have text, if true we set the btn to be sent and not the image picker
    @Override
    public void afterTextChanged(Editable s) {
        if (s.toString().trim().isEmpty())
            resetMessageEdit();
        else {
            sendBtn.setVisibility(View.VISIBLE);
            pickImgBtn.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * reset the btn and the messageEdit to default
     */
    private void resetMessageEdit() {
        messageEdit.removeTextChangedListener(this);

        messageEdit.setText("");
        sendBtn.setVisibility(View.INVISIBLE);
        pickImgBtn.setVisibility(View.VISIBLE);

        messageEdit.addTextChangedListener(this);
    }

    /**
     * Listen to socket opened and inputs
     */
    private class SocketListener extends WebSocketListener {

        // what will happen when the socket will open
        @Override
        public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
            super.onOpen(webSocket, response);
            runOnUiThread(() -> {
                Toast.makeText(ChatActivity.this, "Connection", Toast.LENGTH_SHORT).show();
                initializeView();
            });
        }

        // socket input
        @Override
        public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
            super.onMessage(webSocket, text);

            runOnUiThread(() -> {
                try {
                    JSONObject jsonObject = new JSONObject(text);
                    jsonObject.put("isSent", false);

                    messageAdapter.addMessage(jsonObject);

                    recyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });

        }
    }

    /**
     * When the socket is open the method will run
     * initialize the component on the activity
     */
    private void initializeView() {
        messageEdit = findViewById(R.id.edittest_message);
        sendBtn = findViewById(R.id.send_btn);
        pickImgBtn = findViewById(R.id.pick_img_btn);

        recyclerView = findViewById(R.id.recyclerview);
        messageAdapter = new MessageAdapter(getLayoutInflater());
        recyclerView.setAdapter(messageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        messageEdit.addTextChangedListener(this); // add listener to text changes

        sendBtn.setOnClickListener(v -> {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("name", name);
                jsonObject.put("message", messageEdit.getText().toString());
                jsonObject.put("isSent", true);

                webSocket.send(jsonObject.toString());

                messageAdapter.addMessage(jsonObject);
                recyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);

                resetMessageEdit();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });


        pickImgBtn.setOnClickListener(v -> { // open the gallery picker
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Pick image"), IMAGE_REQUEST_ID);
        });
    }

    // when the user pick the image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST_ID && resultCode == RESULT_OK) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(data.getData()); // get data stream
                Bitmap imageBitmap = BitmapFactory.decodeStream(inputStream); // convert the data stream to bitmap

                sendImage(imageBitmap); // send the image
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * send image on the socket.
     * the format will be JPEG, and the quality will be 50%
     * @param imageBitmap image in Bitmap object
     */
    private void sendImage(Bitmap imageBitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
        String base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);  // Base64 from android.util

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", name);
            jsonObject.put("image", base64String);
            jsonObject.put("isSent", true);

            webSocket.send(jsonObject.toString());

            messageAdapter.addMessage(jsonObject);
            recyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}