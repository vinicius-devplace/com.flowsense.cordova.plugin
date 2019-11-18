package com.flowsense.cordova.plugin;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;


import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

import com.flowsense.flowsensesdk.StartFlowsenseService;
import com.flowsense.flowsensesdk.StartMonitoringLocation;
import com.flowsense.flowsensesdk.Network.UpdatePartnerUserId;
import com.flowsense.flowsensesdk.KeyValues.KeyValuesManager;
import com.flowsense.flowsensesdk.InAppEvent.InAppEvent;

import com.fasterxml.jackson.databind.ObjectMapper;

//TODO: DELETAR IMPORT
import com.flowsense.flowsensesdk.LocationService.GetInstantLocation;
import android.content.Intent;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.Manifest;
import android.content.pm.PackageManager;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import android.os.Bundle;

public class FlowsensePlugin extends CordovaPlugin{
  private static final String CHANGE_VIEW = "FSChangeViewFlowsense";
  private static final String VIEW_TO_CHANGE = "FSViewToChangeFlowsense";
  private static CallbackContext lastCallbackContext, pushContext, notificationContext;
  private static String partnerUserId;
  public static final String ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;

  private Context getApplicationContext() {
          return this.cordova.getActivity().getApplicationContext();
      }

  @Override
	public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException{
		Log.i("FlowsenseSDK", action);

    //Start Service
    if (action.equals("startSDK_fs")){
      setCallbackContext(callbackContext);
      partnerUserId = args.getString(0);
      if(cordova.hasPermission(ACCESS_FINE_LOCATION)) {
        Log.i("FlowsenseSDK", "Permission Granted");
      }
      else
      {
        getPermission(0);
      }
      startSDK(partnerUserId);
      Log.i("FlowsenseSDK", "startSDK_fs");
    }

    else if (action.equals("setSenderID")) {
      try{
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("FSPartnerGCMKey", args.getString(0));
        editor.commit();
      }
      catch(Exception e){
        Log.e("FlowsenseSDK", e.toString());
        Log.e("FlowsenseSDK", "Failed to save Sender ID, please check if it is correct");
      }
    }

    //Update Partner User ID
    else if (action.equals("updatePartnerUserId_fs")) {
        Log.i("FlowsenseSDK", "updatePartnerUserId_fs");
        setCallbackContext(callbackContext);
        updatePartnerUserId_fs(args.getString(0));
    }

    //Start Push lister for view changes
    else if (action.equals("listenPush")){
      setCallbackContextPush(callbackContext);
      openFromPush(cordova.getActivity());
    }

    //Update key-values
    else if (action.equals("updateKeyValues") || action.equals("updateKeyValueBoolean")) {
      Log.i("FlowsenseSDK", "updateKeyValues");
      setCallbackContext(callbackContext);
      updateKeyValues(args.getString(0), args.get(1));
    }

    //Update key-values-dates
    else if (action.equals("updateKeyValuesDate")) {
      Log.i("FlowsenseSDK", "updateKeyValues");
      setCallbackContext(callbackContext);
      updateKeyValuesDate(args.getString(0), args.getString(1));
    }

    //Commit key-values
    else if (action.equals("commitKeyValues")) {
      Log.i("FlowsenseSDK", "Commit Key Values called");
      setCallbackContext(callbackContext);
      commitChanges();
    }

    //GetInstantLocation
    else if (action.equals("getInstantLocation")) {
      Log.i("FlowsenseSDK", "GetInstantLocation called");
      setCallbackContext(callbackContext);
      getInstantLocation();
    }

    else if (action.equals("inAppEvent")) {
      setCallbackContext(callbackContext);
      try{
        String eventName = args.getString(0);
        HashMap<String,Object> map = new ObjectMapper().readValue(args.get(1).toString(),
                                                                      HashMap.class);
        inAppEvent(eventName, map);
      }
      catch(Exception e){
        Log.i("FlowsenseSDK", e.toString());
      }
    }

    else if (action.equals("registerPush")) {
      cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    notificationContext = callbackContext;
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String pushToken = sharedPref.getString("FSPartnerPushToken", null);
                    if (pushToken != null && !pushToken.equals("")){
                      sendPushToken(pushToken);
                    }
                  }
      });
    }

    else {
            Log.e("FlowsenseSDK", "Invalid action : " + action);
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
            return false;
        }

    return true;

  }

  private void setCallbackContext(CallbackContext callbackContext) {
		PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginResult);
        lastCallbackContext = callbackContext;
	}

  private void setCallbackContextPush(CallbackContext callbackContext) {
    PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginResult);
        pushContext = callbackContext;
  }

  private void eventCallbackSuccess(String event) {
		Log.w("FlowsenseSDK", event);
		if (lastCallbackContext != null) {
			PluginResult result = new PluginResult(PluginResult.Status.OK, getResultString(event));
	        result.setKeepCallback(true);
	        lastCallbackContext.sendPluginResult(result);
		}
	}

	private void eventCallbackError(String error) {
		Log.e("FlowsenseSDK", error);
		if (lastCallbackContext != null) {
			PluginResult result = new PluginResult(PluginResult.Status.ERROR, getResultString(error));
	    result.setKeepCallback(true);
	    lastCallbackContext.sendPluginResult(result);
		}
	}

  private JSONObject getResultString(String event) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("FlowsenseSDK", event);
        } catch (JSONException e) {
            Log.e("FlowsenseSDK", e.getMessage(), e);
        }
        return obj;
    }

  public static void sendEvent(JSONObject _json, Context context) {
      PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, _json);
      pluginResult.setKeepCallback(true);
      if (pushContext != null) {
        Log.i("FlowsenseSDK", "Chamei o plugin");

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(
                    context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(CHANGE_VIEW, false);
        editor.putString(VIEW_TO_CHANGE, "");
        editor.apply();

        pushContext.sendPluginResult(pluginResult);
      }
      //Log.e("FlowsenseSDK", "Recebi o Callback!");
  }

  public static void openFromPush(Context context) {
      SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(
                    context);
      String view = sharedPref.getString(VIEW_TO_CHANGE, "");
      if (sharedPref.getBoolean(CHANGE_VIEW, false)){
        try{
          JSONObject jsonObject = new JSONObject();
          jsonObject.put("action", view);
          PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, jsonObject);
          pluginResult.setKeepCallback(true);
          if (pushContext != null) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(CHANGE_VIEW, false);
            editor.putString(VIEW_TO_CHANGE, "");
            editor.apply();
              
            pushContext.sendPluginResult(pluginResult);
          }
        }
        catch (Exception e){
          Log.e("FlowsenseSDK", e.toString());
        }
      }
      //Log.e("FlowsenseSDK", "Recebi o Callback!");
  }

  private void startSDK(String PartnerToken) {
		new StartFlowsenseService(PartnerToken, cordova.getActivity());
    eventCallbackSuccess("Successfully called Start Flowsense Service");
	}

  private void updatePartnerUserId_fs(String PartnerUserId){
    UpdatePartnerUserId updatePartnerUserId = new UpdatePartnerUserId(cordova.getActivity(), PartnerUserId);
    updatePartnerUserId.execute();
    eventCallbackSuccess("Successfully called Update Partner User ID");
  }

  private void updateKeyValues(String key, Object value){
    KeyValuesManager manager = new KeyValuesManager(cordova.getActivity());
    try{
      if (value instanceof Integer)
        manager.setKeyValues(key, Double.valueOf((Integer) value));
      else if (value instanceof Double)
        manager.setKeyValues(key, (Double) value);
      else if (value instanceof Float)
        manager.setKeyValues(key, Double.valueOf((Float) value));
      else if (value instanceof String)
        manager.setKeyValues(key, (String) value);
      else if (value instanceof Boolean)
        manager.setKeyValues(key, (Boolean) value);
    }
    catch(Exception e){
      eventCallbackError(e.toString());
    }
  }

  private void updateKeyValuesDate(String key, String value){
    KeyValuesManager manager = new KeyValuesManager(cordova.getActivity());
    try{
      System.out.println("FlowsenseSDK " + new Date(Long.valueOf(value)));
      manager.setKeyValues(key, new Date(Long.valueOf(value)));
      eventCallbackSuccess("Successfully called Update Key Values");
    }
    catch(Exception e){
      eventCallbackError(e.toString());
    }
  }

  private void commitChanges(){
    try{
      KeyValuesManager manager = new KeyValuesManager(cordova.getActivity());
      manager.commitChanges();
      eventCallbackSuccess("Committing Key Values");
    }
    catch(Exception e){
      eventCallbackError("Error committing Key Values");
    }
  }

  private void getInstantLocation() {
    Intent getIntentInstanceLocation = new Intent(cordova.getActivity(), GetInstantLocation.class);
    cordova.getActivity().startService(getIntentInstanceLocation);
  }

  private void inAppEvent(String eventName, Map<String, Object> map) {
    try{
      new InAppEvent().SaveAndSendEvent(cordova.getActivity(), eventName, map);
      eventCallbackSuccess("InAppEvent called");
    }
    catch(Exception e){
      eventCallbackError("Error running InAppEvent: " + e.toString());
    }
  }

  protected void getPermission(int requestCode){
    cordova.requestPermission(this, requestCode, ACCESS_FINE_LOCATION);
  }

  public void onRequestPermissionResult(int requestCode, String[] permissions,
                                           int[] grantResults) throws JSONException{
      for(int r:grantResults){
          if(r == PackageManager.PERMISSION_DENIED){
              this.lastCallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Permission not Granted"));
              return;
          }
          else {
            Log.i("FlowsenseSDK", "Permission Granted");
            new StartMonitoringLocation(cordova.getActivity());
          }
      }
  }

    public static void sendPushToken(String token) {
      Log.v("FlowsenseSDK", "called Send Push Token");
      Log.v("FlowsenseSDK", "Cordova Push Token: " + token);
      JSONObject json = new JSONObject();
      if (notificationContext != null) {
          try{
            json.put("token", token);
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
            pluginResult.setKeepCallback(true);
            notificationContext.sendPluginResult(pluginResult);
            
          }
          catch(Exception e){
              notificationContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, e.toString()));
              Log.v("FlowsenseSDK", e.toString());
          }  
        }
        else {
          Log.v("FlowsenseSDK", "Context is Null");
        }
    }

    public static void sendExtras(Bundle extras) {
      Log.v("FlowsenseSDK", "called Send Extras");
        if (extras != null) {
            sendPushEvent(convertBundleToJson(extras));
        }
    }

    public static void sendPushEvent(JSONObject _json) {
      Log.v("FlowsenseSDK", "called Send Push Event");
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, _json);
        pluginResult.setKeepCallback(true);
        if (notificationContext != null) {
            notificationContext.sendPluginResult(pluginResult);
        }
    }

    private static JSONObject convertBundleToJson(Bundle extras) {
        Log.d("FlowsenseSDK", "convert extras to json");
        try {
            JSONObject json = new JSONObject();
            JSONObject additionalData = new JSONObject();

            Iterator<String> it = extras.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                Object value = extras.get(key);

                Log.d("FlowsenseSDK", "key = " + key);

                if ( value instanceof String ) {
                    String strValue = (String)value;
                    try {
                        // Try to figure out if the value is another JSON object
                        if (strValue.startsWith("{")) {
                            additionalData.put(key, new JSONObject(strValue));
                        }
                        // Try to figure out if the value is another JSON array
                        else if (strValue.startsWith("[")) {
                            additionalData.put(key, new JSONArray(strValue));
                        }
                        else {
                            additionalData.put(key, value);
                        }
                    } catch (Exception e) {
                        additionalData.put(key, value);
                    }
                }
            } // while

            json.put("additionalData", additionalData);
            Log.v("FlowsenseSDK", "extrasToJSON: " + json.toString());

            return json;
        }
        catch( JSONException e) {
            Log.e("FlowsenseSDK", "extrasToJSON: JSON exception");
        }
        return null;
    }

}
