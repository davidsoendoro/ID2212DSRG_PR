package com.android.project;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.nsd.NsdManager.RegistrationListener;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;

public class RegisterService {

    private String SERVICE_NAME = "TicTacToe Server";
    private String SERVICE_TYPE = "_ttt._tcp.";
    private NsdManager mNsdManager;
    private boolean isRegistered;
    
    private Context mContext;
	  

    public RegisterService(Context context){
    	mContext = context; 
        mNsdManager = (NsdManager) mContext.getSystemService(Context.NSD_SERVICE);
    }

    protected void unregisterService() {
        if (mNsdManager != null && isRegistered) {
            mNsdManager.unregisterService(mRegistrationListener);
            isRegistered = false;
        }
    }
	
	public void registerService(int port) {			
		if(!isRegistered) {
	        NsdServiceInfo serviceInfo = new NsdServiceInfo();
	
//	        try {
//	    		WifiManager wm = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
//	    		String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
//				serviceInfo.setHost(InetAddress.getByName(ip));
//			} catch (UnknownHostException e) {
//				e.printStackTrace();
//			}
	
	        serviceInfo.setServiceName(SERVICE_NAME);
	        serviceInfo.setServiceType(SERVICE_TYPE);
	        serviceInfo.setPort(port);
	
	        String mServiceName = serviceInfo.getServiceName();
//	        String mHost = serviceInfo.getHost().getHostAddress();
//	        String mPort = serviceInfo.getPort() + "";
	        SERVICE_NAME = mServiceName;
	        Log.d("NSD", "Registered name : " + mServiceName 
//	        		+ " Host: " + mHost 
//	        		+ " Port: " + mPort
	        		);
	        
	        mNsdManager.registerService(serviceInfo,
	            NsdManager.PROTOCOL_DNS_SD,
	            mRegistrationListener);
			isRegistered = true;
		}
    }
	  
    RegistrationListener mRegistrationListener = new NsdManager.RegistrationListener() {
 
        @Override
        public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
            String mServiceName = NsdServiceInfo.getServiceName();
            SERVICE_NAME = mServiceName;
            Log.d("NSD", "Registered name : " + mServiceName);
        }
 
        @Override
        public void onRegistrationFailed(NsdServiceInfo serviceInfo,
	                              int errorCode) {
            // Registration failed! Put debugging code here to determine
            // why.
        }
 
        @Override
        public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
            // Service has been unregistered. This only happens when you
            // call
            // NsdManager.unregisterService() and pass in this listener.
            Log.d("NSD",
        		  "Service Unregistered : " + serviceInfo.getServiceName());
        }
 
        @Override
        public void onUnregistrationFailed(NsdServiceInfo serviceInfo,
	                                int errorCode) {
            // Unregistration failed. Put debugging code here to determine
            // why.
        }
    };
}