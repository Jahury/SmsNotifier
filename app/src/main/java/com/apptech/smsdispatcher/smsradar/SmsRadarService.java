/*
 * Copyright (c) Tuenti Technologies S.L. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.apptech.smsdispatcher.smsradar;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.apptech.smsdispatcher.rest.SmsApi;
import com.apptech.smsdispatcher.ui.MainActivity;
import com.apptech.smsdispatcher.R;


public class SmsRadarService extends Service {

	private static final String CONTENT_SMS_URI = "content://sms";
	private static final int ONE_SECOND = 1000;
	private static final int NOTIF_ID = 1;
	private static final String NOTIF_CHANNEL_ID = "Channel_Id";

	private ContentResolver contentResolver;
	private SmsObserver smsObserver;
	private AlarmManager alarmManager;
	private TimeProvider timeProvider;
	private boolean initialized;


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		startForeground();
		if (!initialized) {
 		    initializeSmsDispatcher();
		}
		return START_STICKY;
	}

	private void initializeSmsDispatcher() {
		initialized = true;
			try {
				Thread background = new Thread() {
					public void run() {
						try {
							while (true) {
								new SmsApi().checkMessages(SmsRadar.smsListener);
							sleep(60 * 1000);
							}
						}
						catch (Exception e) {
						}
					}
				};
				background.start();
			} catch (Exception e) {
				e.printStackTrace();
			}

	}

	private void startForeground() {
		try {
			Intent notificationIntent = new Intent(this, MainActivity.class);

			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
					notificationIntent, 0);

			startForeground(NOTIF_ID, new NotificationCompat.Builder(this,
					NOTIF_CHANNEL_ID) // don't forget create a notification channel first
					.setOngoing(true)
					.setSmallIcon(R.mipmap.ic_launcher)
					.setContentTitle(getString(R.string.app_name))
					.setContentText("SMS Host is running")
					.setContentIntent(pendingIntent)
					.build());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		finishService();
	}

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		super.onTaskRemoved(rootIntent);
		restartService();
	}

	private void initializeService() {
		initialized = true;
		initializeDependencies();
		registerSmsContentObserver();
	}

	private void initializeDependencies() {
		if (!areDependenciesInitialized()) {
			initializeContentResolver();
			initializeSmsObserver();
		}
	}

	private boolean areDependenciesInitialized() {
		return contentResolver != null && smsObserver != null;
	}

	private void initializeSmsObserver() {
		Handler handler = new Handler();
		SmsCursorParser smsCursorParser = initializeSmsCursorParser();
		this.smsObserver = new SmsObserver(contentResolver, handler, smsCursorParser);
	}

	private SmsCursorParser initializeSmsCursorParser() {
		SharedPreferences preferences = getSharedPreferences("sms_preferences", MODE_PRIVATE);
		SmsStorage smsStorage = new SharedPreferencesSmsStorage(preferences);
		return new SmsCursorParser(smsStorage, getTimeProvider());
	}

	private void initializeContentResolver() {
		this.contentResolver = getContentResolver();
	}

	private void finishService() {
		initialized = false;
	//	unregisterSmsContentObserver();
	}


	private void registerSmsContentObserver() {
		Uri smsUri = Uri.parse(CONTENT_SMS_URI);
		boolean notifyForDescendents = true;
		contentResolver.registerContentObserver(smsUri, notifyForDescendents, smsObserver);
	}

	private void unregisterSmsContentObserver() {
		contentResolver.unregisterContentObserver(smsObserver);
	}

	private void restartService() {
		Intent intent = new Intent(this, SmsRadarService.class);
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
		long now = getTimeProvider().getDate().getTime();
		getAlarmManager().set(AlarmManager.RTC_WAKEUP, now + ONE_SECOND, pendingIntent);
	}

	private TimeProvider getTimeProvider() {
		return timeProvider != null ? timeProvider : new TimeProvider();
	}

	private AlarmManager getAlarmManager() {
		return alarmManager != null ? alarmManager : (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	}

	/*
	 * Test methods. This methods has been created to modify the service dependencies in test runtime because
	 * without dependency injection we can't provide this entities.
	 */

	void setSmsObserver(SmsObserver smsObserver) {
		this.smsObserver = smsObserver;
	}

	void setContentResolver(ContentResolver contentResolver) {
		this.contentResolver = contentResolver;
	}

	void setAlarmManager(AlarmManager alarmManager) {
		this.alarmManager = alarmManager;
	}

	void setTimeProvider(TimeProvider timeProvider) {
		this.timeProvider = timeProvider;
	}
}
