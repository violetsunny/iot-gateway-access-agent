package cn.enncloud.iot.gateway.modbus.constant;

/**
 * Modbus常用变量
 *
 * @author Mr.Jia
 * @date 2021/11/10 18:11
 */
public interface ModbusConstant {
    /**
     * 功能码
     */
    int FUN_CODE_READ_01 = 0x01;
    int FUN_CODE_READ_02 = 0x02;
    int FUN_CODE_READ_03 = 0x03;
    int FUN_CODE_READ_04 = 0x04;

    /**
     * 写指令：05 (0x05)写单个线圈
     */
    int FUN_CODE_READ_05 = 0x05;

    /**
     * 写指令：06 (0x06)写单个寄存器
     */
    int FUN_CODE_READ_06 = 0x06;

    /**
     * 写指令：15 (0x0F) 写多个线圈
     */
    int FUN_CODE_READ_15 = 0x0F;

    /**
     * 写指令：16 (0x10) 写多个寄存器
     */
    int FUN_CODE_READ_10 = 0x10;

    /**
     * 默认功能码
     */
    int FUN_CODE_READ_DEFAULT = FUN_CODE_READ_03;

    /**
     * modbus-rtu帧头长度
     */
    int RTU_HEAD_LEN = 3;

    /**
     * modbus-rtu的CRC16长度
     */
    int RTU_CRC_LEN = 2;
    /**
     * 响应错误码 0x81
     */
    int FUN_CODE_READ01_ERROR = 0x81;

    /**
     * 响应错误码 0x84
     */
    int FUN_CODE_READ04_ERROR = 0x84;

    /**
     * 响应错误码 0x85
     */
    int FUN_CODE_WRITE85_ERROR = 0x85;

    /**
     * 响应错误码 0x86
     */
    int FUN_CODE_WRITE86_ERROR = 0x86;

    /**
     * 响应错误码 0x90
     */
    int FUN_CODE_WRITE90_ERROR = 0x90;

    /**
     * 针对modbus的01、02\05\06 功能码，只需按照协议规范进行编码和解码，不用依赖用户配置的数据类型和字节序,默认读取一个寄存器。
     */
    int FUN_CODE_01_02_DEFAULT_VALUE = 1;

    /**
     * modbus-rtu默认的最大组帧长度改为10
     */
    int FRAMING_LENGTH_LEN_DEFAULT = 10;

    /**
     * modbus-rtu单个写和读指令响应报文前缀长度
     */
    int RTU_HEAD_WRITE_ONE_CMD_LEN = 6;

    /**
     * modbus-rtu批量写指令(写多个寄存器)响应报文长度
     */
    int RTU_HEAD_WRITE_BATCH_CMD_LEN = 7;

    /**
     * 线圈状态 0
     */
    int RTU_CMD_ADDRESS_0 = 0;

    /**
     * 线圈状态 1
     */
    int RTU_CMD_ADDRESS_1 = 1;
}