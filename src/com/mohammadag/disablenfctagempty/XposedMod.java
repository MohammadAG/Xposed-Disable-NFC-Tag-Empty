package com.mohammadag.disablenfctagempty;

import java.util.List;

import android.app.Activity;
import android.nfc.NdefMessage;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XposedMod implements IXposedHookLoadPackage {
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (lpparam.packageName.equals("com.android.apps.tag")
				|| lpparam.packageName.equals("com.google.android.tag")) {
			doHooks(lpparam);
		}
	}

	private void doHooks(LoadPackageParam lpparam) {
		Class<?> TagViewer = XposedHelpers.findClass("com.android.apps.tag.TagViewer", lpparam.classLoader);
		final Class<?> NdefMessageParser =
				XposedHelpers.findClass("com.android.apps.tag.message.NdefMessageParser", lpparam.classLoader);
		// Get UnknownRecord class
		final Class<?> UnknownRecord = XposedHelpers.findClass("com.android.apps.tag.record.UnknownRecord", lpparam.classLoader);
		XposedHelpers.findAndHookMethod(TagViewer, "buildTagViews", NdefMessage.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Activity activity = (Activity) param.thisObject;
				Object parameter = param.args[0];
				if (parameter == null) {
					activity.finish();
					return;
				}

				NdefMessage message = (NdefMessage) parameter;		
				Object parsedMsg = XposedHelpers.callStaticMethod(NdefMessageParser, "parse", message);
				List<?> records = (List<?>) XposedHelpers.callMethod(parsedMsg, "getRecords");
				final int size = records.size();
				if (size == 0) {
					activity.finish();
					return;
				}
				
				/*
				 * Check if there is at least a known and not text-empty tag.
				 */
				// Declare valid tag status
				boolean validTag = false;
				// Check each tag record
				for (Object record : records) {
					// Get record class
					Class<?> recordClass = record.getClass();
					if (recordClass.isAssignableFrom(UnknownRecord)) {
						// Skip UnknownRecord
						continue;
					}
					// Mark tag as valid
					validTag = true;
					// Stop checking records
					break;
				}
				// Check valid tag status
				if (!validTag) {
					activity.finish();
					return;
				}
			}
		});
	}
}
