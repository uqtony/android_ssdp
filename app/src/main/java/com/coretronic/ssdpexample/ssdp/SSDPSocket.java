package com.coretronic.ssdpexample.ssdp;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Enumeration;

public class SSDPSocket {

    private MulticastSocket multicastSocket;
    private InetAddress inetAddress;
    SocketAddress mSSDPMulticastGroup;

    public SSDPSocket() throws IOException {
        InetAddress localInAddress = InetAddress.getLocalHost();
        //默认地址和端口：port： 1900,  address：239.255.255.250
        mSSDPMulticastGroup = new InetSocketAddress(SSDPConstants.ADDRESS, SSDPConstants.PORT);
        multicastSocket = new MulticastSocket(SSDPConstants.PORT); // Bind some random port for receiving datagram
//        multicastSocket = new MulticastSocket(new InetSocketAddress(localInAddress,
//                0)); // Bind some random port for receiving datagram
        //multicastSocket = new MulticastSocket(mSSDPMulticastGroup); // Bind some random port for receiving datagram
        inetAddress = InetAddress.getByName(SSDPConstants.ADDRESS);
        NetworkInterface netIf = NetworkInterface
                .getByInetAddress(localInAddress);
        multicastSocket.joinGroup(mSSDPMulticastGroup, getWifiNetworkInterface());
    }

    /* Used to send SSDP packet */
    public void send(String data) throws IOException {
        DatagramPacket dp = new DatagramPacket(data.getBytes(), data.length(), mSSDPMulticastGroup);
        multicastSocket.send(dp);
    }

    /* Used to receive SSDP packet */
    public DatagramPacket receive() throws IOException {
        byte[] buf = new byte[1024];
        DatagramPacket dp = new DatagramPacket(buf, buf.length);
        multicastSocket.receive(dp);
        return dp;
    }

    public void close() {
        if (multicastSocket != null) {
            multicastSocket.close();
        }
    }

    public static NetworkInterface getWifiNetworkInterface() throws SocketException{
        Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
        NetworkInterface eth0 = null;
        while (enumeration.hasMoreElements()) {
            eth0 = enumeration.nextElement();
            if (eth0.getName().contains("wlan0"))
                return eth0;
        }
        return null;
    }

    public static void listNetworkInterface() throws SocketException {
        Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
        NetworkInterface eth0 = null;
        while (enumeration.hasMoreElements()) {
            eth0 = enumeration.nextElement();
        }
    }
}