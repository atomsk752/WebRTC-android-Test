package com.honeyrock.webrtctest;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import de.tavendo.autobahn.WebSocket;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketConnectionHandler;
import de.tavendo.autobahn.WebSocketException;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {

    private final WebSocketConnection mConnection = new WebSocketConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = findViewById(R.id.button1);
        Button button2 = findViewById(R.id.button2);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectWebSocket();


            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VideoDialog dialog = new VideoDialog(MainActivity.this);
                dialog.setCancelable(false);
                dialog.show();
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);

                Window window = dialog.getWindow();

                int x = (int)(size.x * 0.9f);
                int y = (int)(size.y * 0.9f);

                window.setLayout(x,y);

            }
        });

    }


    private void connectWebSocket() {

        URI uri;

        try {

            uri = new URI("ws://101.55.28.135:3333/app/hello2");

        } catch (URISyntaxException e) {

            e.printStackTrace();

            return;

        }

        SocketClient socketClient = new SocketClient(uri, getApplicationContext());
        socketClient.connect();



    }

    private void start(){
        final String ws = "ws://101.55.28.135:3333/app/hello2";
        URI wsuri = URI.create(ws);

        try {
            Socket sk = IO.socket(ws);
            sk.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("command","request_offer");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    sk.emit("offer", jsonObject);
                }
            }).on("offer", new Emitter.Listener() {
                @Override
                public void call(Object... args) {

                    Log.i("skconnect========", Arrays.toString(args));
                }
            });
            sk.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
/*
        try {

            mConnection.connect(wsuri, new WebSocketConnectionHandler() {

                @Override
                public void onOpen() {
                    Log.d("ws", "Status: Connected to " + wsuri);

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("command","request_offer");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mConnection.sendTextMessage(jsonObject.toString());
                }

                @Override
                public void onTextMessage(String payload) {
                    Log.d("ws", "Got echo: " + payload);
                }

            });
        } catch (WebSocketException e) {

            Log.d("ws", e.toString());
        }*/
    }

}