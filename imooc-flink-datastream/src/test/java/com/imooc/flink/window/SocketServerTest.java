package com.imooc.flink.window;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author jared
 */
public class SocketServerTest {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(9090);
        Socket connect = serverSocket.accept();
        System.out.println("get connection: " + connect);

        try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(connect.getOutputStream()))) {
            while (true) {
                bufferedWriter.write(ThreadLocalRandom.current().nextInt(0, 1000) + System.lineSeparator());
                bufferedWriter.flush();
            }
        }
    }
}
