package com.deqing.google_glass_trinker;

import java.util.HashMap;
import java.util.Iterator;

import com.google.android.glass.app.Card;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

	Handler handler = new Handler();

    private Card message_card;
    UsbManager mUsbManager;
	PendingIntent mPermissionIntent;
	UsbDevice target_device=null;
	
	private static final String ACTION_USB_PERMISSION =
		    "com.android.example.USB_PERMISSION";	
	
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        if (ACTION_USB_PERMISSION.equals(action)) {
	            synchronized (this) {
	                UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

	                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
	                    if(device != null){
	                      //call method to set up device communication
	                    	System.out.println("GOT Permission!");
	                    	connect_trinket();
	                   }
	                }else {
	                	System.out.println("permission denied for device " + device);
	                }
	            }
	        }
	    }
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

		//IntentFilter attachedFilter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		//registerReceiver(mUsbAttachedReceiver, attachedFilter);
		
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		registerReceiver(mUsbReceiver, filter);
		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		
	    message_card = new Card(this);
	    message_card.setText("Trying to connect to usb flashlight");
	    message_card.setInfo("");
	    View card_view=message_card.toView ();
	    setContentView(card_view);
	    
	    search_usb_device();
	}

	public boolean search_usb_device(){
		boolean result=false;
		HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		
		while(deviceIterator.hasNext()){
		    UsbDevice device = deviceIterator.next();
		    System.out.println("VID "+String.format("%04X", device.getVendorId())+" PID "+String.format("%04X", device.getProductId())+" NAME "+device.getDeviceName());
		    if (device.getVendorId()==0x0525 && device.getProductId()==0xA4A0){
		    	target_device=device;
		    	break;
		    }
		}
		if (target_device!=null){
			System.out.println("GOT DEVICE!");
			message_card.setText("Trinket found!");
    	    View card_view=message_card.toView ();
    	    setContentView(card_view);
			mUsbManager.requestPermission(target_device, mPermissionIntent);
			result=true;
		}else{
			System.out.println("NOT GOT DEVICE!!");
			message_card.setText("Trinket Not found!");
    	    View card_view=message_card.toView ();
    	    setContentView(card_view);
    	    result=false;
		}
		return result;
	}
	
	
	public void connect_trinket(){
		if (mUsbManager.hasPermission (target_device)){
			final UsbDeviceConnection usb_connection;			
			
			usb_connection = mUsbManager.openDevice(target_device); 
			
			byte[] bytes={0};
			int TIMEOUT = 0;

			final int send_RequestType   =(0<<7)|(1<<5)|(0); //USBRQ_DIR_HOST_TO_DEVICE USBRQ_TYPE_CLASS USBRQ_RCPT_DEVICE
			final int receive_RequestType=(1<<7)|(1<<5)|(0); //USBRQ_DIR_DEVICE_TO_HOST USBRQ_TYPE_CLASS USBRQ_RCPT_DEVICE
			
			usb_connection.controlTransfer(send_RequestType, 0x09, 0, 0x80, bytes, 0, TIMEOUT);	//Toggle LED on #1
			
			usb_connection.controlTransfer(send_RequestType, 0x09, 0, 0x81, bytes, 0, TIMEOUT);	//request temperature
			
			handler.postDelayed(new Runnable(){
		    @Override
		    	public void run() {
		    		byte[] bytes={0};
		    		int a=usb_connection.controlTransfer(receive_RequestType, 0x01, 0, 0, bytes, 1, 0);
		    		if (a>0){
		    			message_card.setText("On chip temperature: "+Integer.toString(bytes[0]&0xFF));
			    	    View card_view=message_card.toView ();
			    	    setContentView(card_view);
		    		}
		        }
		    }, 20);
		}else{
			message_card.setText("NO USB permission");
    	    View card_view=message_card.toView ();
    	    setContentView(card_view);
		}
	}


    @Override
    public void onDestroy() {
    	handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

}
