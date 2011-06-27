package com.sqt001.ipcall.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.sqt001.ipcall.R;
import com.sqt001.ipcall.contact.Account;
import com.sqt001.ipcall.contact.ContactsCursor;
import com.sqt001.ipcall.contact.NameColumn;
import com.sqt001.ipcall.contact.NameQueryer;
import com.sqt001.ipcall.contact.NumberQueryer;
import com.sqt001.ipcall.provider.Constants;
import com.sqt001.ipcall.provider.QuickContact;
import com.sqt001.ipcall.provider.QuickContactDbHelper;
import com.sqt001.ipcall.util.StrUitl;
import com.sqt001.ipcall.util.Tuple;

/**
 * Displays a list of contacts. Usually is embedded into the ContactsActivity.
 */
public class ContactsListActivity extends ListActivity {

	private final class RemoveWindow implements Runnable {
		public void run() {
			removeWindow();
		}
	}

	private RemoveWindow mRemoveWindow = new RemoveWindow();
	private boolean mShowing;
	private char mPrevLetter = Character.MIN_VALUE;
	private Handler mHandler = new Handler();
	private WindowManager mWindowManager;
	private TextView mDialogText;
	private boolean mReady;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.contacts_list);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.contacts_title);

		mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

		mDialogText = (TextView) View.inflate(this, R.layout.list_position,
				null);

		mHandler.post(new Runnable() {

			public void run() {
				mReady = true;
				WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
						LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT,
						WindowManager.LayoutParams.TYPE_APPLICATION,
						WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
								| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
						PixelFormat.TRANSLUCENT);
				mWindowManager.addView(mDialogText, lp);
			}
		});

		setupListView();
	}

	private void removeWindow() {
		if (mShowing) {
			mShowing = false;
			mDialogText.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 按下键盘上返回按钮
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(ContactsListActivity.this,
					DialtactsActivity.class);
			Bundle bundle = new Bundle();
			intent.putExtras(bundle);
			startActivity(intent);
			finish();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mReady = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		removeWindow();
		mReady = false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mWindowManager.removeView(mDialogText);
		mReady = false;
	}

	private void setupListView() {
		populateContactList();
		registerScrollHandlerForListview();
		registerClickHandlerForListview();
		registerContextMemuForListview();
	}

	private void registerScrollHandlerForListview() {
		getListView().setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (mReady) {
					String name = getContacts(firstVisibleItem);
					char ch = StrUitl.firstChar(name);
					if(ch ==' '){
						return;
					}
					String value = String.valueOf(ch);
					char firstLetter = value.charAt(0);
					if (!mShowing && firstLetter != mPrevLetter) {
						mShowing = true;
						mDialogText.setVisibility(View.VISIBLE);
					}
					mDialogText.setText(((Character) firstLetter).toString());
					mHandler.removeCallbacks(mRemoveWindow);
					mHandler.postDelayed(mRemoveWindow, 1000);
					mPrevLetter = firstLetter;
				}
			}
		});
	}

	private String getContacts(int position) {
		if (positionIsInValid(position)) {
			return " ";
		}

		String[] contact = getContactName(position);
		if (contact == null) {
			return " ";
		}
		return contact[0];
	}

	private void populateContactList() {
		Cursor c = ContactsCursor.create(this).getContacts();
		// startManagingCursor(c);
		// ListAdapter adapter = getContactAdapter(c);
		MyContactsAdapter adapter = new MyContactsAdapter(getData(), this);
		setListAdapter(adapter);
	}

	private SimpleCursorAdapter getContactAdapter(Cursor c) {
		String[] columns = NameColumn.create().getColumn();
		return new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_1, c, columns,
				new int[] { android.R.id.text1 });
	}

	private void registerClickHandlerForListview() {
		getListView().setOnItemClickListener(
				new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long i) {
						popupNumbersListForCall(position);
					}
				});
		getListView().setTextFilterEnabled(true);
	}

	private void popupNumbersListForCall(int position) {
		// validate position.
		if (positionIsInValid(position)) {
			return;
		}

		Tuple<String[]> contact = getContact(position);
		if (contact == null) {
			return;
		}
		final String[] names = contact.get(0);
		final String[] numbers = contact.get(1);
		if (names == null) {
			return;
		}
		Toast.makeText(ContactsListActivity.this, names[0], Toast.LENGTH_LONG)
				.show();
		if (numbers == null) {
			return;
		}

		new AlertDialog.Builder(this).setTitle(names[0])
				.setIcon(android.R.drawable.sym_call_outgoing)
				.setItems(numbers, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(ContactsListActivity.this,
								DialtactsActivity.class);
						Bundle bundle = new Bundle();
						bundle.putString("name", names[0]);
						bundle.putString("number", numbers[which]);
						bundle.putBoolean("success", true);
						intent.putExtras(bundle);
						startActivity(intent);
						finish();
						Toast.makeText(ContactsListActivity.this, names[0],
								Toast.LENGTH_LONG).show();
						// call(names[0], numbers[which]);
					}
				}).show();
	}

	private final Tuple<String[]> getContact(int position) {
		Map map = (Map) getListAdapter().getItem(position);
		String[] nameAry = (String[]) map.get("names");
		String[] numAry = (String[]) map.get("numbers");

		return new Tuple<String[]>(nameAry, numAry);
	}

	private final String[] getContactName(int position) {
		Map map = (Map) getListAdapter().getItem(position);
		String[] nameAry = (String[]) map.get("names");

		return nameAry;
	}

	private void call(String name, String number) {
		if (Account.activeAccountIfNeed(this)) {
			return;
		}

		// start calling activity
		Intent callIntent = new Intent(this, CallScreenActivity.class);
		if (name != null) {
			callIntent.putExtra(Constants.Call.CALLED_NAME, name);
		}
		callIntent.putExtra(Constants.Call.CALLED_NUM, number);
		startActivity(callIntent);
	}

	private void registerContextMemuForListview() {
		registerForContextMenu(getListView());
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.contact_context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.contact_context_call: {
			handleCallContextItemClick(item);
			return true;
		}

		case R.id.contact_context_add_quick: {
			handleAddQuickContextItemClick(item);
			return true;
		}
		}

		return super.onContextItemSelected(item);
	}

	private void handleCallContextItemClick(MenuItem item) {
		int position = getSelectedItemPositonFromContextMenu(item);
		popupNumbersListForCall(position);
	}

	private void handleAddQuickContextItemClick(MenuItem item) {
		int position = getSelectedItemPositonFromContextMenu(item);
		popupNumbersListForAddQuick(position);
	}

	private int getSelectedItemPositonFromContextMenu(MenuItem item) {
		AdapterView.AdapterContextMenuInfo menuInfo;
		menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		return menuInfo.position;
	}

	private void popupNumbersListForAddQuick(int position) {
		// validate position.
		if (positionIsInValid(position)) {
			return;
		}

		Tuple<String[]> contact = getContact(position);
		if (contact == null) {
			return;
		}
		final String[] names = contact.get(0);
		final String[] numbers = contact.get(1);
		if (numbers == null) {
			return;
		}

		new AlertDialog.Builder(this).setTitle(names[0])
				.setIcon(android.R.drawable.sym_call_outgoing)
				.setItems(numbers, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						addQuick(names[0], numbers[which]);
					}
				}).show();
	}

	private void addQuick(String name, String number) {
		long rowid = QuickContactDbHelper.getDb().createlog(
				new QuickContact(name, number));
		int resId = rowid == -1 ? R.string.add_quick_fail
				: R.string.add_quick_suc;
		Toast.makeText(this, resId, Toast.LENGTH_LONG).show();
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.getMenuInflater().inflate(R.menu.contacts_option, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.contacts_option_quick:
			handleQuickOptionItemClick();
			return true;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	private void handleQuickOptionItemClick() {
		Intent intent = new Intent(this, QuickCallsListActivity.class);
		startActivity(intent);
	}

	class MyContactsAdapter extends BaseAdapter {
		List<Map<String, Object>> dataList;
		Context ct;

		public MyContactsAdapter(List<Map<String, Object>> dataList, Context ct) {
			super();
			this.ct = ct;
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

		public View getView(int position, View convertView, ViewGroup parent) {
			Map<String, Object> map = dataList.get(position);
			String[] name;
			String photoID = "";
			if (map != null) {
				if (map.get("flag") == null) {
					convertView = LayoutInflater.from(
							ct.getApplicationContext()).inflate(
							R.layout.contact_item1, null);
					TextView textName = (TextView) convertView
							.findViewById(R.id.textContactName);
					ImageView imagePhoto = (ImageView) convertView
							.findViewById(R.id.imageContactPhoto);
					name = (String[]) map.get("names");
					photoID = (String) map.get("photo");
					if (photoID != null) {
						Bitmap bitmap = getPhoto(ct, photoID);
						BitmapDrawable bitmapDrawable = new BitmapDrawable(
								bitmap);
						if (bitmapDrawable != null)
							imagePhoto.setImageDrawable(bitmapDrawable);
					}
					textName.setText(name[0]);
				} else {
					convertView = LayoutInflater.from(
							ct.getApplicationContext()).inflate(
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
		Cursor cursor = ContactsCursor.create(this).getContacts();
		char curChar = 'a';
		boolean isFirst = true;
		while (cursor.moveToNext()) {
			int cPhoto = cursor.getColumnIndex(Phone.PHOTO_ID);
			String[] nameAry = NameQueryer.create(cursor).query();
			String[] numberAry = NumberQueryer.create(cursor, this).query();
			String photo = cursor.getString(cPhoto);
			String firstName = nameAry[0];
			char firstChar = StrUitl.firstChar(firstName);
			if (isFirst || curChar != firstChar) {
				map = new HashMap<String, Object>();
				map.put("flag", String.valueOf(firstChar));
				list.add(map);
				isFirst = false;
			}
			map = new HashMap<String, Object>();
			map.put("names", nameAry);
			map.put("numbers", numberAry);
			map.put("photo", photo);
			curChar = firstChar;
			list.add(map);
		}
		return list;
	}

	// 获取联系人图片
	public static Bitmap getPhoto(Context ct, Object photo_id) {
		Bitmap bmp = BitmapFactory.decodeResource(ct.getResources(),
				R.drawable.icon);
		if (photo_id != "" && photo_id != null) {
			String[] projection = new String[] { ContactsContract.Data.DATA15 };
			String selection = "ContactsContract.Data._ID = "
					+ photo_id.toString();
			Cursor cur = ct.getContentResolver().query(
					ContactsContract.Data.CONTENT_URI, projection, selection,
					null, null);
			if (cur != null) {
				cur.moveToFirst();
				byte[] contactIcon = null;
				contactIcon = cur.getBlob(cur
						.getColumnIndex(ContactsContract.Data.DATA15));
				if (contactIcon != null) {
					bmp = BitmapFactory.decodeByteArray(contactIcon, 0,
							contactIcon.length);
				}
			}
		}
		return bmp;
	}

}