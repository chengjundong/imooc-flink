package com.imooc.flink.window;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * @author jucheng
 */
public class SocketTest {

    public static void main(String[] args) throws IOException {

        Socket socket = new Socket("127.0.0.1", 9090);
        System.out.println(socket.isConnected());
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println(bufferedReader.readLine());
    }
}
