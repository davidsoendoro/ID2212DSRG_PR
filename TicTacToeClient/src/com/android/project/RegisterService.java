package com.android.project;

import com.android.project.helper.TicTacToeHelper;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.RegistrationListener;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

public class RegisterService {

    private String SERVICE_NAME = "TicTacToe Server";
    private String SERVICE_TYPE = "_ttt._tcp.";
    private NsdManager mNsdManager;
    private boolean isRegistered;
    
    private Context mContext;
	  

    public RegisterService(Context context){
    	SERVICE_NAME = TicTacToeHelper.getDeviceName() + "-" + TicTacToeHelper.IMEI;
    	mContext = context; 
        mNsdManager = (NsdManager) mContext.getSystemService(Context.NSD_SERVICE);
    }
/**
 * To unregister service 
 */
    protected void unregisterService() {
        if (mNsdManager != null && isRegistered) {
            mNsdManager.unregisterService(mRegistrationListener);
            isRegistered = false;
        }
    }
	/**
	 * To register the network service
	 * @param port on which service is to started
	 */
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
	  /**
	   * Listener for events generated after service registration is started
	   */
    RegistrationListener mRegistrationListener = new NsdManager.RegistrationListener() {
    	/**
    	 * Callback called when service gets registered
    	 * 
    	 */
        @Override
        public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
            String mServiceName = NsdServiceInfo.getServiceName();
            SERVICE_NAME = mServiceName;
            Log.d("NSD", "Registered name : " + mServiceName);
        }
        /**
         * Callback for registration failed
         */
        @Override
        public void onRegistrationFailed(NsdServiceInfo serviceInfo,
	                              int errorCode) {
            // Registration failed! Put debugging code here to determine
            // why.
        }
        /**
         * Callback for unregistering service
         */
        @Override
        public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
            // Service has been unregistered. This only happens when you
            // call
            // NsdManager.unregisterService() and pass in this listener.
            Log.d("NSD",
        		  "Service Unregistered : " + serviceInfo.getServiceName());
        }
        /**
         * callback when unregistration fails
         */
        @Override
        public void onUnregistrationFailed(NsdServiceInfo serviceInfo,
	                                int errorCode) {
            // Unregistration failed. Put debugging code here to determine
            // why.
        }
    };
}