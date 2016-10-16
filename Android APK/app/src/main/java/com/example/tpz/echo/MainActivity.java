package com.example.tpz.echo;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.pusher.client.Pusher;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.SubscriptionEventListener;
import com.google.gson.Gson;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    private TextView speech_output;
    private ImageButton btn_output;
    private final int SPEECH_REQUEST_CODE = 123;

    //Declare keyword to send
    Keyword keyword;
    //Declare result to receive
    Result result = null;
    public static ResultArray resultArray = new ResultArray();

    final String MESSAGES_ENDPOINT = "http://pusher-chat-demo.herokuapp.com";

    MessageAdapter messageAdapter;
    EditText messageInput;
    Button sendButton;

    //User reply indication
    boolean waitingReply = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        speech_output = (TextView) findViewById(R.id.speech_output);
        btn_output = (ImageButton) findViewById(R.id.btn_speak);
//        btn_output.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String text = messageInput.getText().toString();
//                MessageAdapter.IsUser(true);
//                postMessage(text);
//                if(waitingReply && android.text.TextUtils.isDigitsOnly(text)){
//                    waitingReply = false;
//                    int choice = Integer.parseInt(text);
//                    resultArray.context_id.add(result.context_id.get(choice));
//                    resultArray.context_action.add(result.context_action.get(choice));
//                    text = "";
//                }
//                keyword = new Keyword(text);
//                KeywordToResult(keyword);
//            }
//        });

        //Pressed send button
        btn_output.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showGoogleInputDialog();
                return false;
            }
        });
        //Pressed Enter
        messageInput = (EditText) findViewById(R.id.message_input);
        messageInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP){
                    String text = messageInput.getText().toString();
                    MessageAdapter.IsUser(true);
                    postMessage(text);
                    if(waitingReply && android.text.TextUtils.isDigitsOnly(text)){
                        waitingReply = false;
                        int choice = Integer.parseInt(text);
                        resultArray.context_id.add(result.context_id.get(choice - 1));
                        resultArray.context_action.add(result.context_action.get(choice - 1));
                        text = "";
                    }
                    keyword = new Keyword(text);
                    KeywordToResult(keyword);
                }
                return true;
            }
        });

        sendButton = (Button) findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = messageInput.getText().toString();
                MessageAdapter.IsUser(true);
                postMessage(text);
                if(waitingReply && android.text.TextUtils.isDigitsOnly(text)){
                    waitingReply = false;
                    int choice = Integer.parseInt(text);
                    resultArray.context_id.add(result.context_id.get(choice));
                    resultArray.context_action.add(result.context_action.get(choice));
                    text = "";
                }
                keyword = new Keyword(text);
                KeywordToResult(keyword);
            }
        });


        messageAdapter = new MessageAdapter(this, new ArrayList<Message>());
        final ListView messagesView = (ListView) findViewById(R.id.messages_view);
        messagesView.setAdapter(messageAdapter);

        Pusher pusher = new Pusher("faa685e4bb3003eb825c");

        pusher.connect();

        Channel channel = pusher.subscribe("messages");

        channel.bind("new_message", new SubscriptionEventListener() {
            @Override
            public void onEvent(String channelName, String eventName, final String data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Gson gson = new Gson();
                        Message message = gson.fromJson(data, Message.class);
                        messageAdapter.add(message);
                        messagesView.setSelection(messageAdapter.getCount() - 1);
                    }

                });
            }

        });

        btn_output = (ImageButton) findViewById(R.id.btn_speak);
        btn_output.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGoogleInputDialog();
            }
        });

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                UpdateQuestion();
            }
        }, 1000);
    }

    public void showGoogleInputDialog() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "Your device is not supported!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SPEECH_REQUEST_CODE: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> textResult = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    //speech_output.setText(result.get(0));

//                    DataToKeyword(result.get(0));
//                    if(keyword != null) {
//                        KeywordToResult(keyword);
//                        speech_output.setText(result.toString());
//                    }else
//                        speech_output.setText(result.get(0));
                    //Input question verbally
                    String text = textResult.get(0);
                    MessageAdapter.IsUser(true);
                    postMessage(text);
                    if(waitingReply && android.text.TextUtils.isDigitsOnly(text)){
                        waitingReply = false;
                        int choice = Integer.parseInt(text);
                        resultArray.context_id.add(result.context_id.get(choice));
                        resultArray.context_action.add(result.context_action.get(choice));
                        text = "";
                    }
                    keyword = new Keyword(text);
                    KeywordToResult(keyword);
                }
                break;
            }

        }
    }

//    private Keyword DataToKeyword(String data){
//        ServerRequests serverRequests = new ServerRequests(this);
//        serverRequests.fetchDataInBackground(data, new GetDataCallback() {
//            @Override
//            public void done(Keyword tampKeyword) {
//                keyword = tampKeyword;
//            }
//        });
//        return keyword;
//    }

    private Result KeywordToResult(Keyword keyword){
        result = null;
        ServerRequests serverRequests = new ServerRequests(this);
        serverRequests.fetchKeywordInBackground(keyword, new GetKeywordCallback() {
            @Override
            public void done(Result tempResult) {
                result = tempResult;
                if(result != null){
                    String tempStr = "";
                    if(result.name.size() != 0) {
                        for (int i = 0; i < result.name.size(); i++) {
                            tempStr += "\n" + Integer.toString(i + 1) + ". " + result.name.get(i).toString();
                        }
                        //Output Answer
                        MessageAdapter.IsUser(false);
                        //Waiting Reply
                        waitingReply = true;
                    }else{
                        tempStr = result.original;
                        waitingReply = false;
                    }
                    postMessage(tempStr);
                    if(tempResult.name.equals("") && tempResult.original.equals("")){
                        postMessage("Hmm...");
                    }
                }
            }
        });
        return result;
    }

    private void postMessage(String text)  {

        if (text.equals("")) {
            return;
        }
        RequestParams params = new RequestParams();
        params.put("text", text);
        params.put("time", new Date().getTime());

        AsyncHttpClient client = new AsyncHttpClient();

        client.post(MESSAGES_ENDPOINT + "/messages", params, new JsonHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        messageInput.setText("");
                    }
                });
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(getApplicationContext(), "Something went wrong :(", Toast.LENGTH_LONG).show();
            }
        });

    }
    private void UpdateQuestion(){
        String question = "";
        switch (resultArray.context_id.size()){
            case 0:
                question = "What hotel do you want?";
                break;
        }
        MessageAdapter.IsUser(false);
        postMessage(question);
    }

}
