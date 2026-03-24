package com.example.gg.nfcproject.util;


import android.os.Build;

import java.lang.reflect.Method;
import java.util.Locale;

public class GPMethods {
    private static final String HexStr = "0123456789abcdef";

    /**
     * 将byte数组转换成16进制组成的字符串 例如 一个byte数组 b[0]=0x07;b[1]=0x10;...b[5]=0xFB; byte2hex(b);
     * 将返回一个字符串"0710BE8716FB"
     *
     * @param bytes 待转换的byte数组
     * @return
     */
    public static String bytesToHexString(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        StringBuilder buff = new StringBuilder();
        int len = bytes.length;
        for (byte aByte : bytes) {
            if ((aByte & 0xff) < 16) {
                buff.append('0');
            }
            buff.append(Integer.toHexString(aByte & 0xff));
        }
        return buff.toString();
    }

    /**
     * 将byte数组转换成16进制组成的字符串 例如 一个byte数组 b[0]=0x07;b[1]=0x10;...b[5]=0xFB; byte2hex(b);
     * 将返回一个字符串"0710BE8716FB"
     *
     * @param bytes 待转换的byte数组
     * @return
     */
    public static String bytesToHexString(byte[] bytes, int len) {
        if (bytes == null) {
            return "";
        }
        StringBuilder buff = new StringBuilder();
        for (int j = 0; j < len; j++) {
            if ((bytes[j] & 0xff) < 16) {
                buff.append('0');
            }
            buff.append(Integer.toHexString(bytes[j] & 0xff));
        }
        return buff.toString();
    }

    /**
     * transfer hex string like "80 02 00 00 00 00" to bytes.
     *
     * @param hexStr hexStr
     * @return byteArray.
     */
    public static byte[] hexToByteArr(String hexStr) {
        char[] charArr = hexStr.replace(" ", "").toLowerCase(Locale.ROOT).toCharArray();
        byte btArr[] = new byte[charArr.length / 2];
        int index = 0;
        for (int i = 0; i < charArr.length; i++) {
            int highBit = HexStr.indexOf(charArr[i]);
            int lowBit = HexStr.indexOf(charArr[++i]);
            btArr[index] = (byte) (highBit << 4 | lowBit);
            index++;
        }
        return btArr;
    }

    /**
     * bytes数组转成int 例如{0x6f,0x00}转成0x6f00
     *
     * @param src
     * @return
     */
    public static int bytes2Int(byte[] src) {
        return Integer.parseInt(bytesToHexString(src), 16);
    }

    /**
     * 将整数转为16进行数后并以指定长度返回（当实际长度大于指定长度时只返回从末位开始指定长度的值）
     *
     * @param val int 待转换整数
     * @param len int 指定长度
     * @return String
     */
    public static String Int2HexStr(int val, int len) {
        String result = Integer.toHexString(val).toUpperCase(Locale.getDefault());
        int r_len = result.length();
        if (r_len > len) {
            return result.substring(r_len - len, r_len);
        }
        if (r_len == len) {
            return result;
        }
        StringBuffer strBuff = new StringBuffer(result);
        for (int i = 0; i < len - r_len; i++) {
            strBuff.insert(0, '0');
        }
        return strBuff.toString();
    }


    /**
     * 将16进制组成的字符串转换成byte数组 例如 hex2Byte("0710BE8716FB"); 将返回一个byte数组
     * b[0]=0x07;b[1]=0x10;...b[5]=0xFB;
     *
     * @param src 待转换的16进制字符串
     * @return
     */
    public static byte[] str2bytes(String src) {
        if (src == null || src.length() == 0 || src.length() % 2 != 0) {
            return null;
        }
        int nSrcLen = src.length();
        byte[] byteArrayResult = new byte[nSrcLen / 2];
        StringBuilder strBufTemp = new StringBuilder(src);
        String strTemp;
        int i = 0;
        while (i < strBufTemp.length() - 1) {
            strTemp = src.substring(i, i + 2);
            byteArrayResult[i / 2] = (byte) Integer.parseInt(strTemp, 16);
            i += 2;
        }
        return byteArrayResult;
    }


    /**
     * 获取系统属性
     *
     * @param key
     * @param defaultValue
     * @return
     */
    @SuppressWarnings("finally")
    public static String getProperty(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String) (get.invoke(c, key, defaultValue));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static boolean setAndroidOsSystemProperties(String key, String value) {
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method systemProperties_set = c.getMethod("set", String.class, String.class);
            systemProperties_set.invoke(c, key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static final String[] units = {"B", "KB", "MB", "GB", "TB"};

    public static String getUnit(float size) {
        float unit = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? 1000 : 1024;
        int index = 0;
        while (size > unit && index < 4) {
            size = size / unit;
            index++;
        }
        return String.format(Locale.getDefault(), " %.2f %s", size, units[index]);
    }
}
