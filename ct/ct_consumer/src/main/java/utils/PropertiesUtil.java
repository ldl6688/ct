package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @ Autheor:ldl
 * @Description:
 * @Date: 2018/3/20 15:34
 * @Modified By:
 */

public class PropertiesUtil {
    public static Properties properties = null;

    static {
        InputStream is = ClassLoader.getSystemResourceAsStream("hbase_consumer.properties");
        properties = new Properties();

        try {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

}
