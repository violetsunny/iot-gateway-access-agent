# iot-gateway-new

## Connector

### TCP

TCP通讯中需要使用解码器处理传输中出现的粘包、拆包情况，框架内置了针对不同报文格式的编解码组件。

+ DIRECT  
  不处理，将接收数据直接交给协议组件

+ DELIMITER  
  基于分隔符的解码器  
  配置参数： 
    - maxLength：报文最字节数
    - delimiter： 分隔符，HEX字符串


+ LENGTH_FIELD  
  基于长度字段的解码器  
  配置参数： 
    - maxLength：报文最大字节数
    - lengthFieldOffset：长度字段启示位置
    - lengthFieldLength： 长度字段字节数
    - lengthAdjustment：长度域矫正量
    - initialBytesToStrip：需要丢弃的字节数
    - byteOrder：字节序，BIG_ENDIAN或者LITTLE_ENDIAN


+ JSON_OBJECT  
  Json Object解码器  


+ FIXED_LENGTH  
  固定长度的解码器  
  配置参数： 
    - frameLength：报文长度



#### 配置 TCP Server


```yml
ennwiot:
  serverId: node1
  connector-tcp:
    server:
      name: tcpTestServer
      configuration:
          # 地址
        - address: 0.0.0.0
          # 端口
          port: 36001
          # 解码器类型
          parserType: LENGTH_FIELD
          # 解码器配置
          parserParams:
            byteOrder: LITTLE_ENDIAN
            maxLength: 10240
            lengthFieldOffset: 13
            lengthFieldLength: 2
            lengthAdjustment: 3
            initialBytesToStrip: 0
          # 协议配置
          protocol:
            path: http://127.0.0.1:9000/device/std-enn-tcp.jar
            mainClass: cn.enncloud.iot.gateway.protocol.std.EnnStandardJsonProtocol
            params:
              p1: aaa
              p2: bbb
```

#### 配置 TCP Client

```yml

ennwiot:
  serverId: node1
  connector-tcp:
    client:
      name: tcpTestServer
      configuration:
          # 服务端地址
        - remoteAddress: 127.0.0.1
          # 服务端端口
          remotePort: 36001
          # 是否重连
          reconnectEnabled: true
          # 重连间隔，单位秒
          reconnectInterval: 30
          # 心跳间隔，单位秒
          heartbeatInterval: 10
          # 解码器类型
          parserType: LENGTH_FIELD
          # 解码器配置
          parserParams:
            byteOrder: LITTLE_ENDIAN
            maxLength: 10240
            lengthFieldOffset: 13
            lengthFieldLength: 2
            lengthAdjustment: 3
            initialBytesToStrip: 0
          # 协议配置
          protocol:
            path: http://127.0.0.1:9000/device/std-enn-tcp.jar
            mainClass: cn.enncloud.iot.gateway.protocol.std.EnnStandardJsonProtocol
            params:
              p1: aaa
              p2: bbb
```

### MQTT

### HTTP

### SNMP



## Jar协议开发

### 协议开发

```java

public class MyProtocol extends AbstractProtocol {
    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public List<? extends Message> decodeMulti(byte[] messageBytes, Object... params) throws DecodeMessageException {
        return null;
    }

    @Override
    public byte[] encode(Message message) throws EncodeMessageException {
        return new byte[0];
    }

    @Override
    public byte[] encodeMulti(List<? extends Message> messages) throws EncodeMessageException {
        return new byte[0];
    }

    @Override
    public boolean login(LoginRequest loginRequest) throws AuthException {
        return false;
    }
}

```



## JavaScript脚本支持

### JavaScript引擎

使用Java 8内置Nashorn JavaScript引擎，Nashorn JavaScript基于ECMAScript 5.1（ES5规范）。

### 内置对象和方法

#### $log

$log对象可供开发者在JavaScript脚本中打印各级别日志。

```javascript
// 打印DEBUG日志
$log.debug('this is a debug log');
// 打印INFO日志
$log.info('this is a info log');
// 打印WARN日志
$log.warn('this is a warn log');
// 打印ERROR日志
$log.error('this is a error log');
```

#### $ctx

$ctx对象可供开发者查询设备信息。

+ JavaScript中通过SN获取设备ID

```javascript
var projectId = null;
var sn = "D000001";
var deviceId = $ctx.getDeviceIdBySn(projectId, sn);
```

+ JavaScript中通过设备ID获取设备SN

```javascript
var deviceId = "3541123541";
var sn = $ctx.getSnByDeviceId(deviceId);
```

#### $params

$params为协议配置中配置的自定义参数。

自定义参数(yml)：

```yml
ennwiot:
  serverId: node1
  connector-mqtt:
    name: 标准MQTT Connector
    type: mqtt
    configuration:
      - clientId: protocol-script-test-1
        port: 1883
        host: enn-emq.fat.ennew.com
        password: xxxx
        username: admin
        qos: 0
        topics:
          - /edge/single/+/+/rtg
        protocol:
          type: script
          name: script-protocol-test
          path: http://127.0.0.1/protocol-test/protocol-script-test.js
          # 自定义参数
          params:
            p1: aaa
            p2: bbb
```

JavaScript中获取参数：
```javascript
var p1 = $params.p1;
var p2 = $params.p2;
```



### JavaScript脚本模板


```javascript
codec.decoder(function(data){
    // 字节数组
    var byteArray = data.bytes;
    // 参数
    var topic = data.params[0];
    // 将字节数组转换为字符串
    var deviceMessage = '';
    for(var i=0;i<byteArray.length;i++){
        deviceMessage+=String.fromCharCode(byteArray[i]);
    }
    // TODO 消息解码
    
    // 消息类型
    var msgType = 'DEVICE_REPORT_REQ';
    // 平台设备ID
    var deviceId = '3541123541';
    // 指标
    var metrics = [];
    metrics.push({metricA: ''});
    metrics.push({metricB: ''});
    // 上报时间
    var time = new Date().getTime();
    // 接收时间
    var ingestionTime = new Date().getTime();
    // 消息ID
    var msgId = '';
    // 返回实时上报消息数组
    var msg = {
            deviceId: deviceId,
            timeStamp: time,
            ingestionTime: ingestionTime,
            messageId: msgId,
            metrics: metrics,
            messageType: msgType
        };
    var msgList = [];
    msgList.push(msg);
    return JSON.stringify(msgList);
});


```


### 配置JavaScript协议

```yml

ennwiot:
  serverId: node1
  connector-mqtt:
    name: 标准MQTT Connector
    type: mqtt
    configuration:
      - clientId: protocol-script-test-1
        port: 1883
        host: enn-emq.fat.ennew.com
        password: xxxx
        username: admin
        qos: 0
        topics:
          - /edge/single/+/+/rtg
        # 协议配置
        protocol:
          # 类型指定 script
          type: script
          # 协议名称
          name: script-protocol-test
          # 脚本文件地址
          path: http://127.0.0.1/protocol-test/protocol-script-test.js
          # 或配置脚本文本内容
          script: "function decodeMulti(deviceMessage, params){return [];}"

```