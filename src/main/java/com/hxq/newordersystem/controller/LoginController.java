/**
 * Copyright (C), 2015-2019, XXX有限公司
 * FileName: LoginController
 * Author:   hxq
 * Date:     2019/7/9 0:01
 * Description: 登陆的控制类
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hxq.newordersystem.controller;

import com.hxq.newordersystem.entity.Manager;
import com.hxq.newordersystem.entity.Waiter;
import com.hxq.newordersystem.repository.ManagerRepository;
import com.hxq.newordersystem.repository.WaiterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.Optional;

/**
 * 〈一句话功能简述〉<br> 
 * 〈登陆的控制类〉
 *
 * @author hxq
 * @create 2019/7/9
 * @since 1.0.0
 */
@Controller
public class LoginController {

    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private WaiterRepository waiterRepository;

    @GetMapping("/")
    public String logi(){
        return "login_page";
    }
    @PostMapping("/login")
    public String Login(@RequestParam("id")String id,
                        @RequestParam("password")String password,
                        @RequestParam("identity")Integer identity,
                        HttpSession session){
        //identity:1管理员，2：服务员

        if (null==id||null==password) {
            return "redirect:/";
        }
            //管理员

        if(identity.equals(1)){
            Optional<Manager> managerOptional=managerRepository.findById(id);
            //判断是否存在id
            if (managerOptional.isPresent()){
                //判断密码是否一致
               if (managerOptional.get().getPassword().equals(password)){
                   session.setAttribute("identity",identity);
                   session.setAttribute("identity1","管理员");
                   return "redirect:/findallfish";
               }else {
                   return "redirect:/";
               }
            }
        }else{//服务员
            Optional<Waiter> waiterOptional=waiterRepository.findById(id);
            //判断是否存在id
            if (waiterOptional.isPresent()){
                //判断密码是否一致
                if(waiterOptional.get().getPassword().equals(password)){
                    session.setAttribute("identity",identity);
                    session.setAttribute("identity1","服务员");
                    return "redirect:findallorderforfish";
                }else {
                    return "redirect:/";
                }
            }

        }
        return "redirect:/";

    }
}

