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
		if (!lpparam.packageName.equals("com.android.apps.tag"))
			return;
		
		Class<?> TagViewer = XposedHelpers.findClass("com.android.apps.tag.TagViewer", lpparam.classLoader);
		final Class<?> NdefMessageParser =
				XposedHelpers.findClass("com.android.apps.tag.message.NdefMessageParser", lpparam.classLoader);
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
			}
		});
	}
}
