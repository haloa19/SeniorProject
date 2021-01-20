package com.dteviot.epubviewer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dteviot.epubviewer.WebServer.ServerSocketThread;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import static android.R.attr.bitmap;
import static android.R.attr.path;

public class Main5Activity extends Activity implements TextToSpeech.OnInitListener {

    ImageView imageview;
    Bitmap bitmap;
    String path;

    //------소켓통신------//
    TextView infoIp, infoPort;
    static final int SocketServerPORT = 8080;
    ServerSocket serverSocket;

    ServerSocketThread serverSocketThread;

    private TextToSpeech mTTS;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main5);

        infoIp = (TextView) findViewById(R.id.infoip);
        infoPort = (TextView) findViewById(R.id.infoport);

        infoIp.setText(getIpAddress());

        serverSocketThread = new ServerSocketThread();
        serverSocketThread.start();

        Intent intent = getIntent();

        /* 값전달 테스트용 */
        Log.d("TEST", intent.getExtras().getString("test"));
        Log.d("KEY", intent.getExtras().getString("key"));
        Log.d("파일네임 : ",Gloval.getB_name());


        mTTS = new TextToSpeech(getApplicationContext(), this);

        path = Environment.getExternalStorageDirectory().getAbsolutePath();
        bitmap = BitmapFactory.decodeFile(path + "/Download/"+Gloval.getB_name()+"/OEBPS/Images/"+intent.getExtras().getString("key")+".png");
        imageview = (ImageView)findViewById(R.id.imageview);
        imageview.setImageBitmap(bitmap);   //가지고 온 이미지 띄어주기
    }


//소켓통신9
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "SiteLocalAddress: "
                                + inetAddress.getHostAddress() + "\n";
                    }
                }
            }
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }

    public class ServerSocketThread extends Thread {

        @Override
        public void run() {
            Socket socket = null;

            try {
                serverSocket = new ServerSocket(SocketServerPORT);
                Main5Activity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        infoPort.setText("I'm waiting here: "
                                + serverSocket.getLocalPort());
                    }});

                while (true) {
                    socket = serverSocket.accept();
                    FileTxThread fileTxThread = new FileTxThread(socket);
                    fileTxThread.start();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    public class FileTxThread extends Thread {
        Socket socket;
        String string;
        FileTxThread(Socket socket){
            this.socket= socket;
        }
        @Override
        public void run() {
            File file = new File(
                    Environment.getExternalStorageDirectory().getAbsolutePath()+"/rabbit.png");

            byte[] bytes = new byte[(int) file.length()];
            BufferedInputStream bis;
            try {

                bis = new BufferedInputStream(new FileInputStream(file));
                bis.read(bytes, 0, bytes.length);

                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeObject(bytes);
                oos.flush();
                socket.close();

                final String sentMsg = "File sent to: " + socket.getInetAddress();
                Main5Activity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(Main5Activity.this,
                                sentMsg,
                                Toast.LENGTH_LONG).show();
                    }});

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    public void onInit(int i){
        mTTS.speak("촉각그래픽 디스플레이로 사진전송을 위한 블루투스 설정 화면입니다." +
                "블루투스사용을위해 화면을 터치해 주시기바랍니다.", TextToSpeech.QUEUE_FLUSH, null);
    }
}
