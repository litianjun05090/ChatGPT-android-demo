package com.example.gpt;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gpt.ChatManager;
import com.example.gpt.ChatManager.ChatMessage;
import com.example.gpt.ChatManager.ChatMessage.ChatRole;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {

    private static final String API_KEY="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";
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

        //配置recycle view
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
                messageAdapter.notifyDataSetChanged();//通知适配器数据已更新
                //将RecyclerView滚动到最新消息的位置,每次新消息添加到聊天列表时，RecyclerView都会滚动到最底部
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });

    }


    void callAPI(String question) {
        //okhttp
        sendButton.setImageResource(R.drawable.ic_send_disable);
        sendButton.setEnabled(false);

        chatApiBuffer="";
        messageList.add(new Message("正在回复~~", Message.SEND_BY_BOT));
        multiChatList.add(new ChatMessage(ChatRole.ASSISTANT).setText(question));
        chatApiClient.sendPromptList(multiChatList);

    }





}