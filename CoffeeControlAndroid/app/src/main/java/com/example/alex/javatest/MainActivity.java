package com.example.alex.javatest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

    /** Called when the activity is first created. */
    private Button btn_receive;
    private Button btn_stop;
    private TextView txt;
    private TextView txt2;
    private String line;
    private int state;

    private static final String HOST = "zeus.vse.gmu.edu";
    private static final int PORT = 5021;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        state = 0;
        initControl();
    }

    private void initControl() {
        btn_receive = (Button) findViewById(R.id.btn_receive);
        btn_stop = (Button) findViewById(R.id.btn_stop);
        txt = (TextView) findViewById(R.id.response);
        txt2= (TextView) findViewById(R.id.response2);
        btn_stop.setOnClickListener(new ReceiverListener());
        btn_receive.setOnClickListener(new ReceiverListener());
    }


    @SuppressLint("HandlerLeak")
    class ReceiverListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            if(v.equals((View)btn_receive)) {
                new Thread() {
                    @Override
                    public void run() {
                        if(state == 5 || state == 9){
                            state = 1;
                        }

                        try {
                            // Create Socket instance
                            Socket socket = new Socket(HOST, PORT);
                            socket.setKeepAlive(true);
                            // Get input buffer
                            boolean doConn;
                            if (socket.isConnected())
                                doConn = true;
                            else
                                doConn = false;

                            while (doConn) {

                                if (socket.isClosed())
                                    socket = new Socket(HOST, PORT);

                                DataOutputStream DOS = new DataOutputStream(socket.getOutputStream());
                                byte[] input = {(byte) 255, (byte) 255, 1, (byte) state};// 1 is the id of the phone
                                //1 is to start brewing
                                DOS.write(input);


                                BufferedReader br = new BufferedReader(
                                        new InputStreamReader(socket.getInputStream()));

                                String read = br.readLine();
                                line = read;

                                handler.sendEmptyMessage(0);

                                if (!read.isEmpty()) {
                                    if (read.charAt(0) == 'D') {
                                        if (read.charAt(1) == '1') {
                                            state = 1;
                                            handler.sendEmptyMessage(0);
                                        } else if (read.charAt(1) == '5') {
                                            state = 5;
                                            doConn = false;
                                        }
                                        else if(read.charAt(1) == '9') {
                                            state = 9;
                                        }
                                    }
                                }


                                DOS.flush();
                                DOS.close();
                                br.close();
                            }

                            socket.close();
                        } catch (UnknownHostException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        handler.sendEmptyMessage(0);
                    }

                }.start();
            } else {
                new Thread() {
                    @Override
                    public void run() {
                        state = 5;
                        handler.sendEmptyMessage(0);
                    }
                }.start();
            }
        }

    }

    // Define Handler object
    private Handler handler = new Handler() {
        @Override
        // When there is message, execute this method
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // Update UI
            if(state == 1) {
                txt2.setText("Temp: " + line);
                txt.setText("State: coffee BREWING");
            }
            else if(state == 0){
                txt.setText("State: coffee IDLE");
            }
            else if(state == 5){
                txt.setText("State: coffee STOPPED");
            }
            else if(state == 9){
                txt.setText("State : coffee FINISHED");
            }
            //txt2.append(line);
            Log.i("PDA", "----->" + line);
        }
    };

}