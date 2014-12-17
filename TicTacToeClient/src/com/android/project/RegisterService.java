package com.android.project;

import android.app.Activity;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.nsd.NsdManager.RegistrationListener;
import android.os.Bundle;
import android.util.Log;

public class RegisterService {

    private String SERVICE_NAME = "TicTacToe Server";
    private String SERVICE_TYPE = "_ttt._tcp.";
    private NsdManager mNsdManager;
	  

    public RegisterService(NsdManager manager){
        
       
       mNsdManager=manager; 
        //mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
    }
    
  
    

    protected void unregisterService() {
        if (mNsdManager != null) {
            mNsdManager.unregisterService(mRegistrationListener);
        }
    }
	
	public void registerService(int port) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(SERVICE_NAME);
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setPort(port);
        
        mNsdManager.registerService(serviceInfo,
            NsdManager.PROTOCOL_DNS_SD,
            mRegistrationListener);
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