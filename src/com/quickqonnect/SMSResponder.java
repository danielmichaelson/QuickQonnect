package com.quickqonnect;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;

public class SMSResponder {
	private static final String TAG = "SMSResponder";
	public static final String SMS_URI = "content://sms";
	public static final String SMS_CONVERSATIONS_URI = "content://mms-sms/conversations/";
	public static final String ADDRESS = "address";
	public static final String PERSON = "person";
	public static final String DATE = "date";
	public static final String READ = "read";
	public static final String STATUS = "status";
	public static final String TYPE = "type";
	public static final String BODY = "body";
	public static final String SEEN = "seen";
	public static final int MESSAGE_TYPE_SENT = 2;
	public static final int MESSAGE_IS_READ = 1;
	public static final int MESSAGE_IS_SEEN = 1;
	
	public static void sendSMS(final String to, final String message, ContentResolver cr) {
		// Send that shit
		Log.i(TAG, "Responding to " + to + " with " + message);
		storeSentMessage(to, message, cr);
		final SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(to, null, message, null, null);

	}
	private static void storeSentMessage(String to, String message, ContentResolver cr) {
		ContentValues values = new ContentValues();
		values.put(ADDRESS, to);
		values.put(DATE, System.currentTimeMillis());
		values.put(READ, MESSAGE_IS_READ);
		values.put(TYPE, MESSAGE_TYPE_SENT);
		values.put(SEEN, MESSAGE_IS_SEEN);
		values.put(BODY, message);
		cr.insert(Uri.parse(SMS_URI), values);
	}
}
