package com.whimsy.myhttp2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by whimsy on 12/6/14.
 */
public class Utils {

    /**
     * 讲时间格式转化炜GMT
     * @return
     */
    public static String GMTTime() {
        Date d=new Date();
        DateFormat format=new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format.format(d);
    }
    /**
     * 这里我们自己模拟读取一行，因为如果使用API中的BufferedReader时，它是读取到一个回车换行后
     * 才返回，否则如果没有读取，则一直阻塞，直接服务器超时自动关闭为止，如果此时还使用BufferedReader
     * 来读时，因为读到最后一行时，最后一行后不会有回车换行符，所以就会等待。如果使用服务器发送回来的
     * 消息头里的Content-Length来截取消息体，这样就不会阻塞
     *
     * contentLe 参数 如果为0时，表示读头，读时我们还是一行一行的返回；如果不为0，表示读消息体，
     * 时我们根据消息体的长度来读完消息体后，客户端自动关闭流，这样不用先到服务器超时来关闭。
     */
    public static String readLine(InputStream is, int contentLe) throws IOException {
        ArrayList lineByteList = new ArrayList();
        byte readByte;
        int total = 0;
        if (contentLe != 0) {
            do {
                readByte = (byte) is.read();
                lineByteList.add(Byte.valueOf(readByte));
                total++;
            } while (total < contentLe);//消息体读还未读完
        } else {
            do {
                readByte = (byte) is.read();
                lineByteList.add(Byte.valueOf(readByte));
            } while (readByte != 10);
        }

        byte[] tmpByteArr = new byte[lineByteList.size()];
        for (int i = 0; i < lineByteList.size(); i++) {
            tmpByteArr[i] = ((Byte) lineByteList.get(i)).byteValue();
        }
        lineByteList.clear();

        return new String(tmpByteArr, "GBK");
    }

    /**
     * 处理字节流, 不转化为字符串, 在处理图片上传的时候,需要直接对字节流进行处理
     *
     * 注意 is.read() 接口在buffer返回的数字是读到的内容数, 所以不是说打开一个很大的buffer就可以读到
     * 所有的数据. 在处理图片上传的时候遇到该坑.  i.e.
     *
     * byte [] bytes = new byte[contentLenth];
     *
     * is.read(bytes); 不一定能够读到contentLength个字节.
     *
     * @param is
     * @param contentLength
     * @return
     * @throws IOException
     */
    public static byte[] readContent(InputStream is, int contentLength) throws IOException {
        ArrayList lineByteList = new ArrayList();
        byte readByte;
        int total = 0;
        do {
            readByte = (byte) is.read();
            lineByteList.add(Byte.valueOf(readByte));
            total++;
        } while (total < contentLength);//消息体读还未读完


        byte[] tmpByteArr = new byte[lineByteList.size()];
        for (int i = 0; i < lineByteList.size(); i++) {
            tmpByteArr[i] = ((Byte) lineByteList.get(i)).byteValue();
        }
        lineByteList.clear();
        return tmpByteArr;
    }

    /**
     * 处理文件上传相关
     * @param bytes
     * @param boundaryBytes
     * @param startPos
     * @return
     */
    public static int find(byte[] bytes, byte[] boundaryBytes, int startPos) {


        int cnt = 0;

        for (int i = startPos; i <= bytes.length - boundaryBytes.length; ++i) {

            ++cnt;
            boolean flag = true;

            for (int j = 0; j < boundaryBytes.length; ++j) {
                if (bytes[i + j] != boundaryBytes[j]) {
                    flag = false;
                    break;
                }
            }
            if (flag)  return i;
        }
        return -1;
    }

    /**
     * 分割表单数据中的各个部分
     * @param bytes
     * @param number
     * @param boundary
     * @return
     */

    public static byte[] readMultiPart(byte[] bytes, int number, String boundary) {

        byte[] boundaryBytes = boundary.getBytes();
        byte [] sep = {13,10,13,10}; // \r\n\r\n

        int startPos = -1;
        while (number >= 0) {
            startPos = Utils.find(bytes, boundaryBytes, startPos + 1);

            --number;
        }

        int imageStart = Utils.find(bytes, sep, startPos + 1);
        int endPos = Utils.find(bytes, boundaryBytes, startPos+1);

        imageStart += 4;
        endPos -= 4;

        byte [] retBytes = new byte[endPos - imageStart];

        System.arraycopy(bytes, imageStart, retBytes, 0, endPos - imageStart);
        return retBytes;
    }
}
