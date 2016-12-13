package com.quickqonnect;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import com.quickqonnect.R;

public class MainActivity extends Activity {
	
    private static Context context;
    private static String password = "1337";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		MainActivity.context = getApplicationContext();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	public static Context getAppContext() {
        return MainActivity.context;
    }

	public static Boolean verifyPassword(String word) {
		return word.equals(password);
	}

}
