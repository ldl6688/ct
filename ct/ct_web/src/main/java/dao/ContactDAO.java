package dao;

import bean.Contact;
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
public class ContactDAO {
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public List<Contact> getContacts(){
        String sql = "SELECT id, telephone, name FROM tb_contacts;";
        BeanPropertyRowMapper<Contact> contactBeanPropertyRowMapper = new BeanPropertyRowMapper<>(Contact.class);
        List<Contact> contactList = namedParameterJdbcTemplate.query(sql, contactBeanPropertyRowMapper);
        return contactList;
    }


    public List<Contact> getContactWithId(HashMap<String, String> hashMap){
        String sql = "SELECT id, telephone, name FROM tb_contacts WHERE id = :id;";
        BeanPropertyRowMapper<Contact> contactBeanPropertyRowMapper = new BeanPropertyRowMapper<>(Contact.class);
        List<Contact> contactList = namedParameterJdbcTemplate.query(sql, hashMap, contactBeanPropertyRowMapper);
        return contactList;
    }


}
