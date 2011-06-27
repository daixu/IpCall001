package com.sqt001.ipcall.contact;

import android.net.Uri;
import android.provider.ContactsContract;

class ContactContentUriNew extends ContactContentUri {
  @Override
  protected Uri onGetUri() {
    return ContactsContract.Contacts.CONTENT_URI;
  }
}
