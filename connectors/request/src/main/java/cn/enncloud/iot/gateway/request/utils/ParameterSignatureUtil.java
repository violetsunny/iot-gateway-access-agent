/**
 * LY.com Inc.
 * Copyright (c) 2004-2025 All Rights Reserved.
 */
package cn.enncloud.iot.gateway.request.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author kanglele
 * @version $Id: ParameterSignatureUtil, v 0.1 2025/3/24 15:57 kanglele Exp $
 */
public class ParameterSignatureUtil {
    public static String generateSignature(String operatorId, String data, String timeStamp, String seq, String sigSecret) {
        try {
            // 步骤1：在签名密钥后面添加0来创建一个长为64字节的字符串
            String paddedKey = padKey(sigSecret);
            // 步骤2：将字符串与ipad做异或运算
            byte[] ipad = new byte[64];
            for (int i = 0; i < 64; i++) {
                ipad[i] = (byte) 0x36;
            }
            byte[] strBytes = paddedKey.getBytes(StandardCharsets.UTF_8);
            byte[] istrBytes = new byte[64];
            for (int i = 0; i < 64; i++) {
                istrBytes[i] = (byte) (strBytes[i] ^ ipad[i]);
            }
            // 步骤3：将消息内容附加到istr的末尾
            String message = operatorId + data + timeStamp + seq;
            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
            byte[] combinedIstr = new byte[istrBytes.length + messageBytes.length];
            System.arraycopy(istrBytes, 0, combinedIstr, 0, istrBytes.length);
            System.arraycopy(messageBytes, 0, combinedIstr, istrBytes.length, messageBytes.length);
            // 步骤4：对第三步生成的数据流做MD5运算
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] md5Istr = md5.digest(combinedIstr);
            // 步骤5：将字符串与opad做异或运算
            byte[] opad = new byte[64];
            for (int i = 0; i < 64; i++) {
                opad[i] = (byte) 0x5c;
            }
            byte[] ostrBytes = new byte[64];
            for (int i = 0; i < 64; i++) {
                ostrBytes[i] = (byte) (strBytes[i] ^ opad[i]);
            }
            // 步骤6：将第四步的结果附加到ostr的末尾
            byte[] combinedOstr = new byte[ostrBytes.length + md5Istr.length];
            System.arraycopy(ostrBytes, 0, combinedOstr, 0, ostrBytes.length);
            System.arraycopy(md5Istr, 0, combinedOstr, ostrBytes.length, md5Istr.length);
            // 步骤7：对第六步生成的数据流做MD5运算
            byte[] finalDigest = md5.digest(combinedOstr);
            StringBuilder result = new StringBuilder();
            for (byte b : finalDigest) {
                result.append(String.format("%02X", b));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String padKey(String key) {
        if (key.length() >= 64) {
            return key.substring(0, 64);
        }
        StringBuilder paddedKey = new StringBuilder(key);
        while (paddedKey.length() < 64) {
            paddedKey.append('0');
        }
        return paddedKey.toString();
    }
}
