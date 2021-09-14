package com.nickmafra.tcpjoystick;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

public class JoyClient extends Thread {

    private static final String TAG = JoyClient.class.getSimpleName();
    private static final int TIME_OUT = 5000;

    private final Activity activity;
    private Socket socket;
    private InetSocketAddress socketAddress;
    private final AtomicReference<InetSocketAddress> atomicSocketAddress = new AtomicReference<>();

    private final Object waitObj = new Object();

    private final ConcurrentLinkedQueue<byte[]> commands = new ConcurrentLinkedQueue<>();

    public JoyClient(Activity activity) {
        this.activity = activity;
    }

    private void log(final String text) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logError(final Exception e) {
        Log.e(TAG, "logError: " + e.getMessage(), e);
        log(e.getMessage());
    }

    private void closeSocket() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            socket = null;
        } catch (IOException e) {
            logError(new RuntimeException("Error during close connection.", e));
        }
    }

    private void reconnect() {
        closeSocket();
        if (socketAddress == null) {
            log("Disconnected");
            return;
        }
        try {
            socket = new Socket();
            socket.connect(socketAddress, TIME_OUT);
            log("Connected");
        } catch (SocketTimeoutException e) {
            Log.e(TAG, "logError: " + e.getMessage(), e);
            log("Connection timeout");
        } catch (IOException e) {
            logError(new RuntimeException("Error during starting connection.", e));
        }
    }

    private boolean reconnectIfNeeded() {
        InetSocketAddress newSocketAddress = atomicSocketAddress.get();
        if (newSocketAddress == socketAddress) {
            return false; // not changed
        }
        socketAddress = atomicSocketAddress.get();
        reconnect();
        return true;
    }

    private boolean sendCommandIfNeeded() {
        if (socket == null || !socket.isConnected() || socket.isClosed()) {
            return false;
        }
        byte[] command = commands.poll();
        if (command == null) {
            return false;
        }
        try {
            socket.getOutputStream().write(command);
        } catch (IOException e) {
            logError(new RuntimeException("Error during command sending.", e));
        }
        return true;
    }

    private boolean doSomething() {
        return reconnectIfNeeded() || sendCommandIfNeeded();
    }

    private void doSomethingOrWait() throws InterruptedException {
        while (!doSomething()) {
            synchronized (waitObj) {
                waitObj.wait();
            }
        }
    }

    @Override
    public void run() {
        try {
            while (!interrupted()) {
                doSomethingOrWait();
            }
        } catch (Exception e) {
            logError(e);
            closeSocket();
            interrupt();
        }
    }

    public void setConnection(String host, int port) {
        synchronized (waitObj) {
            atomicSocketAddress.set(new InetSocketAddress(host, port));
            waitObj.notifyAll();
        }
    }

    public void addCommand(@NonNull byte[] command) {
        synchronized (waitObj) {
            commands.add(command);
            waitObj.notifyAll();
        }
    }
}
