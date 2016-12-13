package com.quickqonnect;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;


public class SMSReceiver extends BroadcastReceiver {
	private final String TAG = "SMSReceiver";
	private static ContentResolver cr;

	// Receives SMS, verifies, and sends message to Performer
	@Override
	public void onReceive( Context context, Intent intent ) {
		Log.i( TAG , "message received" );
		final Bundle bundle = intent.getExtras();
		SmsMessage[] msgs = null;
		String message = "";
		String sender = "";
		String command = "";
		String request = "";
		if ( bundle != null ) {
			cr = context.getContentResolver();
			final Object[] pdus = ( Object[] ) bundle.get( "pdus" );
			msgs = new SmsMessage[pdus.length];
			for ( int i = 0; i < msgs.length; i++ ) {
				msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
				message = msgs[i].getDisplayMessageBody();
				sender = msgs[i].getDisplayOriginatingAddress();
				//Check message matches format "!qq " then breaks into command/request
				if( message.substring(0, 4).equalsIgnoreCase("!qq ") ) {
					message = message.substring(4);
					if(MainActivity.verifyPassword(message.substring(0, message.indexOf(' ')))){
						message = message.substring(message.indexOf(' ') + 1);
						command = message.substring(0, message.indexOf(' '));
						request = message.substring(message.indexOf(' ')+1);
						Log.i( TAG, "calling perform: " + command + " : " + request + " pw=true");
						Performer.perform(command, request, sender, true, cr);
					} else {
						command = message.substring(0, message.indexOf(' '));
						request = message.substring(message.indexOf(' ')+1);
						Log.i( TAG, "calling perform: " + command + ":" + request);
						Performer.perform(command, request, sender, false, cr);
					}
					
				}
			}

			
		}
	}

}
