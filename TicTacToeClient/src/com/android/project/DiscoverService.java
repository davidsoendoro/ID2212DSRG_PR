package com.android.project;

import java.net.InetAddress;

import android.app.ProgressDialog;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

public class DiscoverService {
    private String SERVICE_NAME = "Client Device";
    private String SERVICE_TYPE = "_ttt._tcp.";
    
    public String hostAddress;
    public int hostPort;
    private NsdManager mNsdManager;
    private Runnable callback;
    private TicTacToeGenericActivity activity;
    private boolean isCalling = false;
    
    public DiscoverService(NsdManager manager) {
        
        
        // NSD Stuff
       mNsdManager=manager;
    	// mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        
    }
    
    public void discover(){
    	while(isCalling);
		isCalling = true;
		
		getActivity().setDialog(ProgressDialog.show(getActivity(), 
				"Search Game", "Now Searching..."));
    	mNsdManager.discoverServices(SERVICE_TYPE,
                NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    	
    }
    
    protected void onDestroy() {
        if (mNsdManager != null) {
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        }
    }
    
    public Runnable getCallback() {
		return callback;
	}

	public void setCallback(Runnable callback) {
		this.callback = callback;
	}
	
	public TicTacToeGenericActivity getActivity() {
		return activity;
	}

	public void setActivity(TicTacToeGenericActivity activity) {
		this.activity = activity;
	}
	
    NsdManager.DiscoveryListener mDiscoveryListener = new NsdManager.DiscoveryListener() {
    
        // Called as soon as service discovery begins.
        @Override
        public void onDiscoveryStarted(String regType) {
            Log.d("NSD", "Service discovery started");
        }
        
        @Override
        public void onServiceFound(NsdServiceInfo service) {
                // A service was found! Do something with it.
                Log.d("NSD", "Service discovery success : " + service);
                Log.d("NSD", "Host = "+ service.getServiceName());
                Log.d("NSD", "port = " + String.valueOf(service.getPort()));
                isCalling=false;
                
            if (!service.getServiceType().equals(SERVICE_TYPE)) {
                // Service type is the string containing the protocol and
                // transport layer for this service.
                Log.d("NSD", "Unknown Service Type: " + service.getServiceType());
            } else if (service.getServiceName().equals(SERVICE_NAME)) {
                // The name of the service tells the user what they'd be
                // connecting to. It could be "Bob's Chat App".
                Log.d("NSD", "Same machine: " + SERVICE_NAME);
            } else {
                Log.d("NSD", "Diff Machine : " + service.getServiceName());
                // connect to the service and obtain serviceInfo
                mNsdManager.resolveService(service, mResolveListener);
            }
        }
        
        @Override
        public void onServiceLost(NsdServiceInfo service) {
            // When the network service is no longer available.
            // Internal bookkeeping code goes here.
            Log.e("NSD", "service lost" + service);
            isCalling=false;
        }
        
        @Override
        public void onDiscoveryStopped(String serviceType) {
            Log.i("NSD", "Discovery stopped: " + serviceType);
            isCalling=false;
        }
        
        @Override
        public void onStartDiscoveryFailed(String serviceType, int errorCode) {
            Log.e("NSD", "Discovery failed: Error code:" + errorCode);
            mNsdManager.stopServiceDiscovery(this);
            isCalling=false;
        }
        
        @Override
        public void onStopDiscoveryFailed(String serviceType, int errorCode) {
            Log.e("NSD", "Discovery failed: Error code:" + errorCode);
            mNsdManager.stopServiceDiscovery(this);
            isCalling=false;
        }
    };
    
    NsdManager.ResolveListener mResolveListener = new NsdManager.ResolveListener() {
    
        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
            // Called when the resolve fails. Use the error code to debug.
            Log.e("NSD", "Resolve failed " + errorCode);
            Log.e("NSD", "serivce = " + serviceInfo);
            isCalling=false;
        }
    
        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            Log.d("NSD", "Resolve Succeeded. " + serviceInfo);
            
            if (serviceInfo.getServiceName().equals(SERVICE_NAME)) {
                Log.d("NSD", "Same IP.");
                return;
            }
        
            // Obtain port and IP
            hostPort = serviceInfo.getPort();
            hostAddress = serviceInfo.getHost().getHostAddress();
            isCalling=false;
            getActivity().runOnUiThread(callback);
            
        }
    };
    
}
