package com.elead.provider.service;


import com.elead.provider.domain.Good;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(value = "EUREKA-PROVIDER"/*,fallback = GoodFeignClientImpl.class*/)
public interface GoodFeignClient {

    @GetMapping("/Good/find/{id}")
    public Good findGoodById(@PathVariable("id") int id);
}
