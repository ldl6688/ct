package controller;

import bean.CallLog;
import bean.Contact;
import com.google.gson.Gson;
import dao.CallLogDAO;
import dao.ContactDAO;
import entries.QueryInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * @ Autheor:ldl
 * @Description:
 * @Date: 2018/3/26 11:00
 * @Modified By:
 */

@Controller
public class CallLogHandler {

//    @Autowired
    @Resource
    private ContactDAO contactDAO;

    @Autowired
    private CallLogDAO callLogDAO;

    @RequestMapping("/queryContact")
    public ModelAndView query(Contact contact) {
//        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");

//        ContactDAO contactDAO = applicationContext.getBean(ContactDAO.class);
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("id", String.valueOf(contact.getId()));
        List<Contact> contactList = contactDAO.getContactWithId(paramMap);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("jsp/queryContact");
        modelAndView.addObject("contacts", contactList);
        return modelAndView;
    }

    @RequestMapping("/queryContactList")
    public ModelAndView queryList() {
        List<Contact> contactList = contactDAO.getContacts();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("jsp/queryContact");
        modelAndView.addObject("contacts", contactList);
        return modelAndView;
    }

    @RequestMapping("/queryCallLogList")
    public ModelAndView queryCallLogList(QueryInfo queryInfo) {
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("telephone", String.valueOf(queryInfo.getTelephone()));
        paramMap.put("year", String.valueOf(queryInfo.getYear()));
        paramMap.put("day", String.valueOf(queryInfo.getDay()));

        List<CallLog> callLogList = callLogDAO.getCallLogList(paramMap);

        Gson gson = new Gson();
        String resultList = gson.toJson(callLogList);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("jsp/callLogList");
        modelAndView.addObject("callLogList", resultList);
        return modelAndView;
    }

    @RequestMapping("/queryCallLogList2")
    public String queryCallLogList2(Model model,QueryInfo queryInfo) {
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("telephone", String.valueOf(queryInfo.getTelephone()));
        paramMap.put("year", String.valueOf(queryInfo.getYear()));
        paramMap.put("day", String.valueOf(queryInfo.getDay()));

        List<CallLog> callLogList = callLogDAO.getCallLogList(paramMap);

        StringBuilder dateString = new StringBuilder();
        StringBuilder countString = new StringBuilder();
        StringBuilder durationString = new StringBuilder();

        for(int i = 0; i < callLogList.size(); i++){
            CallLog callLog = callLogList.get(i);
            if(Integer.valueOf(callLog.getMonth()) > 0){
                dateString.append(callLog.getMonth()).append("月").append(",");
                countString.append(callLog.getCall_sum()).append(",");
                durationString.append(Float.valueOf(callLog.getCall_duration_sum()) / 60f).append(",");
            }
        }
        model.addAttribute("telephone", callLogList.get(0).getTelephone());
        model.addAttribute("name", callLogList.get(0).getName());
        model.addAttribute("date", dateString.deleteCharAt(dateString.length() - 1));
        model.addAttribute("count", countString.deleteCharAt(countString.length() - 1));
        model.addAttribute("duration", durationString.deleteCharAt(durationString.length() - 1));
        return "jsp/CallLogListEchart";
    }

    /**
     * 返回json字符串格式
     * @param model
     * @param queryInfo
     * @return
     */
    @ResponseBody
    @RequestMapping("/queryCallLogList3")
    public String queryCallLogList3(Model model,QueryInfo queryInfo) {
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("telephone", String.valueOf(queryInfo.getTelephone()));
        paramMap.put("year", String.valueOf(queryInfo.getYear()));
        paramMap.put("day", String.valueOf(queryInfo.getDay()));

        List<CallLog> callLogList = callLogDAO.getCallLogList(paramMap);

        Gson gson = new Gson();
        String resultList = gson.toJson(callLogList);



        return resultList;
    }
}
