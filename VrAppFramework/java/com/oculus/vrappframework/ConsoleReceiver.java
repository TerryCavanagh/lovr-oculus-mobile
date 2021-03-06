/************************************************************************************

Filename    :   ConsoleReceiver.java
Content     :   A broadcast receiver that allows adb to send commands to an application.
Created     :   11/21/2014
Authors     :   Jonathan E. Wright

Copyright   :   Copyright (c) Facebook Technologies, LLC and its affiliates. All rights reserved.

*************************************************************************************/
package com.oculus.vrappframework;

import android.app.Activity;
import android.util.Log;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;

//==============================================================
// ConsoleReceiver
//
// To send a "console" command to the app:
// adb shell am broadcast -a oculus.console --es cmd <command text>
// where <command text> is replaced with a quoted string:
// adb shell am broadcast -a oculus.console --es cmd "print QA Event!"
//==============================================================
public class ConsoleReceiver extends BroadcastReceiver {
	public static final String TAG = "OvrConsole";

	public static String CONSOLE_INTENT = "oculus.console";
	public static String CONSOLE_STRING_EXTRA = "cmd";
	
	private static native void nativeConsoleCommand( long act, String command );

	static ConsoleReceiver receiver = new ConsoleReceiver();
	static boolean registeredReceiver = false;
	static Activity activity = null;

	public static void startReceiver( Activity act )
	{
		activity = act;
		if ( !registeredReceiver )
		{
			Log.d( TAG, "!!#######!! Registering console receiver" );

			IntentFilter filter = new IntentFilter();
			filter.addAction( CONSOLE_INTENT );
			act.registerReceiver( receiver, filter );
			registeredReceiver = true;
		}
		else
		{
			Log.d( TAG, "!!!!!!!!!!! Already registered console receiver!" );
		}
	}
	public static void stopReceiver( Activity act )
	{
		if ( registeredReceiver )
		{
			Log.d( TAG, "Unregistering console receiver" );
			act.unregisterReceiver( receiver );
			registeredReceiver = false;
		}
	}

 	@Override
	public void onReceive(Context context, final Intent intent) {
		Log.d( TAG, "!@#!@ConsoleReceiver action:" + intent );
		if ( intent.getAction().equals( CONSOLE_INTENT ) )
		{
			// Unity apps will not have a VrActivity, so they can only use console functions that are ok
			// with a NULL appPtr.
			String cmdValue = intent.getStringExtra( CONSOLE_STRING_EXTRA );
			if ( cmdValue != null && !cmdValue.equals( "" ) )
			{
				if ( activity instanceof VrActivity )
				{		
					nativeConsoleCommand( ((VrActivity)activity).getAppPtr(), cmdValue );
				}
				else
				{
					nativeConsoleCommand( ((long)0), cmdValue );
				}
			}
		}
	}

}
