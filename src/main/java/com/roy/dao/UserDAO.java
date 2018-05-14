package com.roy.dao;

import com.roy.domain.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Mapper
public interface UserDAO {

    @Select("select * from user where id=#{id}")
    User getById(@Param("id") int id);
    @Insert("insert into user(id,name) values(#{id},#{name})")
    int insert(User user);
}
