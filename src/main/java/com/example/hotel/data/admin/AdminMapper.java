package com.example.hotel.data.admin;

import com.example.hotel.po.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author: chenyizong
 * @Date: 2020-03-04
 */
@Mapper
@Repository
public interface    AdminMapper {

    int addManager(User user);
    int editUserInfo(@Param("id") int id,
                     @Param("password") String address,
                     @Param("username") String username,
                     @Param("phonenumber") String phonenumber
    );

    List<User> getAllManagers();

    List<User> getAllUsers();
    List<User> getVip();
    int deleteUser(@Param("userid")int userid);

}
