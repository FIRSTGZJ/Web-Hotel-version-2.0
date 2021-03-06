package com.example.hotel.controller.user;

import com.example.hotel.bl.user.AccountService;

import com.example.hotel.po.User;
import com.example.hotel.vo.UserForm;
import com.example.hotel.vo.ResponseVO;
import com.example.hotel.vo.UserInfoVO;
import com.example.hotel.vo.UserVO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController()
@RequestMapping("/api/user")
public class AccountController {
    private final static String ACCOUNT_INFO_ERROR = "用户名或密码错误";

    @Autowired
    private AccountService accountService;

    @PostMapping("/login")
    public ResponseVO login(@RequestBody UserForm userForm) {
        User user = accountService.login(userForm);
        if (user == null) {
            return ResponseVO.buildFailure(ACCOUNT_INFO_ERROR);
        }
        return ResponseVO.buildSuccess(user);
    }

    @PostMapping("/register")
    public ResponseVO registerAccount(@RequestBody UserVO userVO) {
        return accountService.registerAccount(userVO);
    }


    @GetMapping("/{id}/getUserInfo")
    public ResponseVO getUserInfo(@PathVariable int id) {
        User user = accountService.getUserInfo(id);
        if(user==null){
            return ResponseVO.buildFailure(ACCOUNT_INFO_ERROR);
        }
        return ResponseVO.buildSuccess(user);
    }

    @PostMapping("/{id}/userInfo/update")
    public ResponseVO updateInfo(@RequestBody UserInfoVO userInfoVO,@PathVariable int id){
        return accountService.updateUserInfo(id,userInfoVO.getPassword(),userInfoVO.getUserName(),userInfoVO.getPhoneNumber(),userInfoVO.getAvatarurl());
    }

    @PostMapping("/creditSet")
    public ResponseVO creditSet(@RequestBody UserVO userVO){
        return accountService.creditSet(userVO);
    }

    @PostMapping("/lvSet")
    public ResponseVO lvSet(@RequestBody UserVO userVO){
        return accountService.lvSet(userVO);
    }

    @PostMapping("/getAccountByEmail")
    public ResponseVO getAccountByEmail(@RequestBody UserVO userVO) {
        User user = accountService.getAccountByEmail(userVO.getEmail());
        if(user==null){
            return ResponseVO.buildFailure(ACCOUNT_INFO_ERROR);
        }
        return ResponseVO.buildSuccess(user);
    }

    @GetMapping("/{id}/getUserImg")
    public ResponseVO getUserImg(@PathVariable Integer id){
        return ResponseVO.buildSuccess();
    }

    @PostMapping("/{id}/updateUserImg")
    public ResponseVO updateUserImg(@RequestParam("file") MultipartFile file, @PathVariable Integer id){
        return ResponseVO.buildSuccess(accountService.updateUserImg(file,id));
    }
}
