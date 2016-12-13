package com.quickqonnect;

import java.util.ArrayList;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

public class Performer {
	private static final String TAG = "Performer";

	//Receives message text and calls corresponding method
	public static void perform(String command, String request, String to, Boolean verified, ContentResolver cr) {
		Log.i(TAG, "command: " + command);
		if ( command.equalsIgnoreCase("contact") ) {
			if( request.substring(0, 4).equalsIgnoreCase("add ") ) {
				Log.i(TAG, "calling add contact");
				// add contact, removing "add "
				contactAdd( request.substring(4), to, verified, cr );
			} else {
				Log.i(TAG, "calling contactLookup of " + request);
				contactLookup(request, to, verified, cr);
			}
		} else if ( command.equalsIgnoreCase("wifi") ) {
			Log.i( TAG, "calling wifi toggle: " + request );
			toggleWifi(request, to, verified, cr);
		} else if ( command.equalsIgnoreCase("ringer") ) {
			Log.i( TAG, "calling ringer toggle: " + request );
			toggleRinger(request, to, verified, cr);
		} else
			SMSResponder.sendSMS(to, "Command '" + command + "' not recognized", cr);
	}

	private static void contactAdd(String name, String to, Boolean verified, ContentResolver cr) {
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		ops.add(ContentProviderOperation.newInsert(
				ContactsContract.RawContacts.CONTENT_URI)
				.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
				.withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
				.build());

		ops.add(ContentProviderOperation.newInsert(
				ContactsContract.Data.CONTENT_URI)
				.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
				.withValue(ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
						.withValue(
								ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
								name).build());

		ops.add(ContentProviderOperation.
				newInsert(ContactsContract.Data.CONTENT_URI)
				.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
				.withValue(ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
						.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, to)
						.withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
								ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
								.build());


		// Ask the Contact provider to create a new contact
		Log.i(TAG,"Creating contact: " + name + " - " + to);
		try {
			cr.applyBatch(ContactsContract.AUTHORITY, ops);
			SMSResponder.sendSMS(to, "Contact added successfully" , cr);
		} catch (Exception e) {
			// Display warning
			Context ctx = MainActivity.getAppContext();
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(ctx, "Contact creation failed", duration);
			toast.show();
			SMSResponder.sendSMS(to, "Contact creation failed", cr);
			// Log exception
			Log.e(TAG, "Exception encoutered while inserting contact: " + e);
		}
	}

	private static void contactLookup( String name, String to, Boolean verified, ContentResolver cr ) {
		if(verified){
			Log.i( TAG, "Looking up contact - " + name );
			String number = null;
			Cursor phones = cr.query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null,
					null, null);
			while (phones.moveToNext()) {
				if( phones
						.getString( phones
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME) )
								.equalsIgnoreCase(name) ) {
					name = phones
							.getString( phones
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME) );
					number = phones
							.getString(phones
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					Log.i( TAG, "contact found - " + name + ":" + number);
				}

			}
			phones.close();
			if( number == null ){
				number = "not found";
			}
			SMSResponder.sendSMS( to, name + ": " + number, cr );
		}
		else
			SMSResponder.sendSMS(to, "Password required", cr);
		
	}

	private static void toggleWifi( String toggle, String to, Boolean verified, ContentResolver cr) {
		WifiManager wifiManager = (WifiManager)MainActivity.getAppContext().getSystemService(Context.WIFI_SERVICE);
		if( toggle.equalsIgnoreCase("on") ) {
			Log.i( TAG, "they want wifi on");
			if( wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED ) {
				Log.i( TAG, "Wifi is already on");
				SMSResponder.sendSMS(to, "WiFi is already on", cr);
			} else {
				Log.i( TAG, "Turning wifi on");
				wifiManager.setWifiEnabled(true);
				SMSResponder.sendSMS(to, "WiFi turned on", cr);
			}
		} else if ( toggle.equalsIgnoreCase("off") ) {
			Log.i( TAG, "they want wifi off");
			if( wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED ) {
				Log.i( TAG, "wifi is already off");
				SMSResponder.sendSMS(to, "WiFi is already off", cr);
			} else {
				Log.i( TAG, "Let's turn wifi off");
				wifiManager.setWifiEnabled(true);
				SMSResponder.sendSMS(to, "WiFi turned off", cr);
			}
		} else {
			Log.i(TAG, "Toggle not recognized '" + toggle + "'");
			SMSResponder.sendSMS(to, "Toggle '" + toggle + "' not recognized", cr);
		}
	}

	private static void toggleRinger( String toggle, String to, Boolean verified, ContentResolver cr ) {
		AudioManager audioManager = (AudioManager) MainActivity.getAppContext().getSystemService(Context.AUDIO_SERVICE);
		if( toggle.equalsIgnoreCase("silent") ) {
			Log.i(TAG, "They want the ringer silent");
			if(audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
				Log.i(TAG, "Ringer already silent");
				SMSResponder.sendSMS(to, "Ringer already silent", cr);
			} else {
				Log.i(TAG, "Making ringer silent");
				audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
				SMSResponder.sendSMS(to, "Ringer silenced", cr);
			}
		} else if ( toggle.equalsIgnoreCase("normal") ) {
			Log.i(TAG, "They want the ringer normal");
			if(audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
				Log.i(TAG, "Ringer already normal");
				SMSResponder.sendSMS(to, "Ringer already normal", cr);
			} else {
				Log.i(TAG, "Making ringer normal");
				audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				SMSResponder.sendSMS(to, "Ringer now normal", cr);
			}
		} else if ( toggle.equalsIgnoreCase("vibrate") ) {
			Log.i(TAG, "They want vibration");
			if(audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
				Log.i(TAG, "Ringer already on vibrate");
				SMSResponder.sendSMS(to, "Ringer already on vibrate", cr);
			} else {
				Log.i(TAG, "Making ringer vibrate");
				audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
				SMSResponder.sendSMS(to, "Ringer is now on vibrate", cr);
			}
		}
	}

}
