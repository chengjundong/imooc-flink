package com.imooc.flink.socket;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author jucheng
 * @since 2022/2/12
 */
public class MySocketServer implements Runnable {

    private final SocketDataGenerator dataGenerator;

    public MySocketServer(SocketDataGenerator dataGenerator) {
        this.dataGenerator = dataGenerator;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(9090);
            Socket connect = serverSocket.accept();
            System.out.println("get connection: " + connect);

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connect.getOutputStream()))) {
                while (true) {
                    writer.write(dataGenerator.generateData() + System.lineSeparator());
                    writer.flush();
                }
            }


        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
