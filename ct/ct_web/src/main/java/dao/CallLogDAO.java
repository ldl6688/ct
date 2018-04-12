package dao;

import bean.CallLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

/**
 * @ Autheor:ldl
 * @Description:
 * @Date: 2018/3/26 11:00
 * @Modified By:
 */

@Repository
public class CallLogDAO {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    public List<CallLog> getCallLogList(HashMap<String, String> hashMap){
        //1、电话号码 2、年 3、日
        String sql = "SELECT t3.id_date_contact, t3.id_date_dimension, t3.id_contact, t3.call_sum, t3.call_duration_sum , t3.telephone, t3.name, t4.year, t4.month, t4.day FROM (SELECT t2.id_date_contact, t2.id_date_dimension, t2.id_contact, t2.call_sum, t2.call_duration_sum , t1.telephone, t1.name FROM (SELECT id, telephone, name FROM tb_contacts WHERE telephone = :telephone ) t1 INNER JOIN tb_call t2 ON t1.id = t2.id_contact ) t3 INNER JOIN (SELECT id, year, month, day FROM tb_dimension_date WHERE year = :year AND day = :day ) t4 ON t3.id_date_dimension = t4.id ORDER BY t4.year, t4.month, t4.day;";
        BeanPropertyRowMapper<CallLog> contactBeanPropertyRowMapper = new BeanPropertyRowMapper<>(CallLog.class);
        List<CallLog> contactList = namedParameterJdbcTemplate.query(sql, hashMap, contactBeanPropertyRowMapper);
        return contactList;
    }

}

