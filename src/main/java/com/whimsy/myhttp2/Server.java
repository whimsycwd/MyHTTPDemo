package com.whimsy.myhttp2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by whimsy on 11/22/14.
 */
public class Server {

    public void work() throws IOException {
        ServerSocket server = new ServerSocket(10080);
        Socket client = null;

        boolean flag = true;
        while (flag) {
            client = server.accept();
            System.out.println("Connected~");

            new Thread(new ServerThread(client)).start();
        }

        server.close();
    }
    public static void main(String [] args) throws Exception {
        new Server().work();
    }


}
