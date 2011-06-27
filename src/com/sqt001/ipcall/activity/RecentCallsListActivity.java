package com.sqt001.ipcall.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.sqt001.ipcall.R;
import com.sqt001.ipcall.contact.Account;
import com.sqt001.ipcall.provider.CallLogDbAdapter;
import com.sqt001.ipcall.provider.CallLogDbHelper;
import com.sqt001.ipcall.provider.Constants;
import com.sqt001.ipcall.provider.QuickContact;
import com.sqt001.ipcall.provider.QuickContactDbHelper;
import com.sqt001.ipcall.util.Tuple;

/**
 * 通话记录activity
 */
public class RecentCallsListActivity extends ListActivity {
	private CallLogDbAdapter mDb;
	private MyAdapter mAdapter;
	private Cursor mCursor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.recent_calls);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.recent_title);
		registerClickHandlerForListview();
		registerContextMemuForListview();
	}

	private void registerContextMemuForListview() {
		registerForContextMenu(getListView());
	}

	@Override
	public void onResume() {
		super.onResume();
		populateRecentCallList();
	}

	private void populateRecentCallList() {
		mDb = CallLogDbHelper.getDb();
		mCursor = mDb.fetchAlllogs();
		// startManagingCursor(mCursor);
		//
		// mAdapter = new SimpleCursorAdapter(this,
		// // Use a template that displays a text view
		// android.R.layout.simple_list_item_2,
		// // Give the cursor to the list adatper
		// mCursor,
		// // Map the NAME column in the people database to...
		// new String[] {CallLogDbAdapter.KEY_NAME, CallLogDbAdapter.KEY_TIME},
		// // The "text1" view defined in the XML template
		// new int[] {android.R.id.text1, android.R.id.text2});
		mAdapter = new MyAdapter(getData());
		setListAdapter(mAdapter);
	}

	private void registerClickHandlerForListview() {
		getListView().setOnItemClickListener(
				new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long i) {
						popupOptionList(position);
					}
				});
	}

	private void popupOptionList(final int position) {
		// validate position.
		if (positionIsInValid(position)) {
			return;
		}

		// get name and number
		Tuple<String> contact = getContactFromList(position);
		if (contact == null) {
			return;
		}
		final String name = contact.get(0);
		final String number = contact.get(1);
		final String title = (name != null && name.length() > 0) ? name
				: number;

		// init option items
		String callStr = getString(R.string.call);
		final String[] options = new String[] { callStr };

		// show popup list
		new AlertDialog.Builder(this).setTitle(title)
				.setIcon(android.R.drawable.sym_call_outgoing)
				.setItems(options, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						handlePopupListItemClicked(which, position);
					}
				}).show();
	}

	private void handlePopupListItemClicked(int which, int position) {
		switch (which) {
		case 0:
			call(position);
			break;
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.call_log_context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.call_log_context_del: {
			handleDelContextItemClick(item);
			return true;
		}

		case R.id.call_log_context_clear: {
			handleClearContextItemClick();
			return true;
		}
		}

		return super.onContextItemSelected(item);
	}

	private int getSelectedItemPositonFromContextMenu(MenuItem item) {
		AdapterView.AdapterContextMenuInfo menuInfo;
		menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		return menuInfo.position;
	}

	private void handleDelContextItemClick(MenuItem item) {
		delete(getSelectedItemPositonFromContextMenu(item));
	}

	private void handleClearContextItemClick() {
		clear();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.getMenuInflater().inflate(R.menu.call_log_option, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.call_log_option_call:
			handleCallOptionItemClick();
			return true;

		case R.id.call_log_option_del:
			handleDelOptionItemClick();
			return true;

		case R.id.call_log_option_clear:
			handleClearOptionItemClick();
			return true;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	private int getSelectedItemPositonFromOptionMenu() {
		return getListView().getSelectedItemPosition();
	}

	private void handleCallOptionItemClick() {
		call(getSelectedItemPositonFromOptionMenu());
	}

	private void handleAddQuickOptionItemClick() {
		addQuick(getSelectedItemPositonFromOptionMenu());
	}

	private void handleDelOptionItemClick() {
		delete(getSelectedItemPositonFromOptionMenu());
	}

	private void handleClearOptionItemClick() {
		clear();
	}

	private void call(int position) {
		if (Account.activeAccountIfNeed(this)) {
			return;
		}

		// validate position.
		if (positionIsInValid(position)) {
			return;
		}

		// get name and number
		Tuple<String> contact = getContactFromList(position);
		if (contact == null) {
			return;
		}
		String name = contact.get(0);
		String number = contact.get(1);

		// start calling activity
		Intent callIntent = new Intent(this, CallScreenActivity.class);
		if (name != null) {
			callIntent.putExtra(Constants.Call.CALLED_NAME, name);
		}
		callIntent.putExtra(Constants.Call.CALLED_NUM, number);
		startActivity(callIntent);
	}

	/**
	 * get contact from list in pattern (name, number)
	 * 
	 * @return null if length of number less than 3.
	 */
	Tuple<String> getContactFromList(int position) {
		// number
//		String number = mCursor.getString(mCursor
//				.getColumnIndexOrThrow(CallLogDbAdapter.KEY_NUMBER));
		HashMap	 map =  (HashMap) getListAdapter().getItem(position);
		String number = (String) map.get(CallLogDbAdapter.KEY_NUMBER);
		if (number == null || number.length() < 3) {
			return null;
		}

		// name
//		String name = mCursor.getString(mCursor
//				.getColumnIndexOrThrow(CallLogDbAdapter.KEY_NAME));
		String name = (String) map.get(CallLogDbAdapter.KEY_NAME);
		return new Tuple<String>(name, number);
	}

	private void delete(int position) {
		// validate position.
		if (positionIsInValid(position)) {
			return;
		}
		Map<String, Object> map = mAdapter.dataList.get(position);
//		mCursor.moveToPosition(position);
//		int id = mCursor.getInt(mCursor
//				.getColumnIndexOrThrow(CallLogDbAdapter.KEY_ROWID));
		Integer id = (Integer) map.get(CallLogDbAdapter.KEY_ROWID);
		if(id==null)
			return;
		if (mDb.deletelog(id)) {
			mCursor.requery();
			mAdapter.dataList.remove(position);
			mAdapter.notifyDataSetChanged();
		}
	}

	private boolean positionIsInValid(int position) {
		if (getListAdapter().getCount() <= 0) {
			return true;
		}

		if (position == AdapterView.INVALID_POSITION) {
			return true;
		}

		return false;
	}

	private void clear() {
		if (positionIsInValid(0)) {
			return;
		}

		if (mDb.deleteAllLogs()) {
			mCursor.requery();
			mAdapter.dataList.clear();
			mAdapter.notifyDataSetChanged();
		}
	}

	private void addQuick(int position) {
		// validate position.
		if (positionIsInValid(position)) {
			return;
		}

		// get name and number
		Tuple<String> contact = getContactFromList(position);
		if (contact == null) {
			return;
		}
		String name = contact.get(0);
		String number = contact.get(1);

		// add to db
		long rowid = QuickContactDbHelper.getDb().createlog(
				new QuickContact(name, number));
		int resId = rowid == -1 ? R.string.add_quick_fail
				: R.string.add_quick_suc;
		Toast.makeText(this, resId, Toast.LENGTH_LONG).show();
	}

	class MyAdapter extends BaseAdapter {
		List<Map<String, Object>> dataList;

		public MyAdapter(List<Map<String, Object>> dataList) {
			super();
			this.dataList = dataList;
		}

		@Override
		public int getCount() {
			return dataList.size();
		}

		@Override
		public Object getItem(int position) {
			return dataList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Map<String, Object> map = dataList.get(position);
			String name = "";
			String time = "";
			if (map != null) {
				if (map.get("flag") == null) {
					convertView = LayoutInflater.from(
							RecentCallsListActivity.this
									.getApplicationContext()).inflate(
											R.layout.call_textview, null);
					TextView tv1 = (TextView) convertView.findViewById(R.id.tv1);
					TextView tv2 = (TextView) convertView.findViewById(R.id.tv2);
					name = (String) map.get(CallLogDbAdapter.KEY_NAME);
//					number = (String) map.get(CallLogDbAdapter.KEY_NUMBER);
					time = (String) map.get(CallLogDbAdapter.KEY_TIME);
//					textView.setText(time + "  :" + name+":"+number);
					tv1.setText(name);
					tv2.setText(time);
				} else {
					convertView = LayoutInflater.from(
							RecentCallsListActivity.this
									.getApplicationContext()).inflate(
							R.layout.call_log_item1, null);
					TextView textView = (TextView) convertView
							.findViewById(R.id.textCallTitle);
					textView.setText((String) map.get("flag"));
				}
			}
			return convertView;
		}
	}

	public List<Map<String, Object>> getData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map;

		Cursor cursor = mCursor;
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间

		SimpleDateFormat dateSD = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat timeSD = new SimpleDateFormat("yyyy-MM-dd  hh:mm:ss");
		String curDateStr = dateSD.format(curDate);
		boolean isFirst = true;
		try {
			while (cursor.moveToNext()) {
				int cName = cursor.getColumnIndex(CallLogDbAdapter.KEY_NAME);
				int cTime = cursor.getColumnIndex(CallLogDbAdapter.KEY_TIME);
				int cNumber = cursor.getColumnIndex(CallLogDbAdapter.KEY_NUMBER);
				int cID = cursor.getColumnIndex(CallLogDbAdapter.KEY_ROWID);
				String name = cursor.getString(cName);// 获得名字
				String time = cursor.getString(cTime);
				String number = cursor.getString(cNumber);
				Integer id = cursor.getInt(cID);
				Date date = timeSD.parse(time);
				String dateStr = dateSD.format(date);
				if (isFirst || (!dateStr.equals(curDateStr))) {
				  map = new HashMap<String, Object>();
				  if (isFirst) {
            if (dateStr.equals(curDateStr)) {
              map.put("flag", getString(R.string.today));
            } else {
              map.put("flag", dateStr);
            }
          }else {
            map.put("flag", dateStr);
          }
					list.add(map);
					isFirst = false;
				}
				map = new HashMap<String, Object>();
				map.put(CallLogDbAdapter.KEY_NAME, name);
				map.put(CallLogDbAdapter.KEY_TIME, time);
				map.put(CallLogDbAdapter.KEY_NUMBER, number);
				map.put(CallLogDbAdapter.KEY_ROWID, id);
				list.add(map);
				curDateStr = dateStr;
			}
		} catch (ParseException e) {
		  Toast.makeText(this, "日期格式错误", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}finally{
			cursor.close();
		}
		return list;
	}
} // class CallLogActivity
