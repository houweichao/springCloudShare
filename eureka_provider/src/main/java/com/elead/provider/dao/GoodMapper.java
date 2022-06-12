package com.elead.provider.dao;

import com.elead.provider.domain.Good;
import org.springframework.stereotype.Repository;

@Repository
public class GoodMapper {

    public Good findById(int id){
        return new Good(1,"手机",23,7876);
    }
}
