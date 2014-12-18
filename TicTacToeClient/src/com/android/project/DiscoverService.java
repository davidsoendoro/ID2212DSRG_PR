package com.android.project;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

public class DiscoverService {
    private String SERVICE_NAME = "Client Device";
    private String SERVICE_TYPE = "_ttt._tcp.";
    
    private NsdManager mNsdManager;
    private Runnable callback;
    private TicTacToeGenericActivity activity;
    private boolean isCalling = false;
    
    private List<NsdServiceInfo> services;
    private String result;
    
    private boolean isDiscovering;
    
    /**
     * Constructor to initiate DiscoverService
     * @param context
     */
    public DiscoverService(Context context) {
        // NSD Stuff
    	mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    /**
     * To start discovery of network service specified by SERVICE_TYPE
     */
    public void discover(){
    	while(isCalling);
		isCalling = true;
		
//		getActivity().setDialog(ProgressDialog.show(getActivity(), 
//				"Search Game", "Now Searching..."));
    	mNsdManager.discoverServices(SERVICE_TYPE,
                NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    	
    }

    /**
     * To tear down the discovery
     */
    protected void onDestroy() {
        if (mNsdManager != null && isDiscovering) {
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
            isDiscovering = false;
        }
    }
    
	/**
	 * Set callback for the network function
	 * @param callback
	 */
	public void setCallback(Runnable callback) {
		this.callback = callback;
	}
	
	/**
	 * Get the result of the latest process
	 * @return result in JSON format in String
	 */
	public String getResult() {
		return result;
	}

	/**
	 * Set the result of the latest process
	 * @param result
	 */
	public void setResult(String result) {
		this.result = result;
	}

	/**
	 * Get all services discovered
	 * @return All discovered services
	 */
	public List<NsdServiceInfo> getServices() {
		return services;
	}

	/**
	 * Get activity of the network function caller - used for GUI purposes
	 * @return the activity caller
	 */
	public TicTacToeGenericActivity getActivity() {
		return activity;
	}

	/**
	 * Set activity of the network function caller - used for GUI purposes
	 * @param activity
	 */
	public void setActivity(TicTacToeGenericActivity activity) {
		this.activity = activity;
	}

	/**
	 * To stop service discovery of network
	 */
	public void stopDiscoveryService() {
        if (mNsdManager != null && isDiscovering) {
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
            isDiscovering = false;
        }
	}
	
	/**
	 * Get NSD manager
	 * @return NSD manager associated to this object
	 */
    public NsdManager getmNsdManager() {
		return mNsdManager;
	}

    /**
     * Discovery listener for events after discovery is started
     */
	NsdManager.DiscoveryListener mDiscoveryListener = new NsdManager.DiscoveryListener() {
    
        // Called as soon as service discovery begins.
        @Override
        public void onDiscoveryStarted(String regType) {
            Log.d("NSD", "Service discovery started");
            isDiscovering = true;
            services = new ArrayList<NsdServiceInfo>();
        }
        
        @Override
        public void onServiceFound(NsdServiceInfo service) {
            // A service was found! Do something with it.
            Log.d("NSD", "Service discovery success : " + service);
            Log.d("NSD", "Host = "+ service.getServiceName());
            Log.d("NSD", "port = " + String.valueOf(service.getPort()));
            isCalling = false;
                
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
//                mNsdManager.resolveService(service, new NsdManager.ResolveListener() {
//                    
//                    @Override
//                    public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
//                        // Called when the resolve fails. Use the error code to debug.
//                        Log.e("NSD", "Resolve failed " + errorCode);
//                        Log.e("NSD", "serivce = " + serviceInfo);
//                        isCalling=false;
//                    }
//                
//                    @Override
//                    public void onServiceResolved(NsdServiceInfo serviceInfo) {
//                        Log.d("NSD", "Resolve Succeeded. " + serviceInfo);
//
//                		WifiManager wm = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
//                		String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
//            			
//                        if (serviceInfo.getServiceName().equals(SERVICE_NAME) || 
//                        		serviceInfo.getHost().getHostAddress().equals("10.0.2.15") ||
//                        		serviceInfo.getHost().getHostAddress().equals(ip)) {
//                            Log.d("NSD", "Same IP.");
//                            return;
//                        }
//                    
//                        // Obtain port and IP
//                        hostPort = serviceInfo.getPort();
//                        hostAddress = serviceInfo.getHost().getHostAddress();
//                        isCalling = false;
//                        getActivity().runOnUiThread(callback);
//                        
//                    }
//                });
            }

            isDiscovering = false;
            mNsdManager.stopServiceDiscovery(this);
            result = "DiscoveryService";
            
            services.add(service);
            getActivity().runOnUiThread(callback);
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
}
