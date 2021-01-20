package com.dteviot.epubviewer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ListEpubActivity extends ListActivity {
    public static final String FILENAME_EXTRA = "FILENAME_EXTRA";
    public static final String PAGE_EXTRA = "PAGE_EXTRA";

    private ListView mListView;
    private EpubListAdapter mEpubListAdapter;
    private String mRootPath;
    private ArrayList<String> mFileNames;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Intent intent_book = getIntent();
        //bookName = intent_book.getExtras().getString("BOOKNAME"); //

        mListView = getListView(); // get the built-in ListView
        listEpubFiles();

        mEpubListAdapter = new EpubListAdapter(this, mFileNames);

        mListView.setAdapter(mEpubListAdapter);

    }

    /*
     * returns true if SD card storage (or equivalent) is available
     */
    private boolean isMediaAvailable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            //System.out.print("여기다");
            return true;
        } else {
            return Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
        }
    }

    /*
     * populate mFileNames with all files in Downloads directory of SD card
     */

    private void listEpubFiles() {
        mFileNames = new ArrayList<String>();
        if (!isMediaAvailable()) {
            Utility.finishWithError(this, R.string.sd_card_not_mounted);
        } else {
            File path = Environment
                    .getExternalStorageDirectory();
            mRootPath = path.toString();
            System.out.println("주소 : "+ mRootPath);
            String[] filesInDirectory = path.list();
            if (filesInDirectory != null) {
                for (String fileName : filesInDirectory) {
                    if (isEpubBookFile(fileName)) {
                        mFileNames.add(fileName);
                        //System.out.println("등록된 파일 이름 : " + fileName);
                    }
                }
            }
            if (mFileNames.isEmpty()) {
                Utility.finishWithError(this, R.string.no_epub_found);
            }
            Collections.sort(mFileNames);
        }
    }


    /*
     * returns true if filename ends in epub
     */

    private boolean isEpubBookFile(String fileName) {
        System.out.println("탐색한 파일 또는 디렉토리 이름 : "+fileName);
        // Pattern pattern = Pattern.compile(".*epub$");
        //  return pattern.matcher(fileName.toLowerCase()).matches();
        String lowerCasedName = fileName.toLowerCase();
        //System.out.println("소문자 이름 : "+lowerCasedName);
        if (lowerCasedName.endsWith(".epub")) {

            return true;
        }
        else{
            return false;
        }
    }

    private String titleToFileName(String title) {
        return mRootPath + "/" + title;
    }

    /*
     * Class implementing the "ViewHolder pattern", for better ListView
     * performance
     */
    private static class ViewHolder {
        public TextView textView; // refers to ListView item's TextView
    }

    /*
     * Populates entries on the list
     */
    private class EpubListAdapter extends ArrayAdapter<String> {
        private List<String> mTitles;
        private LayoutInflater mInflater;

        public EpubListAdapter(Context context, List<String> titles) {
            super(context, -1, titles);
            this.mTitles = titles;
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder; // holds references to current item's GUI

            // if convertView is null, inflate GUI and create ViewHolder;
            // otherwise, get existing ViewHolder
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.epub_list_item, null);
                viewHolder = new ViewHolder();

                viewHolder.textView = (TextView) convertView.findViewById(R.id.epub_title);
                convertView.setTag(viewHolder); // store as View's tag
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            // Populate the list item (view) with the comic's details
            String title = mTitles.get(position);
            viewHolder.textView.setText(title);
            return convertView;
        }
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        String fileName = titleToFileName(((TextView)v).getText().toString());
        System.out.println("주소체크 : " + fileName);
        Intent intent = new Intent();
        intent.putExtra(FILENAME_EXTRA, fileName);
        // set page to first, because ListChaptersActivity returns page to start at
        intent.putExtra(PAGE_EXTRA, 0);
        setResult(RESULT_OK, intent);

        finish();

    }
}