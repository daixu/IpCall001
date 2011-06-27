package com.sqt001.ipcall.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.sqt001.ipcall.R;
import com.sqt001.ipcall.util.DBUtil;
import com.sqt001.ipcall.util.SoftObj;

public class RecommendColumnActivity extends Activity {
  private ListView mListView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mListView = new ListView(this);

    if ((getData().size() > 0) && (!getData().equals(""))) {
      SimpleAdapter adapter = new SimpleAdapter(this, getData(), R.layout.recommendcolumn_item, 
          new String[] { "title", "message" }, new int[] { R.id.tv1, R.id.tv2 });
      adapter.notifyDataSetChanged();
      mListView.setAdapter(adapter);
      setContentView(mListView);
      mListView.setOnItemClickListener(new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> apapter, View arg1, final int position, long id) {
          final ArrayList<SoftObj> lst = new DBUtil(RecommendColumnActivity.this).readAll();
          new AlertDialog.Builder(RecommendColumnActivity.this).setTitle(lst.get(position).getTitle().trim())
              .setMessage(lst.get(position).getMessage().trim())
              .setPositiveButton(R.string.access, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  Uri uri = Uri.parse(lst.get(position).getUrl());
                  Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                  startActivity(intent);
                }
              }).setNegativeButton(R.string.back, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
              })
              .create()
              .show();
        }
      });
    }
  }

  private List<Map<String, String>> getData() {
    ArrayList<Map<String, String>> table = new ArrayList<Map<String, String>>();
    ArrayList<SoftObj> lst = new DBUtil(RecommendColumnActivity.this).readAll();
    SoftObj so = null;
    for (int i = 0; i < lst.size(); i++) {
      so = lst.get(i);
      Map<String, String> item = new HashMap<String, String>();
      item.put("title", so.getTitle());
      item.put("message", so.getMessage());
      table.add(item);
    }
    return table;
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
      new AlertDialog.Builder(RecommendColumnActivity.this)
          .setTitle(R.string.exit)
          .setIcon(android.R.drawable.ic_menu_info_details)
          .setMessage(R.string.really_exit)
          .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              finish();
              System.exit(0);
            }
          }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
          })
          .create()
          .show();
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }
}
