/**
 * Copyright (C), 2015-2019, XXX有限公司
 * FileName: OrderForController
 * Author:   hxq
 * Date:     2019/7/8 16:22
 * Description: 订单的控制类
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hxq.newordersystem.controller;

import com.hxq.newordersystem.entity.Fish;
import com.hxq.newordersystem.entity.OrderFish;
import com.hxq.newordersystem.entity.OrderFor;
import com.hxq.newordersystem.entity.OrderForFish;
import com.hxq.newordersystem.repository.FishRepository;
import com.hxq.newordersystem.repository.OrderFishRepository;
import com.hxq.newordersystem.repository.OrderForRepository;
import net.sf.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 〈一句话功能简述〉<br> 
 * 〈订单的控制类〉
 *
 * @author hxq
 * @create 2019/7/8
 * @since 1.0.0
 */
@Controller
public class OrderForController {

    @Autowired
    private OrderForRepository orderForRepository;

    @Autowired
    private OrderFishRepository orderFishRepository;

    @Autowired
    private FishRepository fishRepository;

    /**
     * 差一个从小程序传值过来的订单
     */


    /**
     * Web端
     * 找到所有的订单详情
     * 前端请求href为/findallorderforfish，请求方式是get
     * 得到的结果数据类型是OrderForFish集合，存放于model中，命名为{orderforfishlist}
     * 返回一个用于显示所有订单详情的页面
     * 已测试接口，成功
     * @param model
     * @return
     */
   // @ResponseBody
    @GetMapping("/findallorderforfish")
    public String findAll(Model model, HttpSession session){
        int identity=(int)session.getAttribute("identity");
        List<OrderFor> orderFors=orderForRepository.findAll();
        ArrayList<OrderForFish> orderForFishArrayList=new ArrayList<OrderForFish>();
        for (OrderFor o:orderFors){
            //先存订单
            OrderForFish orderForFish=new OrderForFish();
            orderForFish.setOrderFor(o);
            //找到订单上的菜品
            List<OrderFish> orderFishList=orderFishRepository.findAllByOrderId(o.getId());
            //存入订单上的所有菜
            orderForFish.setOrderFishs(orderFishList);
            //存入链表
            orderForFishArrayList.add(orderForFish);

            //System.out.println(orderForFish.toString());
        }
        model.addAttribute("orderforfishlist",orderForFishArrayList);
        if(identity==1)
            return "Admin/Order/Order_manage";
        else
            return "Waiter/Order/Order_manage";
    }

    /**
     * Web端
     * 通过订单id找到一个订单详情
     * 前端请求href为/findoneorderforfish/{id}，请求方式是get
     * 得到的结果是OrderForFish,存入model中，取名为{oederforfish}
     * 返回一个显示一个订单详情的页面
     * 已测试接口，测试成功
     * @param id
     * @param model
     * @return
     */
   // @ResponseBody
    @GetMapping("/findoneorderforfish/{id}")
    public String findOne(@PathVariable("id")Integer id,Model model){
        OrderForFish orderForFish=new OrderForFish();
        //找到订单
        Optional<OrderFor> orderFor=orderForRepository.findById(id);
        //存订单
        orderForFish.setOrderFor(orderFor.get());
        //找到该订单所点的所有菜
        List<OrderFish> orderFishList=orderFishRepository.findAllByOrderId(orderFor.get().getId());
        //存订单上的菜
        orderForFish.setOrderFishs(orderFishList);

        //System.out.println(orderForFish.toString());
        //存入model
        model.addAttribute("oederforfish",orderForFish);
        return "一个用于显示一个订单详情的页面";


    }


    /**
     * Web端
     * 根据订单id查出一个订单，然后根据大的折扣与总价想成进行更新优惠后的价格newSum，更新订单
     * 前端请求的是Form表单，action为/addnewsum，有两个参数
     * 返回值为重定向到根据订单id查询一个订单的函数
     * 已测试接口，测试成功
     * @param id 订单id
     * @param newSumPoint 折扣的数值，例如8.8折为0.8
     * @return
     */
    //@ResponseBody
    @PostMapping("/addnewsum")
    public String addNewSum(@RequestParam("id")Integer id,
                            @RequestParam("newSumPoint")Float newSumPoint){
        Optional<OrderFor> o=orderForRepository.findById(id);
        OrderFor orderFor=o.get();
        Float newSum=orderFor.getSum()*newSumPoint;
        orderFor.setNewSum(newSum);
        orderForRepository.save(orderFor);
        //System.out.println(orderFor.toString());
       // model.addAttribute("orderfor",orderFor);
        return "redirect:/findallorderforfish";//重定向到通过订单id显示订单详情的页面

    }


    /**
     * Web端
     * 在订单详情中删除一道菜，更新订单的总价
     * 前端请求href为/deleteorderfish/{id}，请求方式为get
     *返回值为重定向到一个订单详情页面
     * @param orderFishId
     * @return
     */
   // @ResponseBody
    @GetMapping("/deleteorderfish/{id}")
    public String deleteOrderFish(@PathVariable("id")Integer orderFishId){
        //找出待删除的订单里的那道菜
        Optional<OrderFish> orderFishOptional=orderFishRepository.findById(orderFishId);
        //找出订单
        Optional<OrderFor> orderForOptional=orderForRepository.findById(orderFishOptional.get().getOrderId());
        OrderFor orderFor=orderForOptional.get();
        //订单总价减去删掉的菜的总价
        Float sum=orderFor.getSum();
        sum=sum-(orderFishOptional.get().getPrice()*orderFishOptional.get().getCount());
        //更新订单总价
        orderFor.setSum(sum);
        orderForRepository.save(orderFor);
        //删除待删除的订单里的那道菜
        orderFishRepository.deleteById(orderFishId);
        return "redirect:/findallorderforfish";//重定向到通过订单id显示订单详情的页面
    }

    /**
     * Web端
     * 修改订单页面详情里的某道菜的数量
     * 前端 请求例子 <a class="btn btn-sm btn-primary" th:href="@{/updatecount(id=${orderforfish.orderfish.id},count=${orderforfish.orderfish.count})}" >提交</a>
     * 前端需要发送两个参数，被修改数量的那道菜的id和修改后的count，请求方式为Get
     * 更新订单里的菜OrderFish中的被修改的那道菜的数量，更新订单的总价
     * 返回：重定向到显示一个订单详情页面
     * 已测试接口，成功
     * @param request
     * @return
     */
    //@ResponseBody
    @PostMapping("/updatecount")
    public String updateCount(HttpServletRequest request){
        Integer id=Integer.valueOf(request.getParameter("id"));
        Integer count=Integer.valueOf(request.getParameter("count"));
        //找出修改数量的那道菜
        Optional<OrderFish> orderFishOptional=orderFishRepository.findById(id);
        OrderFish orderFish=orderFishOptional.get();
        //找出订单
        Optional<OrderFor> orderForOptional=orderForRepository.findById(orderFish.getOrderId());
        OrderFor orderFor=orderForOptional.get();
        //求得原本的订单里的总价
        Float sum=orderFor.getSum();
        sum=sum-(orderFish.getPrice()*orderFish.getCount());
        //更新订单里的菜的数量
        orderFish.setCount(count);
        orderFishRepository.save(orderFish);
        //更新订单的总价
        sum=sum+(orderFish.getCount()*orderFish.getPrice());
        orderFor.setSum(sum);
        orderForRepository.save(orderFor);
        return "redirect:/findallorderforfish";//重定向到通过订单id显示订单详情的页面
    }

    /**
     * Web端
     * 模拟支付
     * 前端href为/paymoney/{id}，需要传一个订单id,请求方式为get
     * 更新订单里的支付状态
     * 返回:重定向到一个显示订单详情的页面
     * @param id
     * @return
     */
    //@ResponseBody
    @GetMapping("/paymoney/{id}")
    public String payMoney(@PathVariable("id")Integer id){
        Optional<OrderFor> orderForOptional=orderForRepository.findById(id);
        OrderFor orderFor=orderForOptional.get();
        //将订单状态置为已支付
        System.out.println(id);
        orderFor.setState("已支付");
        orderForRepository.save(orderFor);
        return "redirect:/findallorderforfish";//重定向到通过订单id显示订单详情的页面
    }

    /**
     * Web端
     * 再次点菜
     * 在订单管理管理中点击点菜按钮
     * 前端请求为/showaddotherfish/{id}，需要提交一个订单id,请求方式是GET
     * 重定向到一个返回所有菜的界面
     * 已测试接口，成功
     * @param id
     * @param model
     * @return
     */
    //@ResponseBody
    @GetMapping("/showaddotherfish/{id}")
    public String showAllFish(@PathVariable("id")Integer id,Model model){
        model.addAttribute("orderId",id);
        return "redirect:/findalladd";
    }

    /**
     * Web端
     * 再次点菜的功能
     * 在显示所有菜的页面中，选择某道菜，修改数量，点击添加按钮
     * 前端以form表单请求，需要提交订单id,点的菜的id，和点的数量，请求方式以post方式请求
     * 重定向到显示所有菜品的页面，再次点菜
     * 已测试接口，成功
     * @param orderForId
     * @param fishId
     * @param count
     * @return
     */
    //@ResponseBody
    @PostMapping("/addorderfish")
    public String addOtherFish(@RequestParam(value = "orderForId")Integer orderForId,
                               @RequestParam(value = "fishId")Integer fishId,
                               @RequestParam(value = "count")Integer count,
                               HttpSession session){
        //找出所添加的那道菜
        Optional<Fish> fishOptional=fishRepository.findById(fishId);
        //找到订单
        Optional<OrderFor> orderForOptional=orderForRepository.findById(orderForId);
        //存入订单里的菜
        OrderFish orderFish=new OrderFish();
        orderFish.setCount(count);
        orderFish.setFishId(fishId);
        orderFish.setFishName(fishOptional.get().getName());
        orderFish.setOrderId(orderForId);
        orderFish.setPrice(fishOptional.get().getPrice());
        orderFishRepository.save(orderFish);
        //修改订单里的总价
        OrderFor orderFor=orderForOptional.get();
        Float sum=orderFor.getSum();
        sum=sum+orderFish.getPrice()*count;
        orderFor.setSum(sum);
        orderForRepository.save(orderFor);
        System.out.println(fishId);
        return "redirect:/showaddpage/"+orderForId+"";
    }

    /**
     * Web端
     * 点完菜之后的操作
     * 在显示所有的菜品的页面点击结束按钮，需要传一个订单（OrderFor）id
     * 前端请求href为/finishadd/{id},请求方式为get
     * 返回到显示一个订单管理的页面
     * 已测试接口，成功
     * @param id
     * @return
     */
    //@ResponseBody
    @GetMapping("/finishadd")
    public String finishAdd(){
        return "redirect:/findallorderforfish";//重定向到通过订单id显示订单详情的页面
    }


    /**



     * 差一个从小程序传值过来的订单



     * 小程序端



     * 从小程序传输订单过来



     * 小程序中的数据需转化为String：fishlist:JSON.stringify（fishlist）



     * 请求方式是Post。请求路径为url+/orderfor/orderfish



     * "Content-Type": "application/x-www-form-urlencoded"



     * @param userId 用户id



     * @param tableNumber 桌号



     * @param number 人数



     * @param fishList 订单菜的链表



     * @return



     */



    @ResponseBody



    @PostMapping("/orderfor/orderfish")



    public Map getOrderForFishFromVX(@RequestParam("userid")String userId,



                                     @RequestParam("tableNumber")Integer tableNumber,



                                     @RequestParam("number")Integer number,



                                     @RequestParam("fishlist")String fishList){



        Map map=new HashMap();



        //存订单



        OrderFor orderFor=new OrderFor();



        orderFor.setNumber(number);



        orderFor.setTableNumber(tableNumber);



        orderFor.setUserId(Integer.valueOf(userId));



        orderFor.setState("未支付");



        String s=new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());



        Date date=null;



        try {



            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");



            date=sdf.parse(s);



        }catch (Exception e){



            e.printStackTrace();



        }



        orderFor.setOrderTime(date);



        //获取出订单id



        OrderFor u=orderForRepository.save(orderFor);



        //String 转换成 集合  前端数组转换String



        JSONArray jsonlist = JSONArray.fromObject(fishList);



        List<OrderFish> orderFishList=(List<OrderFish>) JSONArray.toCollection(jsonlist,OrderFish.class);



        //存菜



        Float sum=0f;



        for (OrderFish orderFish:orderFishList){



            orderFish.setOrderId(u.getId());



            orderFishRepository.save(orderFish);



            sum=sum+(orderFish.getPrice()*orderFish.getCount());



        }



        //存订单的总价



        u.setSum(sum);



        orderForRepository.save(u);







        map.put("status", 1);



        map.put("msg", "下单成功");







        return map;







    }
}

