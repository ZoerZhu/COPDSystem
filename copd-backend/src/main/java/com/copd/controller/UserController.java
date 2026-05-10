package com.copd.controller;

import com.copd.common.Result;
import com.copd.dto.LoginDTO;
import com.copd.dto.RegisterDTO;
import com.copd.dto.UserInfoDTO;
import com.copd.entity.User;
import com.copd.service.UserService;
import com.copd.util.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody @Validated RegisterDTO registerDTO) {
        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);
        Map<String, Object> result = userService.register(user);
        if (Boolean.TRUE.equals(result.get("success"))) {
            return Result.success(result);
        }
        return Result.error(result.get("message").toString());
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody @Validated LoginDTO loginDTO) {
        Map<String, Object> result = userService.login(loginDTO.getUsername(), loginDTO.getPassword());
        if (Boolean.TRUE.equals(result.get("success"))) {
            return Result.success(result);
        }
        return Result.error(result.get("message").toString());
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<UserInfoDTO> getUserInfo(@RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
            User user = userService.getUserInfo(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            UserInfoDTO dto = new UserInfoDTO();
            BeanUtils.copyProperties(user, dto);
            if (user.getLastLoginTime() != null) {
                dto.setLastLoginTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(user.getLastLoginTime()));
            }
            return Result.success(dto);
        } catch (Exception e) {
            return Result.error("获取用户信息失败");
        }
    }

    /**
     * 修改个人信息
     */
    @PutMapping("/update")
    public Result<String> updateUserInfo(@RequestHeader("Authorization") String token,
                                         @RequestBody User user) {
        try {
            Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
            user.setId(userId);
            boolean success = userService.updateUserInfo(user);
            if (success) {
                return Result.success("更新成功");
            }
            return Result.error("更新失败");
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @PutMapping("/changePassword")
    public Result<String> changePassword(@RequestHeader("Authorization") String token,
                                          @RequestBody Map<String, String> params) {
        try {
            Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
            String oldPassword = params.get("oldPassword");
            String newPassword = params.get("newPassword");

            if (oldPassword == null || newPassword == null) {
                return Result.error("密码不能为空");
            }

            User user = userService.getUserInfo(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }

            String encryptedOldPassword = org.springframework.util.DigestUtils.md5DigestAsHex(oldPassword.getBytes());
            if (!encryptedOldPassword.equals(user.getPassword())) {
                return Result.error("原密码错误");
            }

            user.setPassword(newPassword);
            boolean success = userService.updateUserInfo(user);
            if (success) {
                return Result.success("密码修改成功");
            }
            return Result.error("密码修改失败");
        } catch (Exception e) {
            return Result.error("密码修改失败: " + e.getMessage());
        }
    }

    /**
     * 检查用户名是否已存在
     */
    @GetMapping("/checkUsername")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        boolean exists = userService.checkUsername(username);
        return Result.success(exists);
    }
}
