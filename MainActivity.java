package com.dteviot.epubviewer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.dteviot.epubviewer.WebServer.FileRequestHandler;
import com.dteviot.epubviewer.WebServer.ServerSocketThread;
import com.dteviot.epubviewer.WebServer.WebServer;
import com.dteviot.epubviewer.epub.Book;
import com.dteviot.epubviewer.epub.TableOfContents;

import java.util.ArrayList;

import nl.siegmann.epublib.epub.Main;

public class MainActivity extends Activity implements IResourceSource {
    private final static int LIST_EPUB_ACTIVITY_ID = 0; 
    private final static int LIST_CHAPTER_ACTIVITY_ID = 1; 
    private final static int CHECK_TTS_ACTIVITY_ID = 2; 
    
    public static final String BOOKMARK_EXTRA = "BOOKMARK_EXTRA";

    Intent i;
    SpeechRecognizer mRecognizer;

    /*
     * the app's main view
     */
    EpubWebView mEpubWebView;
    TextToSpeechWrapper mTtsWrapper;
    private ServerSocketThread mWebServerThread = null;
  
    Button button;

    static final int SocketServerPORT = 8080;
    static final String SocketServerAddress = "192.168.0.29";

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEpubWebView = (EpubWebView) findViewById(R.id.webview);
        mEpubWebView = createView();
        setContentView(mEpubWebView);
        mTtsWrapper = new TextToSpeechWrapper();
        mWebServerThread = createWebServer();
        mWebServerThread.startThread();

        i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(listener);

        mEpubWebView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                mRecognizer.startListening(i);
                return true;
                //웹뷰에서 setOnLongClickListener사용시에는 return true로 설정해줘야함
            }
        });


        button = (Button)findViewById(R.id.btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ImageActivity.class);
                startActivity(intent);
            }
        });

        ClientRxThread clientRxThread = new ClientRxThread(/*address.getText().toString()*/SocketServerAddress, SocketServerPORT);
        clientRxThread.start();

       
        //TestCases.run(this);
        if (savedInstanceState != null) {

            mEpubWebView.gotoBookmark(new Bookmark(savedInstanceState));
        } else {
            Bookmark bookmark = new Bookmark(this);
            if (bookmark.isEmpty()) {
                launchBookList();
            } else {
                mEpubWebView.gotoBookmark(bookmark);
            }
        }

    }

    public RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {

        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int error) {

        }
        SpeechRecognizer mRecognizer;

        @Override
        public void onResults(Bundle results) {

            String key;
            key = SpeechRecognizer.RESULTS_RECOGNITION;

            ArrayList<String> mResult = results.getStringArrayList(key);

            Intent intent = new Intent(MainActivity.this, Main5Activity.class);
            intent.putExtra("key", mResult.get(1));
            startActivity(intent);

        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    };
  
  /*------------------------소켓통신-----------------------------------*/
    private class ClientRxThread extends Thread {
        String dstAddress;
        int dstPort;

        ClientRxThread(String address, int port) {
            dstAddress = address;
            dstPort = port;
        }

        @Override
        public void run() {
            Socket socket = null;

            try {
                socket = new Socket(dstAddress, dstPort);


                //File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/bread.png");

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String str = in.readLine();
                //System.out.println(str);
                //Log.d("tttttt", str);
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);

                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+str+".png");

                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                byte[] bytes;
                FileOutputStream fos = null;
                try {
                    bytes = (byte[])ois.readObject();
                    fos = new FileOutputStream(file);
                    fos.write(bytes);
                } catch (ClassNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    if(fos!=null){
                        fos.close();
                    }

                }

                socket.close();

                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,
                                "Finished",
                                Toast.LENGTH_LONG).show();
                    }});

            } catch (IOException e) {

                e.printStackTrace();

                final String eMsg = "Something wrong: " + e.getMessage();
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,
                                eMsg,
                                Toast.LENGTH_LONG).show();
                    }});

            } finally {
                if(socket != null){
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

/*----------------------------여기까지 소켓통신---------------------------------*/

    
    private ServerSocketThread createWebServer() {
        FileRequestHandler handler = new FileRequestHandler(this);
        WebServer server = new WebServer(handler);
        return new ServerSocketThread(server, Globals.WEB_SERVER_PORT);
    }
    
    private EpubWebView createView() {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.GINGERBREAD_MR1) {
            return new EpubWebView23(this); 
        } else {
            return new EpubWebView30(this); 
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.menu_pick_epub:
            launchBookList();
            return true;
        case R.id.menu_bookmark:
            launchBookmarkDialog();
            return true;
        case R.id.menu_chapters:
            launchChaptersList();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void launchBookList() {

        Intent listComicsIntent = new Intent(MainActivity.this, Main3Activity.class);

        Intent intent_book = getIntent();
        String bookName = intent_book.getExtras().getString("BOOKNAME"); // 2Activity에서 책이름을 받아옴
        listComicsIntent.putExtra("BOOKNAME", bookName);

        startActivityForResult(listComicsIntent, LIST_EPUB_ACTIVITY_ID);

    }

    private void launchChaptersList() {
        Book book = getBook(); 
        if (book == null) {
            Utility.showToast(this, R.string.no_book_selected);
        } else {
            TableOfContents toc = book.getTableOfContents();
            if (toc.size() == 0) {
                Utility.showToast(this, R.string.table_of_contents_missing);
            } else {
                Intent listChaptersIntent = new Intent(this, ListChaptersActivity.class);
                toc.pack(listChaptersIntent, ListChaptersActivity.CHAPTERS_EXTRA);
                startActivityForResult(listChaptersIntent, LIST_CHAPTER_ACTIVITY_ID);
            }
        }
    }

    private void launchBookmarkDialog() {
        BookmarkDialog dlg = new BookmarkDialog(this);
        dlg.show();
        dlg.setSetBookmarkAction(mSaveBookmark);
        dlg.setGotoBookmarkAction(mGotoBookmark);
        dlg.setStartSpeechAction(mStartSpeech);
        dlg.setStopSpeechAction(mStopSpeech);
    }

    /*
     * Should return with epub or chapter to load
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHECK_TTS_ACTIVITY_ID) {
            mTtsWrapper.checkTestToSpeechResult(this, resultCode);
            return;
        }
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case LIST_EPUB_ACTIVITY_ID: // ID가 0이면
                    onListEpubResult(data);
                    break;

                case LIST_CHAPTER_ACTIVITY_ID:
                    onListChapterResult(data);
                    break;
                    
                default:
                    Utility.showToast(this, R.string.something_is_badly_broken);
            }
        } else if (resultCode == RESULT_CANCELED) {
            Utility.showErrorToast(this, data);
        }
    }

    private void onListEpubResult(Intent data) {

        String fileName = data.getStringExtra(Main3Activity.FILENAME_EXTRA);  //change
        loadEpub(fileName, null);
    }

    private void onListChapterResult(Intent data) {
        Uri chapterUri = data.getParcelableExtra(ListChaptersActivity.CHAPTER_EXTRA);
        mEpubWebView.loadChapter(chapterUri);
    }

    private void loadEpub(String fileName, Uri chapterUri) {
        mEpubWebView.setBook(fileName);
        mEpubWebView.loadChapter(chapterUri);
    }
    
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        Bookmark bookmark = mEpubWebView.getBookmark();
        if (bookmark != null) {
            bookmark.save(outState);
        }
    }

    private IAction mSaveBookmark = new IAction() {
        public void doAction() {
            Bookmark bookmark = mEpubWebView.getBookmark();
            if (bookmark != null) {
                bookmark.saveToSharedPreferences(MainActivity.this);
            }
        }
    };

    private IAction mGotoBookmark = new IAction() {
        public void doAction() {
            mEpubWebView.gotoBookmark(new Bookmark(MainActivity.this));
        }
    };

    private IAction mStartSpeech = new IAction() {
        public void doAction() {
            mTtsWrapper.checkTextToSpeech(MainActivity.this, CHECK_TTS_ACTIVITY_ID);
            mEpubWebView.speak(mTtsWrapper);
        }
    };
    
    private IAction mStopSpeech = new IAction() {
        public void doAction() {
            mTtsWrapper.stop();
        }
    };
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTtsWrapper.onDestroy();
        mWebServerThread.stopThread();
    }

    /*
     * Book currently being used.
     * (Hack to provide book to WebServer.)
     */
    public Book getBook() { 
        return mEpubWebView.getBook(); 
    }


    @Override
    public ResourceResponse fetch(Uri resourceUri) {
        return getBook().fetch(resourceUri);
    }
}