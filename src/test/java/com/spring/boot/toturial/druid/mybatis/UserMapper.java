package com.spring.boot.toturial.druid.mybatis;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {
    int insert(User user);

    void delete(@Param("id") int id);

    void update(@Param("name") String name, @Param("id") int id);

    User select(@Param("id") int id);

    List<User> list(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize);
}