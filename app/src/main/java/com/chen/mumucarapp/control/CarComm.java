package com.chen.mumucarapp.control;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Timer;
import java.util.TimerTask;

public class CarComm {
    public static final int AUTHORIZE_FAILED = 3;

    public static final int CONNECTED = 1;

    public static final int CONNECTING = 0;

    public static final int DISCONNECTED = 2;

    private static final String PASSWORD = "password";

    public static final int POWER_REPORT = 4;

    public static final String KEY_POWER = "battery";

    public static final String KEY_DIR = "direct";

    public static final String KEY_CONN = "connect";

    public static final String KEY_PASS = "password";


    private int[] speed;

    private SocketAddress addr = new InetSocketAddress("192.168.2.3", 9000);

    protected boolean authorized = false;

//    private Handler handler;

    private boolean isClosed = true;

    protected boolean isConnected = false;

    protected boolean keepAlive = true;

    private Object lock = new Object();

    private boolean[] orientations;

    private int password;

    protected boolean paused = false;

    public static Session session = new Session();;

    private Timer timer;

    private DatagramSocket udpSocket;

    public CarComm(boolean[] paramArrayOfboolean, int[] paramArrayOfint) {
//        this.handler = paramHandler;
        this.orientations = paramArrayOfboolean;
        this.speed = paramArrayOfint;
    }

    private boolean connectUdp() {
        try {
            if (this.udpSocket == null)
                this.udpSocket = new DatagramSocket(29123);
            return true;
        } catch (Exception exception) {
            close();
            exception.printStackTrace();
            return false;
        }
    }

    private boolean send(String paramString) {
        if (paramString != null && this.udpSocket != null)
            try {
                this.udpSocket.send(new DatagramPacket(paramString.getBytes(), (paramString.getBytes()).length, this.addr));
                Log.d("send: {}", paramString);
                return true;
            } catch (Exception exception) {
                Log.d("send: {} failed" , paramString );
                exception.printStackTrace();
            }
        return false;
    }

    private void start() {
        this.timer = new Timer();
        this.timer.schedule(new SendCommandTask(), 1000L, 100L);
    }

    public void close() {
        stop();
        pause();
        this.keepAlive = false;
        if (this.udpSocket != null) {
            this.udpSocket.close();
            this.udpSocket = null;
        }
        this.isClosed = true;
        this.authorized = false;
    }

    public boolean connect() {
        start();
        resume();
        this.keepAlive = true;
        if (!this.isClosed)
            synchronized (this.lock) {
                this.lock.notify();
                Log.d("Carcomm","notify");
//                this.handler.obtainMessage(0).sendToTarget();
                return true;
            }
        if (connectUdp()) {
            this.isClosed = false;
            keepAlive();
            receive();
            return true;
        }
        return false;
    }

    public void disconnect() {
        stop();
        pause();
        this.authorized = false;
    }

    public void keepAlive() {
        (new Thread() {
            public void run() {
                int i = 0;
//                CarComm.this.handler.obtainMessage(0).sendToTarget();
                while (true) {
                    if (!CarComm.this.keepAlive)
                        return;
                    int j = i;
                    if (!CarComm.this.paused) {
                        String str;
                        if (CarComm.this.authorized) {
                            str = "$?";
                        } else {
                            CarComm.this.password = CarComm.this.session.getInt(KEY_PASS, Util.get4Random());
                            str = "*" + CarComm.this.password + CarComm.this.password;
                        }
                        if (!CarComm.this.send(str)) {
                            j = ++i;
                            if (i > 3) {
                                j = 0;
//                                CarComm.this.handler.obtainMessage(2).sendToTarget();
                                CarComm.this.authorized = false;
                                CarComm.this.isConnected = false;
                                CarComm.this.pause();
                            }
                        } else {
                            CarComm.this.isConnected = true;
                            j = 0;
                        }
                    }
                    try {
                        Thread.sleep(3000L);
                        i = j;
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        return;
                    }
                }
            }
        }).start();
    }

    public void pause() {
        this.paused = true;
    }

    public void receive() {
        (new Thread() {
            public void run() {
                byte[] arrayOfByte = new byte[63];
                DatagramPacket datagramPacket = new DatagramPacket(arrayOfByte, arrayOfByte.length);
                while (true) {
                    if (!CarComm.this.keepAlive)
                        return;
                    if (!CarComm.this.paused) {
                        try {
                            CarComm.this.udpSocket.receive(datagramPacket);
                            String str = new String(datagramPacket.getData(), datagramPacket.getOffset(), datagramPacket.getLength());
                            if (str.startsWith("*A")) {
//                                CarComm.this.handler.obtainMessage(1).sendToTarget();
                                CarComm.this.session.saveInt(KEY_CONN, 1);
                                CarComm.this.authorized = true;
                                CarComm.this.session.saveInt(KEY_PASS, CarComm.this.password);
                            } else if (str.startsWith("*R")) {
//                                CarComm.this.handler.obtainMessage(3).sendToTarget();
                                CarComm.this.authorized = false;
                            } else if (str.startsWith("*Z")) {
//                                CarComm.this.handler.obtainMessage(1).sendToTarget();
                                CarComm.this.session.saveInt(KEY_CONN, 1);
                                CarComm.this.authorized = true;
                                CarComm.this.session.saveInt(KEY_PASS, CarComm.this.password);
                            } else {
                                boolean bool = str.startsWith("$!");
                                if (bool) {
                                    try {
                                        int i = Integer.valueOf(str.substring(2), 16).intValue();
                                        CarComm.this.session.saveInt(KEY_POWER, i);
//                                        CarComm.this.handler.obtainMessage(4, i, i).sendToTarget();
//                                        Log.d("startwith receive{}, i = {}", str, i);
                                    } catch (Exception exception) {
                                        Log.e("Exception in auth: {}", str);
                                    }
                                } else if (str.startsWith("*?password")) {
                                    CarComm.this.authorized = false;
//                                    Log.d("auth feild: {}", str);
                                }
                            }
                            Log.d("Carcomm","udp receive: {}, saved password:{}" + str + CarComm.this.session.getInt(KEY_PASS,900));
                        } catch (Exception exception) {
                            exception.printStackTrace();
                            Log.e("Carcomm", "exception: {}",exception);
                        }
                        continue;
                    }
                    synchronized (CarComm.this.lock) {
                        Log.d("Carcomm","wait");
                        try {
                            CarComm.this.lock.wait(10000L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            Log.e("Carcomm", "exception: {}",e);
                        }
                    }
                    /* monitor exit ClassFileLocalVariableReferenceExpression{type=ObjectType{java/lang/Object}, name=SYNTHETIC_LOCAL_VARIABLE_4} */
                }
            }
        }).start();
    }

    public void resume() {
        this.paused = false;
    }

    public boolean sendWithPwd(String paramString, int[] paramArrayOfint) {
        if (this.isConnected && paramString != null && this.udpSocket != null) {
            String str = String.format("%s", new Object[] { Integer.valueOf(paramArrayOfint[0]) });
            Log.d("Carcomm","sendWithPwd paramString : {} array of int: {}"+ paramString + str);
            try {
                byte[] arrayOfByte = (String.valueOf(paramString) + this.password + str).getBytes();
                this.udpSocket.send(new DatagramPacket(arrayOfByte, arrayOfByte.length, this.addr));
                Log.d("Carcomm","send with pwd: {} " + paramString + this.password + str);
                return true;
            } catch (Exception exception) {
                Log.e("Carcomm","send with pwd: {}" + paramString + this.password + " failed");
                exception.printStackTrace();
            }
        }
        return false;
    }

    public void stop() {
        if (this.timer != null) {
            this.timer.cancel();
            this.timer = null;
        }
    }

    private class SendCommandTask extends TimerTask {
        private SendCommandTask() {}

        public void run() {
            int dir = CarComm.this.session.getInt(KEY_DIR,0);
            if (dir == 0) CarComm.this.sendWithPwd(Commands.getCommand(CarComm.this.orientations), CarComm.this.speed);
        }
    }
}
