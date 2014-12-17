
package com.example.android.wifidirect.discovery;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/* Adapted from the example at https://android.googlesource.com/platform/development/+
    /master/samples/WiFiDirectServiceDiscovery/src/com/example/android/wifidirect/
    discovery/WiFiServiceDiscoveryActivity.java
*/

public class ClientSocketHandler extends Thread {

    private static final String TAG = "ClientSocketHandler";
    private Handler handler;
    private ChatManager chat;
    private InetAddress mAddress;

    public ClientSocketHandler(Handler handler, InetAddress groupOwnerAddress) {
        Log.d(TAG, "ClientSocketHandler(Handler handler, InetAddress groupOwnerAddress)    (bjs)");    //bjs
        this.handler = handler;
        this.mAddress = groupOwnerAddress;
    }

    @Override
    public void run() {
        Log.d(TAG, "run()    (bjs)");    //bjs
        Socket socket = new Socket();
        try {
            socket.bind(null);
            socket.connect(new InetSocketAddress(mAddress.getHostAddress(),
                    WiFiServiceDiscoveryActivity.SERVER_PORT), 5000);
            Log.d(TAG, "Launching the I/O handler");
            chat = new ChatManager(socket, handler);
            new Thread(chat).start();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return;
        }
    }

    public ChatManager getChat() {
        Log.d(TAG, "getChat()    (bjs)");    //bjs
        return chat;
    }

}
