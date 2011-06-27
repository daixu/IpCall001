package com.sqt001.ipcall.activity;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.text.method.DialerKeyListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sqt001.ipcall.R;
import com.sqt001.ipcall.application.AppPreference;
import com.sqt001.ipcall.application.BuildConfig;
import com.sqt001.ipcall.contact.Account;
import com.sqt001.ipcall.contact.NameLookup;
import com.sqt001.ipcall.provider.BalanceTask;
import com.sqt001.ipcall.provider.CallerNumberSetter;
import com.sqt001.ipcall.provider.Constants;
import com.sqt001.ipcall.provider.DownloadTask;
import com.sqt001.ipcall.provider.UpdateTask;
import com.sqt001.ipcall.util.AboutDlg;
import com.sqt001.ipcall.util.AppUtils;
import com.sqt001.ipcall.util.FileUtils;
import com.sqt001.ipcall.util.HttpManager;
import com.sqt001.ipcall.util.ResourceUtils;
import com.sqt001.ipcall.util.UserTask;

/**
 * 拨打电话界面
 */
public class TwelveKeyDialer extends Activity {
  private MiniNumberPadAdapter mNumberChooser = null;
  private EditText mDigits = null;
  private BalanceTask mBalanceTask = null;
  private Drawable mDigitsBackground;
  private Drawable mDigitsEmptyBackground;
  private View mDialButton;
  private View mDelete;
  private View mAddressList;
  private TextView mTextView;

  private boolean mDTMFToneEnabled;
  private ToneGenerator mToneGenerator;
  private Object mToneGeneratorLock = new Object();
  private static final int TONE_LENGTH_MS = 100;
  private static final int DIAL_TONE_STREAM_TYPE = AudioManager.STREAM_MUSIC;
  private static final int TONE_RELATIVE_VOLUME = 60;

  private UpdateTask mUpdateTask = null;
  private DownloadTask mDownloadTask = null;
  private final String TAG = "TwelveKeyDialer";

  @Override
  protected void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    setContentView(R.layout.twelve_key_dialer);
    initViewFields();
    importNumber();
    setupDigitEdit();
    setupMiniNumberPad();
    setupCallButton();
    setupClearButton();
    showNewVersionHint();
    checkNet();
    unInstallOldVersion();
    activeAccount();

    initNumber();
  }

  private void importNumber() {

  }

  public void showCallerLable() {
    String number = AppPreference.getMyNum();
    String num = AppPreference.getAccount();
    if ((number.length() > 0) && (!number.equals(""))) {
      mTextView.setText("主叫号码:" + number);
    } else {
      if ((num.length() > 0) && (!num.equals(""))) {
        mTextView.setText("主叫号码:" + num);
      }
    }
  }

  private void initNumber() {
    String number = this.getIntent().getExtras().getString("number");
    if (number != null)
      mDigits.setText(number);
  }

  @Override
  protected void onResume() {
    super.onResume();
    hideSoftKeyboardForNumberEdit();
    initTonePlayer();
    showCallerLable();
  }

  @Override
  protected void onPause() {
    super.onPause();
    releaseTonePlayer();
    showCallerLable();
  }

  private void initViewFields() {
    mDigits = (EditText) findViewById(R.id.number);// 拨号的输入框
    mDialButton = findViewById(R.id.keypad_call);// 拨打按钮
    mDelete = findViewById(R.id.keypad_del);// 删除按钮
    mAddressList = findViewById(R.id.address_list);
    mAddressList.setOnClickListener(new ItemClickListener());
    mTextView = (TextView) findViewById(R.id.dial_prompt);
    showCallerLable();
  }

  private class ItemClickListener implements View.OnClickListener {

    @Override
    public void onClick(View v) {
      Intent intent = new Intent(TwelveKeyDialer.this, ContactsListActivity.class);
      startActivity(intent);
      finish();
    }
  };

  private void hideSoftKeyboardForNumberEdit() {
    mDigits.setInputType(android.text.InputType.TYPE_NULL);

    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(mDigits.getWindowToken(), 0);
  }

  private void initTonePlayer() {
    mDTMFToneEnabled = Settings.System.getInt(getContentResolver(), Settings.System.DTMF_TONE_WHEN_DIALING, 1) == 1;

    // if the mToneGenerator creation fails, just continue without it. It is
    // a local audio signal, and is not as important as the dtmf tone itself.
    synchronized (mToneGeneratorLock) {
      if (mToneGenerator == null) {
        try {
          // we want the user to be able to control the volume of the dial tones
          // outside of a call, so we use the stream type that is also mapped to
          // the
          // volume control keys for this activity
          mToneGenerator = new ToneGenerator(DIAL_TONE_STREAM_TYPE, TONE_RELATIVE_VOLUME);
          setVolumeControlStream(DIAL_TONE_STREAM_TYPE);
        } catch (RuntimeException e) {
          mToneGenerator = null;
        }
      }
    }
  }

  private void releaseTonePlayer() {
    synchronized (mToneGeneratorLock) {
      if (mToneGenerator != null) {
        mToneGenerator.release();
        mToneGenerator = null;
      }
    }
  }

  private void setupDigitEdit() {
    Resources r = getResources();
    mDigitsBackground = r.getDrawable(R.drawable.btn_dial_textfield_active);
    mDigitsEmptyBackground = r.getDrawable(R.drawable.btn_dial_textfield);

    mDigits.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        call();
        return true;
      }
    });

    mDigits.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        call();
      }
    });

    mDigits.addTextChangedListener(new TextWatcher() {
      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        if (!isDigitsEmpty()) {
          mDigits.setBackgroundDrawable(mDigitsBackground);
          mDigits.setTextSize(33);
        } else {
          mDigits.setCursorVisible(false);
          mDigits.setBackgroundDrawable(mDigitsEmptyBackground);
          mDigits.setTextSize(14);
          mDigits.setHint(R.string.dial_prompt);
        }
        updateDialAndDeleteButtonEnabledState();
      }
    });

    mDigits.setKeyListener(DialerKeyListener.getInstance());
  }

  private boolean isDigitsEmpty() {
    return mDigits.length() == 0;
  }

  private void updateDialAndDeleteButtonEnabledState() {
    boolean enableOrNot = !numberIsUnValid(fetchCalledNumber());
    mDialButton.setEnabled(enableOrNot);
  }

  private void setupMiniNumberPad() {
    // Bundle bundle = this.getIntent().getExtras();
    // String number = bundle.getString("number");
    // Toast.makeText(TwelveKeyDialer.this, number, Toast.LENGTH_LONG).show();
    // mDigits.setText(number);
    mNumberChooser = new MiniNumberPadAdapter();
    for (int id : new int[] { R.id.n0, R.id.n1, R.id.n2, R.id.n3, R.id.n4, R.id.n5, R.id.n6, R.id.n7, R.id.n8, R.id.n9,
        R.id.pound, R.id.star, R.id.keypad_del }) {
      findViewById(id).setOnClickListener(mNumberChooser);
    }
  }

  private class MiniNumberPadAdapter implements View.OnClickListener {
    class KeySoundPair {
      public int code;
      public int sound;

      public KeySoundPair(int code, int sound) {
        this.code = code;
        this.sound = sound;
      }
    }

    Map<Integer, KeySoundPair> mKeyMapper = null;

    public MiniNumberPadAdapter() {
      initKeyMapper();
    }

    private void initKeyMapper() {
      mKeyMapper = new HashMap<Integer, KeySoundPair>();
      for (int[] idx : new int[][] { { R.id.n0, KeyEvent.KEYCODE_0, ToneGenerator.TONE_DTMF_0 },
          { R.id.n1, KeyEvent.KEYCODE_1, ToneGenerator.TONE_DTMF_1 },
          { R.id.n2, KeyEvent.KEYCODE_2, ToneGenerator.TONE_DTMF_2 },
          { R.id.n3, KeyEvent.KEYCODE_3, ToneGenerator.TONE_DTMF_3 },
          { R.id.n4, KeyEvent.KEYCODE_4, ToneGenerator.TONE_DTMF_4 },
          { R.id.n5, KeyEvent.KEYCODE_5, ToneGenerator.TONE_DTMF_5 },
          { R.id.n6, KeyEvent.KEYCODE_6, ToneGenerator.TONE_DTMF_6 },
          { R.id.n7, KeyEvent.KEYCODE_7, ToneGenerator.TONE_DTMF_7 },
          { R.id.n8, KeyEvent.KEYCODE_8, ToneGenerator.TONE_DTMF_8 },
          { R.id.n9, KeyEvent.KEYCODE_9, ToneGenerator.TONE_DTMF_9 },
          { R.id.pound, KeyEvent.KEYCODE_POUND, ToneGenerator.TONE_DTMF_P },
          { R.id.star, KeyEvent.KEYCODE_STAR, ToneGenerator.TONE_DTMF_S },
          { R.id.keypad_del, KeyEvent.KEYCODE_DEL, ToneGenerator.TONE_DTMF_D }, }) {
        mKeyMapper.put(idx[0], new KeySoundPair(idx[1], idx[2]));
      }
    }

    @Override
    public void onClick(View view) {
      KeySoundPair p = mKeyMapper.get(view.getId());
      if (p != null) {
        keyPressed(p.code);
        playTone(p.sound);
      }
    }

    private void keyPressed(int keyCode) {
      String numberBuffer = mDigits.getText().toString();
      KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
      System.out.println(numberBuffer);
      Editable etable = mDigits.getText();
      Selection.setSelection(etable, etable.length());
      mDigits.onKeyDown(keyCode, event);
    }

  } // class MinipadAdapter

  private static final int STOP_TONE = 1;
  private Handler mToneStopper = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
      case STOP_TONE:
        mToneGenerator.stopTone();
        break;
      }
    }
  };

  /**
   * Plays the specified tone for TONE_LENGTH_MS milliseconds.
   * 
   * The tone is played locally, using the audio stream for phone calls. Tones
   * are played only if the "Audible touch tones" user preference is checked,
   * and are NOT played if the device is in silent mode.
   * 
   * @param tone
   *          a tone code from {@link ToneGenerator}
   */
  void playTone(int tone) {
    // if local tone playback is disabled, just return.
    if (!mDTMFToneEnabled) {
      return;
    }

    // Also do nothing if the phone is in silent mode.
    // We need to re-check the ringer mode for *every* playTone()
    // call, rather than keeping a local flag that's updated in
    // onResume(), since it's possible to toggle silent mode without
    // leaving the current activity (via the ENDCALL-longpress menu.)
    AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    int ringerMode = audioManager.getRingerMode();
    if ((ringerMode == AudioManager.RINGER_MODE_SILENT) || (ringerMode == AudioManager.RINGER_MODE_VIBRATE)) {
      return;
    }

    synchronized (mToneGeneratorLock) {
      if (mToneGenerator == null) {
        return;
      }

      mToneStopper.removeMessages(STOP_TONE);

      mToneGenerator.startTone(tone);

      mToneStopper.sendEmptyMessageDelayed(STOP_TONE, TONE_LENGTH_MS);
    }
  }

  private void setupCallButton() {
    mDialButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        call();
      }
    });
  }

  void call() {
    if (Account.activeAccountIfNeed(this)) {
      return;
    }

    String called = fetchCalledNumber();
    if (numberIsUnValid(called)) {
      return;
    }
    // if (called == "1565" || called.equals("1565")) {
    // Intent intent = new Intent(this, LogActivity.class);
    // startActivity(intent);
    // return;
    // }
    startCallTask(called);
  }

  // 获得要呼叫的号码
  String fetchCalledNumber() {
    return mDigits.getText().toString();
  }

  boolean numberIsUnValid(String number) {// 判断拨打的号码
    if (number.length() < 3) { // can call 114
      return true;
    }
    return false;
  }

  void startCallTask(String number) {// 拨打电话
    Intent callIntent = new Intent(this, CallScreenActivity.class);
    callIntent.putExtra(Constants.Call.CALLED_NUM, number);
    String name = NameLookup.create().getName(this, number);
    if ("".equals(name) || name == null || name.length() == 0) {
      name = number;
    }
    callIntent.putExtra(Constants.Call.CALLED_NAME, name);
    startActivity(callIntent);
  }

  private void setupClearButton() {
    mDelete.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        if (mDigits.getText().length() > 0) {
          String number = mDigits.getText().toString();
          if (number != "") {
            number = number.substring(0, number.length() - 1);
            mDigits.setText(number);
          }
        }
      }
    });
    mDelete.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        clearNumbers();
        return true;
      }
    });
  }

  private void clearNumbers() {
    EditText edit = (EditText) findViewById(R.id.number);
    edit.setText("");
  }

  private void showNewVersionHint() {
    if (AppPreference.isForceUpdateMarked()) {
      String text = AppPreference.getNewVersionMsg();
      new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_menu_info_details).setTitle(R.string.update_versions)
          .setMessage(text).setPositiveButton(R.string.upgrade, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              AppPreference.markForceUpdate(false);
              String url = AppPreference.getNewVersionUrl();
              if ((url != null) && (url != null)) {
                download(url);
              }
            }
          }).create().show();
      return;
    }

    if (!AppPreference.checkNeedShowNewVersionHint()) {
      return;
    }
    // if (AppPreference.checkRemindVersionHint()) {
    // return;
    // }
    AppPreference.markNeedNotShowNewVersionHint();

    new AlertDialog.Builder(this).setTitle(this.getString(R.string.new_version_hint))
        .setMessage(AppPreference.getNewVersionMsg())
        .setPositiveButton(this.getString(R.string.ok), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            String url = AppPreference.getNewVersionUrl();
            download(url);
          }
        }).setNegativeButton(this.getString(R.string.cancel), null).show();
  }

  private void checkNet() {
    if (!HttpManager.checkNet(this)) {
      Toast.makeText(TwelveKeyDialer.this, getString(R.string.network_exception), Toast.LENGTH_LONG);
    }
  }

  private void unInstallOldVersion() {
    final String packageName = "com.newding.ipcall";
    if (!AppUtils.isAppExist(this, packageName)) {
      return;
    }

    final Context context = this;
    new AlertDialog.Builder(this).setMessage(getString(R.string.found_old_version))
        .setPositiveButton(this.getString(R.string.ok), new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            AppUtils.deletePackage(context, packageName);
          }
        }).show();
  }

  private void activeAccount() {
    Account.activeAccountIfNeed(this);
  }

  // for debug
  private static final int DEBUG_MENU_LOG_ITEM_ID = -1;

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main_entry_option, menu);
    super.onCreateOptionsMenu(menu);
    if (BuildConfig.isDebug()) {
      menu.add(0, DEBUG_MENU_LOG_ITEM_ID, Menu.NONE, "Log");
    }
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {

    case R.id.main_option_id: {
      handleIdOptionItemSelected();
      return true;
    }

    case R.id.main_option_balance:
      handleBalanceOptionItemClick();
      return true;

    case R.id.main_option_number:
      handleNumberOptionItemSelected();
      return true;

    case R.id.main_option_call_log:
      handleCallLogOptionItemSelected();
      return true;

    case R.id.main_option_help:
      handleHelpOptionItemSelected();
      return true;

    case R.id.main_option_about:
      handleAboutOptionItemSelected();
      return true;

    case R.id.main_option_partener:
      handlePartenerOptionItemSelected();
      return true;

    case R.id.main_option_call:
      handleCallOptionItemSelected();
      return true;

    case R.id.main_option_update: {
      handleUpdateOptionItemSelected();
      return true;
    }

      // for test
    case DEBUG_MENU_LOG_ITEM_ID: {
      Intent intent = new Intent(this, LogActivity.class);
      startActivity(intent);
      return true;
    }
    }
    return super.onOptionsItemSelected(item);
  }

  private void handleCallLogOptionItemSelected() {
    Intent intent = new Intent(TwelveKeyDialer.this, RecentCallsListActivity.class);
    startActivity(intent);
  }

  private void handleHelpOptionItemSelected() {
    String title = getString(R.string.about_help);
    String message = new ResourceUtils(this).getStringFromRawResource(R.raw.help_txt);
    new AboutDlg(this).show(title, message);
  }

  private void handleAboutOptionItemSelected() {
    String title = getString(R.string.about_about);
    String message = AppPreference.getAbout();
    if (message.length() < 2) {
      message = new ResourceUtils(this).getStringFromRawResource(R.raw.about_txt);
    }
    new AboutDlg(this).show(title, message);
  }

  private void handlePartenerOptionItemSelected() {
    String title = getString(R.string.partener);
    String message = new ResourceUtils(this).getStringFromRawResource(R.raw.partener_txt);
    new AboutDlg(this).show(title, message);
  }

  private void handleCallOptionItemSelected() {
    String num = AppPreference.getServiceCallNum();
    if (num != null && num.length() >= 8) {
      Intent phoneIntent = new Intent("android.intent.action.CALL", Uri.parse("tel:" + num));
      startActivity(phoneIntent);
    }
  }

  private void handleNumberOptionItemSelected() {
    if (!Account.isImsiActive()) {
      Account.queryReActiveAccount(this);
      return;
    }

    CallerNumberSetter setter = new CallerNumberSetter(this);
    setter.show(this, null);
  }

  private void handleBalanceOptionItemClick() {
    if (Account.activeAccountIfNeed(this)) {
      return;
    }
    balance();
  }

  private void handleIdOptionItemSelected() {
    new AlertDialog.Builder(this).setTitle(this.getString(R.string.your_id_is)).setMessage(AppPreference.getUserId())
        .setPositiveButton(this.getString(R.string.ok), null).show();
  }

  private void balance() {
    startBalanceTask();
  }

  private void startBalanceTask() {
    mBalanceTask = new BalanceTask(this).execute(new BalanceTask.BalanceTaskListener() {
      public void onCancelGetBalance() {
        if (mBalanceTask != null && mBalanceTask.getStatus() == UserTask.Status.RUNNING) {
          mBalanceTask.cancel(true);
          mBalanceTask = null;
        }
      }

      public void afterInputMyNum() {
        balance();
      }
    });
  }

  private void handleUpdateOptionItemSelected() {
    update();
  }

  private void update() {
    mUpdateTask = new UpdateTask(this).execute(new UpdateTask.UpdateTaskListener() {
      @Override
      public void onCancelGetUpdate() {
        if (mUpdateTask != null && mUpdateTask.getStatus() == UserTask.Status.RUNNING) {
          mUpdateTask.cancel(true);
          mUpdateTask = null;
        }
      }

      @Override
      public void onOkGetUpdate(String url) {
        download(url);
      }
    });
  }

  private void download(String url) {
    if (!FileUtils.isSDCardReady()) {
      new AlertDialog.Builder(this).setMessage(getString(R.string.sdcard_not_ready))
          .setPositiveButton(this.getString(R.string.ok), null).show();
      return;
    }

    // for test url:
    // url =
    // "http://220.112.40.202/down/Newding_Android_2.5.0_20100721_Newding.apk";

    mDownloadTask = new DownloadTask(this, url);
    mDownloadTask.execute();
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
      new AlertDialog.Builder(TwelveKeyDialer.this).setTitle(R.string.exit)
          .setIcon(android.R.drawable.ic_menu_info_details).setMessage(R.string.really_exit)
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
          }).create().show();
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }
}