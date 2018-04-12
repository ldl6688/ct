package consumer;

import hbase.HBaseDAO;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import utils.PropertiesUtil;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;

/**
 * @ Autheor:ldl
 * @Description:
 * @Date: 2018/3/20 15:28
 * @Modified By:
 */

public class HBaseConsumer {
    public static void main(String[] args) throws IOException, ParseException {
        KafkaConsumer<String, String> kafkaConsumer =
                new KafkaConsumer<String, String>(PropertiesUtil.properties);
        kafkaConsumer.subscribe(Arrays.asList(PropertiesUtil.getProperty("kafka.topics")));

        HBaseDAO hbaseDao = new HBaseDAO();
        while (true) {
            ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(100);
            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                String value = consumerRecord.value();
                System.out.println(value);

                //向Hbase表中插入数据
                hbaseDao.put(value);
            }
        }
    }


}
