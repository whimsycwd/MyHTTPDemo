package com.whimsy.myhttp2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * Created by whimsy on 12/6/14.
 */
public class ServiceCenter {

    private static int BUFFER_SIZE = 1024*1024*10;

    /**
     * 渲染view.html, 填入查出的name & image路径
     * @param id
     * @return
     * @throws NullPointerException
     * @throws FileNotFoundException
     */
    private static String renderQueryById(int id) throws NullPointerException, FileNotFoundException {

        String nameFile = Server.class.getClassLoader().getResource("Data/" + id + "-name.txt").getFile();


        Scanner in = new Scanner(new File(nameFile));
        String name = in.next();
        in.close();

        String viewFile = Server.class.getClassLoader().getResource("view.html").getFile();

        in = new Scanner(new File(viewFile));

        StringBuilder sb = new StringBuilder();
        while (in.hasNext()) {
            sb.append(in.next()+"\n");
        }
        String content = sb.toString();

        content = content.replace("${name}", name);
        content = content.replace("${id}", Integer.toString(id));
        content = content.replace("${image}", "Image/" + id + ".png");

        return content;

    }

    /**
     * 获取图片
     * @param file
     * @param out
     * @throws IOException
     */
    public static void doGetImageFile(String file, PrintStream out) throws IOException {


        FileInputStream fin = new FileInputStream(file);
        byte [] buffer = new byte[BUFFER_SIZE];  //  10M


        int contentLenth = 0;
        int len = 0;

        while ((len = fin.read(buffer)) != -1) {
            contentLenth += len;
        }

        out.println("HTTP/1.1 200 OK");
        out.println("Date: " + Utils.GMTTime());
        out.println("Content-Type: image/png");
        out.println("Content-Length : " + contentLenth);
        out.println();

        out.write(buffer, 0, contentLenth);

    }
    public static void doGetTextFile(String file, PrintStream out) throws IOException {


        FileInputStream fin = new FileInputStream(file);
        byte [] buffer = new byte[BUFFER_SIZE];


        int contentLenth = 0;
        int len = 0;

        while ((len = fin.read(buffer)) != -1) {
            contentLenth += len;
        }

        out.println("HTTP/1.1 200 OK");
        out.println("Date: " + Utils.GMTTime());
        out.println("Content-Type: text/html");
        out.println("Content-Length : " + contentLenth);
        out.println();

        out.write(buffer, 0, contentLenth);

    }

    /**
     * 查不到
     * @param out
     */
    public static void rtnNotFound(PrintStream out) {
        out.println("HTTP/1.1 404 Not Fond");
    }

    /**
     * 返回跟页面
     * @param out
     * @throws IOException
     */

    public static void rtnHome(PrintStream out) throws IOException {

        String file = Server.class.getClassLoader().getResource("index.html").getFile();
        ServiceCenter.doGetTextFile(file, out);
    }

    /**
     * 根据请求创建数据
     * @param is
     * @param out
     * @throws IOException
     */
    public static void doPost(InputStream is, PrintStream out) throws IOException {
        String line = "";

        String boundary = "";

        int contentLength = -1;
        do {
            line = Utils.readLine(is, 0);
            if (line.startsWith("Content-Length")) {
                contentLength = Integer.parseInt(line.split(":")[1].trim());
            }

            if (line.startsWith("Content-Type")) {
                int lastIndex=line.lastIndexOf("=");
                boundary=line.substring(lastIndex+1,line.length());
            }
        } while (!line.equals("\r\n"));
  //      System.out.println("boundary=\n------------------\n" + boundary + "\n-------------------\n");


        byte[] bytes = Utils.readContent(is, contentLength);


        byte[] nameBytes = Utils.readMultiPart(bytes, 0, boundary);
        byte[] idBytes = Utils.readMultiPart(bytes, 1, boundary);
        byte[] imageBytes = Utils.readMultiPart(bytes, 2, boundary);

        String name = new String(nameBytes);
        String id = new String(idBytes);


        int filesize = imageBytes.length;


        File fileDir = new File(Server.class.getClassLoader().getResource("").getPath()+"/Image");
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }

        fileDir = new File(Server.class.getClassLoader().getResource("").getPath()+"/Data");
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }


        File file = new File(Server.class.getClassLoader().getResource("Image").getPath()+("/"+id+".png"));
        FileOutputStream fileOut = new FileOutputStream(file);

        fileOut.write(imageBytes, 0, imageBytes.length);
        fileOut.close();


        file = new File( Server.class.getClassLoader().getResource("Data").getPath()+("/"+id+"-name.txt"));

        fileOut = new FileOutputStream(file);
        fileOut.write(nameBytes, 0, nameBytes.length);
        fileOut.close();

        ServiceCenter.rtnHome(out);
    }

    /**
     * 通过ID获取已经存入的相关信息.
     * @param out
     * @param id
     */
    public static void rtnQueryById(PrintStream out, int id) {

        try {
            String content = renderQueryById(id);
            int contentLenth = content.getBytes().length;

            out.println("HTTP/1.1 200 OK");
            out.println("Date: " + Utils.GMTTime());
            out.println("Content-Type: text/html");
            out.println("Content-Length : " + contentLenth);
            out.println();

            out.println(content);
        } catch (NullPointerException e) {
            rtnNotFound(out);
        } catch (FileNotFoundException e) {
            rtnNotFound(out);
        }

    }
}
