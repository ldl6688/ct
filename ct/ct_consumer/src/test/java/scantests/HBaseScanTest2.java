package scantests;


import contants.ContantsUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;
import utils.PropertiesUtil;
import utils.ScanRowKeyUtil;

import java.io.IOException;

/**
 * @ Autheor:ldl
 * @Description:
 * @Date: 2018/3/23 8:39
 * @Modified By:
 */

public class HBaseScanTest2 {

    private static Configuration conf = ContantsUtil.conf;
    private static String tableName = 
            PropertiesUtil.getProperty("hbase.calllog.tablename");

    @Test
    public void scanTest() throws IOException {

        String phone = "15154563015";
        String startDate = "2019-01-01";
        String endDate = "2019-03-02";


        Table hTable = new HTable(conf, tableName);
        Scan scan = new Scan();

        ScanRowKeyUtil scanRowKeyUtil = new ScanRowKeyUtil(phone, startDate, endDate);

        while (scanRowKeyUtil.hasNext()) {
            String[] rowKeys = scanRowKeyUtil.next();
            scan.setStartRow(Bytes.toBytes(rowKeys[0]));
            scan.setStopRow(Bytes.toBytes(rowKeys[1]));

            System.out.println("时间范围" +
                    rowKeys[0].substring(15, 21) +
                    "---" +
                    rowKeys[1].substring(15, 21));

            ResultScanner resultScanner = hTable.getScanner(scan);

            for (Result result : resultScanner) {
                Cell[] cells = result.rawCells();
                StringBuilder sb = new StringBuilder();
                sb.append(Bytes.toString(result.getRow())).append(",");

                for (Cell c : cells) {
                    sb.append(Bytes.toString(CellUtil.cloneValue(c))).append(",");
                }
                System.out.println(sb.toString());
            }

        }

    }

}
