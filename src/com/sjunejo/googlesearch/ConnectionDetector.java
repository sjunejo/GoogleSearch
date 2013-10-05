package com.sjunejo.googlesearch;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
 
/**
 * Checks for connection to the internet.
 * @author Sadruddin
 *
 */
public class ConnectionDetector {
     
    private Context context;
     
    /**
     * Constructor method
     * @param context the current context
     */
    public ConnectionDetector(Context context){
        this.context = context;
    }
 
    /**
     * Method that checks for internet connection
     * @return true if there is an available internet connection,
     * false if there's not.
     */
    public boolean isConnectedToInternet(){
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
          if (connectivity != null) 
          {
              NetworkInfo[] info = connectivity.getAllNetworkInfo();
              if (info != null){
            	  // Goes through all possible types of internet connections
            	  for (int i = 0; i < info.length; i++){
                	  if (info[i].getState() == NetworkInfo.State.CONNECTED){
                          return true;
                      }
                  }
              }
          }
          // No connection found
          return false;
    } // End of isConnectedToInternet() method
    
} // End of ConnectionDetector class definition.