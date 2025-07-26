package simulate;

import cn.enncloud.iot.gateway.utils.ByteArrayUtil;

public class ByteArrayTest {
    public static void main(String[] args) {
        String s = "hello";
        byte []b = s.getBytes();
        System.out.println(ByteArrayUtil.bytes2HexStr(s.getBytes()));
        System.out.println(new String(ByteArrayUtil.hexStr2Bytes(ByteArrayUtil.bytes2HexStr(s.getBytes()))));
    }
}
