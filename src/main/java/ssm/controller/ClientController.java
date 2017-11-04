package ssm.controller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ssm.entity.Hosts;
import ssm.entity.UserEntity;
import ssm.service.HostService;
import sun.security.krb5.internal.PAForUserEnc;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

@Controller

public class ClientController {
    @Autowired
    private HostService hostService;
    private static final Log logger = LogFactory.getLog(ClientController.class);
    //列表展示数据库中的内容
    @RequestMapping(value = "/show")
    public String Test(ModelMap modelMap){
        logger.info("进入test");
        modelMap.addAttribute("hosts",hostService.getList());
        return "Test";
    }
    //跳转页面
//    @RequestMapping(value = "/addPage")
//    public String addPage(){
//        logger.info("进入add");
//        return "add";
//    }
    //数据库删除数据
    @RequestMapping(value = "/delete",method = RequestMethod.POST)
    public String delete(int[] id){
//        logger.info("hosts中的内容是 "+id);
//        int[] ids = null;
        for (int i : id) {
            logger.info("删除的元素是 " + i);
            hostService.deleteItem(i);
        }


        return "redirect:/show";
    }
    //数据库增加记录
    @RequestMapping(value = "/addItem", method=RequestMethod.POST)
    public String add(Hosts hosts){
        logger.info("进入add 内容是 "+hosts);
       hostService.addItem(hosts);
        return "redirect:/show";
    }

    @RequestMapping(value="/{formName}")
    public String gotoForm(@PathVariable String formName) {
        // 动态跳转页面
        return formName;
    }
    //上传文件

    //上传文件，使用对象上传，上传文件会自动绑定到user的MultipartFile中
    @RequestMapping(value="/upload")
    public String register(HttpServletRequest request,
                           @ModelAttribute UserEntity user,
                           Model model) throws Exception {
        System.out.println(user.getUsername());
        //如果文件不为空，写入上传路径
        if(!user.getImage().isEmpty()) {
            //上传文件路径
            String path = request.getServletContext().getRealPath("/images/");
            //上传文件名
            String filename = user.getImage().getOriginalFilename();
            File filepath = new File(path,filename);
            //判断路径是否存在，如果不存在就创建一个
            if (!filepath.getParentFile().exists()) {
                filepath.getParentFile().mkdirs();
            }
            //将上传文件保存到一个目标文件当中
            user.getImage().transferTo(new File(path + File.separator + filename));
            //将用户添加到model
            model.addAttribute("user", user);
            return "userInfo";
        } else {
            return "error";
        }
    }
    @RequestMapping(value="/download")
    public ResponseEntity<byte[]> download(HttpServletRequest request,
                                           @RequestParam("filename") String filename,
                                           Model model)throws Exception {
        //下载文件路径
        String path = request.getServletContext().getRealPath("/images/");
        File file = new File(path + File.separator + filename);
        HttpHeaders headers = new HttpHeaders();
        //下载显示的文件名，解决中文名称乱码问题
        String downloadFielName = new String(filename.getBytes("UTF-8"),"iso-8859-1");
        //通知浏览器以attachment（下载方式）打开图片
        headers.setContentDispositionFormData("attachment", downloadFielName);
        //application/octet-stream ： 二进制流数据（最常见的文件下载）。
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file),
                headers, HttpStatus.CREATED);
    }
}
