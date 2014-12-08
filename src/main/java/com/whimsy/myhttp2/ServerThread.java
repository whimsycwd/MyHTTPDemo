package com.whimsy.myhttp2;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;

/**
 * Created by whimsy on 11/22/14.
 */
public class ServerThread implements Runnable{
    private Socket client = null;

    public ServerThread(Socket client) {
        this.client = client;
    }


    @Override
    public  void run() {
        try {
            PrintStream out = new PrintStream(client.getOutputStream());

            InputStream is = client.getInputStream();

            String str;

            str = Utils.readLine(is, 0);

            String[] values = str.split(" ");


            if (values[0].equals("GET")) {

                if (values[1].startsWith("/queryById") ) {
                    int id = Integer.parseInt(values[1].substring("/queryById/".length()));

                    ServiceCenter.rtnQueryById(out, id);
                } else {



                    try {
                        String file = this.getClass().getClassLoader().getResource(values[1].substring(1)).getFile();


                        String fileExt = values[1].substring(values[1].lastIndexOf(".") + 1);
                        if (fileExt.equals("png")) {
                            ServiceCenter.doGetImageFile(file, out);
                        } else {
                            ServiceCenter.doGetTextFile(file, out);
                        }
                    } catch (NullPointerException e) {
                        ServiceCenter.rtnNotFound(out);
                    }
                }
            }
            else
            if (values[0].equals("POST") && values[1].equals("/serviceImage")) {
                ServiceCenter.doPost(is, out);
            }
            else
            {
                ServiceCenter.rtnNotFound(out);
            }



            out.close();
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
