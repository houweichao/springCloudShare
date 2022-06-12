package com.elead.provider.controller;

import com.elead.provider.domain.Good;
import com.elead.provider.service.GoodService;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/Good")
public class GoodController {

    @Autowired
    GoodService goodService;

    @GetMapping("/find/{id}")
    @HystrixCommand(fallbackMethod = "findById_fallBack",
        commandProperties = {   //HystrixCommandProperties文件中包含所有的属性
                @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",
                                    value = "3000")
        })
    public Good findById(@PathVariable("id") int id,String username){
        /*int i = 1/0;*/
        /*try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        if(id == 1){
            int i = 1/0;
        }
        System.out.println(username);
        return goodService.findById(id);
    }

    public Good findById_fallBack(int id,String username){
        Good good = goodService.findById(id);
        good.setName("降级啦");
        System.out.println(username);
        return good;
    }

}
