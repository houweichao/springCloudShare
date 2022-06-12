package com.elead.provider.service;

import com.elead.provider.dao.GoodMapper;
import com.elead.provider.domain.Good;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GoodService {

    @Autowired
    GoodMapper goodMapper;

    public Good findById(int id){
        return goodMapper.findById(id);
    }
}
