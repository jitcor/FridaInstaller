package com.github.h.f.installer.util;


import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * XZip解压缩工具类
 * @author mazaiting
 */
public class XZipUtil {
    public static final String TAG="XZipUtil";
    /**缓冲字节*/
    public static final int BUFFER = 1024;
    /**后缀名*/
    public static final String EXT = ".xz";

    /**
     * 数据压缩
     * @param data 数据字节
     * @return
     * @throws IOException
     */
    public static byte[] compress(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 压缩
        compress(bais, baos);

        byte[] output = baos.toByteArray();

        // 从缓冲区刷新数据
        baos.flush();
        // 关闭流
        baos.close();
        bais.close();

        return output;
    }

    /**
     * 文件压缩
     * @param file 文件
     * @param delete 是否删除原文件
     * @throws IOException
     */
    public static void compress(File file, boolean delete) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        FileOutputStream fos = new FileOutputStream(file.getPath() + EXT);

        compress(fis, fos);

        fos.flush();

        fos.close();
        fis.close();

        if (delete) {
            file.delete();
        }
    }

    /**
     * 数据压缩
     * @param is 输入流
     * @param os 输出流
     * @throws IOException
     */
    private static void compress(InputStream is, OutputStream os) throws IOException {
        XZCompressorOutputStream bcos = new XZCompressorOutputStream(os);

        int count;
        byte data[] = new byte[BUFFER];

        while((count = is.read(data, 0, BUFFER)) != -1){
            bcos.write(data, 0, count);
        }

        bcos.finish();

        bcos.flush();
        bcos.close();
    }

    /**
     * 文件压缩
     * @param path 文件路径
     * @param delete 是否删除原文件
     * @throws IOException
     */
    public static void compress(String path, boolean delete) throws IOException{
        File file = new File(path);
        compress(file, delete);
    }

    /**
     * 数据解压缩
     * @param data 数据
     * @return
     * @throws IOException
     */
    public static byte[] deCompress(byte[] data) throws IOException{
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // 解压缩
        deCompress(bais, baos);

        data = baos.toByteArray();

        baos.flush();
        baos.close();
        bais.close();

        return data;
    }

    /**
     * 文件解压缩
     * @param file 文件
     * @param delete 是否删除源文件
     * @throws IOException
     */
    public static String deCompress(File file, boolean delete) throws IOException{
        FileInputStream fis = new FileInputStream(file);
        String out=file.getPath().substring(0,file.getPath().length()-EXT.length());
        FileOutputStream fos = new FileOutputStream(out);

        deCompress(fis, fos);

        fos.flush();
        fos.close();
        fis.close();

        if (delete) {
            file.delete();
        }
        return out;
    }

    /**
     * 解压缩
     * @param is 输入流
     * @param os 输出流
     * @throws IOException
     */
    private static void deCompress(InputStream is, OutputStream os) throws IOException {
        XZCompressorInputStream bcis = new XZCompressorInputStream(is);

        int count;
        byte data[] = new byte[BUFFER];

        while((count = bcis.read(data, 0, BUFFER)) != -1){
            os.write(data, 0, count);
        }

        bcis.close();
    }

    /**
     * 文件解压缩
     * @param path 文件路径
     * @param delete 是否删除源文件
     * @throws IOException
     */
    public static void deCompress(String path, boolean delete) throws IOException{
        File file = new File(path);
        deCompress(file, delete);
    }

}
