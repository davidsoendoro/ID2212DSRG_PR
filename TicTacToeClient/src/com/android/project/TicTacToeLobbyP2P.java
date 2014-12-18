package com.android.project;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.android.network.TicTacToeGameAPIImpl;
import com.android.network.TicTacToeGameAPIP2PImpl;
import com.android.project.helper.TicTacToeHelper;

public class TicTacToeLobbyP2P extends TicTacToeGenericActivity implements OnClickListener, Runnable, OnCancelListener {

//	private Button buttonLobbyConnect;
//	private TextView textViewUserIp;
	private boolean isServer = false;
	private RegisterService registerService;
	private DiscoverService discoverService;
	
	private ListView list;
	private ArrayAdapter<String> adapter;
	private ArrayList<String> connectionStrings;
	
	private boolean isGamePlayed = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// GUI
		
		/*setContentView(R.layout.lobby_p2p);
		
		textViewUserIp = (TextView) findViewById(R.id.textView_lobby_ip);
		WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
		String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
		textViewUserIp.setText("Your IP: " + ip + " port: 8090");
		
		buttonLobbyConnect = (Button) findViewById(R.id.button_lobby_create);
		buttonLobbyConnect.setOnClickListener(TicTacToeLobbyP2P.this);
		Button buttonLobbyJoin = (Button) findViewById(R.id.button_lobby_join);
		buttonLobbyJoin.setOnClickListener(TicTacToeLobbyP2P.this);
//		checkBoxVsPlayer = (CheckBox) findViewById(R.id.checkBox_vsplayer);
 */
		setContentView(R.layout.lobby_p2p_nsd);
		Button buttonCreate = (Button) findViewById(R.id.button_lobby_create);
		buttonCreate.setOnClickListener(this);
		Button buttonJoin = (Button) findViewById(R.id.button_lobby_join);
		buttonJoin.setOnClickListener(this);
//		Button buttonUnregister = (Button) findViewById(R.id.button_lobby_unregister);
//		buttonUnregister.setOnClickListener(this);
		list = (ListView) findViewById(R.id.listViewConnections);
	}
/**
 * Common listener for create and join game button
 */
	@Override
	public void onClick(View v) {
		
//		String ip = "130.229.154.233";
//		int port = 8090;
//
//		EditText editTextIp = (EditText) findViewById(R.id.editText_lobby_ip);
//		EditText editTextPort = (EditText) findViewById(R.id.editText_lobby_port);
//		
//		if(editTextIp.getText().length() > 0) {
//			ip = editTextIp.getText().toString();
//		}
//		if(editTextPort.getText().length() > 0) {
//			port = Integer.valueOf(editTextPort.getText().toString());
//		}
		
		// Threading
//		if(registerService != null)
//			registerService.unregisterService();
		if(discoverService != null)
			discoverService.stopDiscoveryService();
		if(v.getId() == R.id.button_lobby_create) {
			registerService = new RegisterService(TicTacToeLobbyP2P.this);

			if(!isGamePlayed) {
				TicTacToeHelper.gameP2p = new TicTacToeGameAPIP2PImpl(TicTacToeLobbyP2P.this);
				TicTacToeHelper.gameP2p.setCallback(TicTacToeLobbyP2P.this);
				isServer = true;
				TicTacToeHelper.gameP2p.createGame(0);
				
				isGamePlayed = true;

				registerService.registerService(TicTacToeHelper.gameP2p.
						getServerSocket().getLocalPort());
			}
		}
		else if(v.getId() == R.id.button_lobby_join) {
			discoverService=new DiscoverService(this.getApplicationContext());
			
			discoverService.setCallback(this);
			discoverService.setActivity(this);
			discoverService.discover();
			
			isServer = false;
			/*
			TicTacToeHelper.game = new TicTacToeGameAPIImpl(TicTacToeLobbyP2P.this, 
					ip, port);
			if(TicTacToeHelper.game.getSocket() != null) {
				TicTacToeHelper.game.setCallback(TicTacToeLobbyP2P.this);
				TicTacToeHelper.game.createGame(0);
			}
			else {
				Toast.makeText(this, "Unable to connect!", Toast.LENGTH_SHORT).show();
			}*/
		}
//		else if(v.getId() == R.id.button_lobby_unregister) {
//			if(registerService != null)
//				registerService.unregisterService();
//			if(discoverService != null)
//				discoverService.stopDiscoveryService();
//		}
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		if(dialog.equals(getDialog())) {
			Log.d(TicTacToeLobbyP2P.class.getName(), "CANCELED!");
			TicTacToeHelper.game.setCallback(TicTacToeLobbyP2P.this);
			TicTacToeHelper.game.cancelGame();
		}
	}
	
	/**
	 * Thread Callback
	 */
	@Override
	public void run() {
		if(getDialog() != null && getDialog().isShowing()) {
			getDialog().dismiss();
		}
		
		String result = "";

		try {
			if (discoverService != null) {
				result = discoverService.getResult();
				if(result != null && result.equals("DiscoveryService")) {
					System.out.println(result);
					
					connectionStrings = new ArrayList<String>();
					ArrayList<NsdServiceInfo> services = (ArrayList<NsdServiceInfo>) discoverService.getServices();
					for(NsdServiceInfo service : services) {
						connectionStrings.add(service.getServiceName());
					}
					
					adapter= new ArrayAdapter<String>(this, 
							android.R.layout.simple_list_item_1,android.R.id.text1, connectionStrings);
					list.setAdapter(adapter);
					
					list.setOnItemClickListener(new OnItemClickListener() {
	
						public void onItemClick(AdapterView<?> parent, View view,
								int position, long id) {
	
							NsdServiceInfo service = discoverService.getServices().get(position);
							
							discoverService.getmNsdManager().resolveService(service, new NsdManager.ResolveListener() {
			                    
			                    @Override
			                    public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
			                        // Called when the resolve fails. Use the error code to debug.
			                        Log.e("NSD", "Resolve failed " + errorCode);
			                        Log.e("NSD", "serivce = " + serviceInfo);
			                    }
			                
			                    @Override
			                    public void onServiceResolved(NsdServiceInfo serviceInfo) {
			                        Log.d("NSD", "Resolve Succeeded. " + serviceInfo);
			
//			                		WifiManager wm = (WifiManager) TicTacToeLobbyP2P.this.getSystemService(Context.WIFI_SERVICE);
//			                		String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
//			            			
//			                        if (serviceInfo.getServiceName().equals(SERVICE_NAME) || 
//			                        		serviceInfo.getHost().getHostAddress().equals("10.0.2.15") ||
//			                        		serviceInfo.getHost().getHostAddress().equals(ip)) {
//			                            Log.d("NSD", "Same IP.");
//			                            return;
//			                        }
			                    
			                        // Obtain port and IP
			                        int hostPort = serviceInfo.getPort();
			                        String hostAddress = serviceInfo.getHost().getHostAddress();
	
			                		System.out.println(hostAddress + ":" + hostPort);
			                		TicTacToeHelper.game = new TicTacToeGameAPIImpl(TicTacToeLobbyP2P.this, 
			                				hostAddress, hostPort);
		                			TicTacToeHelper.game.setCallback(TicTacToeLobbyP2P.this);
		                			TicTacToeHelper.game.createGame(0);
			                    }
			                });
							
						}
					});
				
					discoverService.setResult("");
				}
			}
			if(isServer) {
				result = TicTacToeHelper.gameP2p.getResult();			
				
				System.out.println(result);
				if(result.length() > 0) {
					JSONObject resultObj = new JSONObject(result);
					
					if(resultObj.getString("Request").equals("P2PcreateGame")) {
						// Callback for P2PcreateGame
						Intent i = new Intent(TicTacToeLobbyP2P.this, TicTacToeServer.class);
						i.putExtra("mode", TicTacToeHelper.PVP_1stplayer);
						startActivityForResult(i, TicTacToeHelper.P2P_SERVERDONE);
					}
				}
			}
			else if(!result.equals("DiscoveryService")){
				if(TicTacToeHelper.game != null)
					result = TicTacToeHelper.game.getResult();
				
				if(result != null && result.length() > 0) {
					System.out.println(result);
					JSONObject resultObj = new JSONObject(result);
					
					if(resultObj.getString("Request").equals("createGame")) {
						// Callback for joinGame
						Intent i = new Intent(this, TicTacToeOnline.class);
						i.putExtra("mode", TicTacToeHelper.PVP_2ndplayer);
						startActivityForResult(i, TicTacToeHelper.P2P_CLIENTDONE);
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Callback when game activity is finished and users return to lobby
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		System.out.println("RequestCode: " + requestCode);
		
		if(requestCode == TicTacToeHelper.P2P_SERVERDONE) {
			registerService.unregisterService();
			isGamePlayed = false;
		}
		else if(requestCode == TicTacToeHelper.P2P_CLIENTDONE) {
			connectionStrings.clear();
			adapter.notifyDataSetChanged();
		}
	}
	
}
