package com.sqt001.ipcall.contact;

import android.net.Uri;
import android.provider.Contacts;
import android.provider.Contacts.People;

class ContactContentUriOld extends ContactContentUri {
  @Override
  protected Uri onGetUri() {
    return Contacts.People.CONTENT_URI;
  }
}
