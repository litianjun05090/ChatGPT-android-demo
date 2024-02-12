package com.example.gpt;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gpt.ChatManager.ChatMessage;
import com.example.gpt.ChatManager.ChatMessage.ChatRole;

import java.util.ArrayList;
import java.util.List;




public class MainActivity extends AppCompatActivity {

    private static final String API_KEY="";     //your api key
    RecyclerView recyclerView;
    TextView welcomeTextView;
    EditText messageEditText;
    ImageButton sendButton;
    List<Message> messageList;
    MessageAdapter messageAdapter;
    ChatApiClient chatApiClient = null;
    private ChatManager.MessageList multiChatList = new ChatManager.MessageList();
    private Handler handler = new Handler();
    private String chatApiBuffer = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageList = new ArrayList<>();

        recyclerView = findViewById(R.id.recycler_view);
        welcomeTextView = findViewById(R.id.welcome_text);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_btn);

        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);

        sendButton.setOnClickListener(v -> {
            String question = messageEditText.getText().toString().trim();
            addToChat(question, Message.SEND_BY_ME);
            messageEditText.setText("");
            callAPI(question);
            welcomeTextView.setVisibility(View.GONE);
        });


        chatApiClient=new ChatApiClient(this, "https://api.openai.com/",
                API_KEY, "gpt-3.5-turbo", new ChatApiClient.OnReceiveListener() {
            @Override
            public void onMsgReceive(String message) {
                chatApiBuffer+=message;
                handler.post(()->{
                    messageList.get(messageList.size()-1).setMessage(chatApiBuffer);
                    messageAdapter.notifyDataSetChanged();
                });
                System.out.println("onMsgReceive");
            }

            @Override
            public void onError(String message) {

            }

            @Override
            public void onFunctionCall(String name, String arg) {

            }

            @Override
            public void onFinished(boolean completed) {
                handler.post(()->{
                    sendButton.setImageResource(R.drawable.baseline_send_24);
                    sendButton.setEnabled(true);
                });
            }
        });

    }

    void addToChat(String message, String sendBy) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new Message(message, sendBy));
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });

    }


    void callAPI(String question) {
        //okhttp
        sendButton.setImageResource(R.drawable.ic_send_disable);
        sendButton.setEnabled(false);

        chatApiBuffer="";
        messageList.add(new Message("Responding...", Message.SEND_BY_BOT));
        multiChatList.add(new ChatMessage(ChatRole.ASSISTANT).setText(question));
        chatApiClient.sendPromptList(multiChatList);

    }





}