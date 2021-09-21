package com.imooc.flink;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Socket data sender
 *
 * @author jucheng
 */
public class SocketDataSender {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        for (; ; ) {
            Socket socket = serverSocket.accept();
            socket.setKeepAlive(true);
            System.out.println("socket get: " + socket);
            try (DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
                for (int i=0; i<100; i++) {
                    dos.writeBytes("java,python,java,");
                    System.out.println("flush data...");
                    dos.flush();
                }
            }
        }
    }
}
