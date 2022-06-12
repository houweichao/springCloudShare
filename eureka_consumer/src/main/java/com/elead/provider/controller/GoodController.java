package com.elead.provider.controller;

import com.elead.provider.config.RestTemplateConfig;
import com.elead.provider.domain.Good;


import com.elead.provider.service.GoodFeignClient;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/Order")
public class GoodController {

    @Autowired private RestTemplateConfig restTemplateConfig;

    @Autowired private DiscoveryClient discoveryClient;

    @Autowired private GoodFeignClient goodFeignClient;

    @GetMapping("/find/{id}")
    public Good findById(@PathVariable("id") int id){

        /*RestTemplate restTemplate = restTemplateConfig.getRestTemplate();
        String url = "http://localhost:8000/Good/find/";
        Good good = restTemplate.getForObject(url + id, Good.class);
        return good;*/

        /*RestTemplate restTemplate = restTemplateConfig.getRestTemplate();
        List<ServiceInstance> provider = discoveryClient.getInstances("EUREKA-PROVIDER");
        if(CollectionUtils.isEmpty(provider)){
            return null;
        }

        ServiceInstance serviceInstance = provider.get(0);
        String host = serviceInstance.getHost();
        int port = serviceInstance.getPort();
        System.out.println(host);
        System.out.println(port);

        String uri = "http://"+host+":"+port+"/Good/find/"+id;
        return restTemplate.getForObject(uri, Good.class);*/

        return goodFeignClient.findGoodById(id);

    }
}
