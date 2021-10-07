package com.imooc.flink;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Socket data sender
 *
 * @author jucheng
 */
public class SocketDataSender {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(9999);
        Scanner scanner = new Scanner(System.in);
        for (; ; ) {
            Socket socket = serverSocket.accept();
            socket.setKeepAlive(true);
            System.out.println("socket get: " + socket);
            try (DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
                for (;;) {
                    System.out.println("please write a sentence, words should be split by a white-space...");
                    String str = scanner.nextLine();
                    dos.writeBytes(str + System.lineSeparator());
                    System.out.println("flush data...");
                    dos.flush();
                }
            }
        }
    }
}
