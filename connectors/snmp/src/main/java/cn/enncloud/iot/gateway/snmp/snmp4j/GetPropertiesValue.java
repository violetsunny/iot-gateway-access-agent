package cn.enncloud.iot.gateway.snmp.snmp4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class GetPropertiesValue {

    // slf4j  log
    private Logger log = LoggerFactory.getLogger(GetPropertiesValue.class);

    private Map<String, String> map = null;
    private static Map<String, GetPropertiesValue> cache = new HashMap<String, GetPropertiesValue>();

    private GetPropertiesValue(String configFile) {
        if (log.isDebugEnabled()) {
            log.debug("configFile=" + configFile);
        }
        InputStream is = this.getClass()
                .getResourceAsStream("/" + configFile);
        if (log.isDebugEnabled()) {
            log.debug("InputStream=" + is);
        }
        Properties p = new Properties();
        if (is != null) {
            try {
                p.load(is);
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error("load config error.file=" + configFile, e);
                }
            } finally {
                try {
                    is.close();
                } catch (IOException e) {

                }
            }
        }
        map = new HashMap<>();
        Iterator keys = p.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String value = p.getProperty(key);
            map.put(key, value);
        }
        if (log.isDebugEnabled()) {
            log.debug(configFile + ".content:" + map);
        }
    }

    public static synchronized GetPropertiesValue getInstance(String configFile) {
        GetPropertiesValue pv = cache.get(configFile);
        if (pv == null) {
            pv = new GetPropertiesValue(configFile);
            cache.put(configFile, pv);
        }
        return pv;
    }

    public String getValue(String key) {
        return (String) map.get(key);
    }


}
