package com.example.gg.nfcproject.util;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;

public class NfcBankCardReader {

    private static final String TAG = "NfcBankCardReader";

    // PSE AID: 1PAY.SYS.DDF01
    private static final String PSE_AID = "315041592E5359532E4444463031";

    // 常见银行卡 AID 示例（银联借记/贷记）
    private static final String UNION_PAY_AID = "A000000333010102";

    // 选择 PSE 命令
    private static final String CMD_SELECT_PSE = "00A404000E" + PSE_AID;

    // 选择 AID 命令前缀
    private static final String CMD_SELECT_AID_PREFIX = "00A40400";

    // 读取记录命令前缀 (SFI=01, Record=1c)
    private static final String CMD_READ_RECORD_PREFIX = "00B2010C00";

    private IsoDep isoDep;

    public NfcBankCardReader(Tag tag) {
        if (tag != null) {
            isoDep = IsoDep.get(tag);
        }
    }

    /**
     * 连接卡片
     */
    public boolean connect() {
        if (isoDep == null) {
            LogUtil.e(TAG, "IsoDep is null");
            return false;
        }
        try {
            if (!isoDep.isConnected()) {
                isoDep.connect();
                isoDep.setTimeout(5000);
            }
            return true;
        } catch (IOException e) {
            LogUtil.e(TAG, "connect error: " + e.getMessage());
            return false;
        }
    }

    /**
     * 断开连接
     */
    public void close() {
        if (isoDep != null && isoDep.isConnected()) {
            try {
                isoDep.close();
            } catch (IOException e) {
                LogUtil.e(TAG, "close error: " + e.getMessage());
            }
        }
    }

    /**
     * 发送 APDU 命令（十六进制字符串）
     */
    public byte[] transceive(String hexCmd) throws IOException {
        if (isoDep == null || !isoDep.isConnected()) {
            throw new IOException("Not connected to card");
        }
        LogUtil.d(TAG, "will transceive: " + hexCmd);
        byte[] cmd = hexStringToBytes(hexCmd);
        byte[] response = isoDep.transceive(cmd);

        // 检查 SW1 SW2 是否为 9000
        if (response.length < 2) {
            throw new IOException("Invalid response");
        }
        int sw1 = response[response.length - 2] & 0xFF;
        int sw2 = response[response.length - 1] & 0xFF;
        if (sw1 != 0x90 || sw2 != 0x00) {
            throw new IOException(String.format("APDU failed: %02X%02X", sw1, sw2));
        }
        return Arrays.copyOf(response, response.length - 2);
    }

    /**
     * 选择 PSE
     */
    public byte[] selectPSE() throws IOException {
        return transceive(CMD_SELECT_PSE);
    }

    /**
     * 选择 AID
     */
    public byte[] selectAID(String aid) throws IOException {
        String lenHex = String.format("%02X", aid.length() / 2);
        String cmd = CMD_SELECT_AID_PREFIX + lenHex + aid;
        return transceive(cmd);
    }

    /**
     * 读取记录
     */
    public byte[] readRecord() throws IOException {
        return transceive(CMD_READ_RECORD_PREFIX);
    }

    /**
     * 解析卡号（PAN）
     * 尝试从 5F20 标签中提取
     */
    public String parsePan(byte[] data) {
        String tag = "5F20";
        byte[] tagBytes = hexStringToBytes(tag);
        int idx = indexOfTag(data, tagBytes);
        if (idx < 0) {
            return null;
        }
        int length = data[idx + 1] & 0xFF;
        byte[] value = Arrays.copyOfRange(data, idx + 2, idx + 2 + length);
        return bytesToHex(value);
    }

    /**
     * 查找标签位置
     */
    private int indexOfTag(byte[] data, byte[] tag) {
        for (int i = 0; i <= data.length - tag.length; i++) {
            if (data[i] == tag[0]) {
                boolean match = true;
                for (int j = 1; j < tag.length; j++) {
                    if (data[i + j] != tag[j]) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 读取银行卡号主流程
     */
    public String getCardNumber() {
        try {
            if (!connect()) {
                return null;
            }
            // 1. 选 PSE
            selectPSE();
            // 2. 选 AID（银联示例）
            selectAID(UNION_PAY_AID);
            // 3. 读记录
            byte[] record = readRecord();
            // 4. 解析卡号
            return parsePan(record);
        } catch (IOException e) {
            LogUtil.e(TAG, "getCardNumber error: " + e.getMessage());
            return null;
        } finally {
            close();
        }
    }

    // 工具方法：十六进制字符串转字节数组
    public static byte[] hexStringToBytes(String s) {
        int len = s.length();
        if (len % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have even length");
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    // 工具方法：字节数组转十六进制字符串
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}