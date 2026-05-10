package com.copd.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.copd.annotation.RequireRole;
import com.copd.common.Result;
import com.copd.dto.PatientInfoDTO;
import com.copd.entity.PatientInfo;
import com.copd.entity.User;
import com.copd.service.UserService;
import com.copd.service.PatientInfoService;
import com.copd.service.AbeGroupChangeHistoryService;
import com.copd.util.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/patient")
public class PatientInfoController {

    @Autowired
    private PatientInfoService patientInfoService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private AbeGroupChangeHistoryService abeGroupChangeHistoryService;

    /**
     * 患者新增自己的信息
     */
    @PostMapping("/add")
    @RequireRole("PATIENT")
    public Result<String> addPatientInfo(@RequestBody PatientInfoDTO patientInfoDTO,
                                          @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));

        PatientInfo existing = patientInfoService.getMyPatientInfo(userId);
        if (existing != null) {
            return Result.error("您已填写过患者信息，不能重复添加");
        }

        PatientInfo patientInfo = new PatientInfo();
        BeanUtils.copyProperties(patientInfoDTO, patientInfo);

        boolean success = patientInfoService.addPatientInfo(patientInfo, userId);
        if (success) {
            return Result.success("添加成功");
        }
        return Result.error("添加失败");
    }

    /**
     * 患者获取自己的信息
     */
    @GetMapping("/my")
    @RequireRole("PATIENT")
    public Result<PatientInfoDTO> getMyPatientInfo(@RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));

            PatientInfo patientInfo = patientInfoService.getMyPatientInfo(userId);
            if (patientInfo == null) {
                return Result.error("未找到患者信息");
            }

            PatientInfoDTO dto = new PatientInfoDTO();
            BeanUtils.copyProperties(patientInfo, dto);
            return Result.success(dto);
        } catch (Exception e) {
            return Result.error("获取患者信息失败: " + e.getMessage());
        }
    }

    /**
     * 管理员获取患者列表（分页）
     */
    @GetMapping("/list")
    @RequireRole(admin = true)
    public Result<Map<String, Object>> getPatientList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String goldGroup) {
        IPage<PatientInfo> page = patientInfoService.getPatientList(pageNum, pageSize, name, goldGroup);

        Map<String, Object> result = new HashMap<>();
        result.put("records", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pages", page.getPages());
        result.put("current", page.getCurrent());
        result.put("size", page.getSize());
        return Result.success(result);
    }

    /**
     * 管理员根据ID获取患者信息
     */
    @GetMapping("/{id}")
    @RequireRole(admin = true)
    public Result<PatientInfoDTO> getPatientById(@PathVariable Long id) {
        PatientInfo patientInfo = patientInfoService.getPatientById(id);
        if (patientInfo == null) {
            return Result.error("未找到患者信息");
        }

        PatientInfoDTO dto = new PatientInfoDTO();
        BeanUtils.copyProperties(patientInfo, dto);
        return Result.success(dto);
    }

    /**
     * 更新患者信息
     */
    @PutMapping("/update")
    @RequireRole(value = {"PATIENT", "ADMIN"})
    public Result<String> updatePatientInfo(@RequestBody PatientInfoDTO patientInfoDTO,
                                            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        String role = jwtUtil.getRoleFromToken(token.replace("Bearer ", ""));

        PatientInfo patientInfo = new PatientInfo();
        BeanUtils.copyProperties(patientInfoDTO, patientInfo);

        if ("ADMIN".equals(role)) {
            if (patientInfoDTO.getId() != null) {
                patientInfoService.updateById(patientInfo);
                // 管理员更新患者基础信息后，需要记录 ABE 分组变化
                abeGroupChangeHistoryService.recordIfGroupChanged(patientInfoDTO.getId());
                return Result.success("管理员更新成功");
            }
        }

        PatientInfo myInfo = patientInfoService.getMyPatientInfo(userId);
        if (myInfo == null) {
            return Result.error("未找到患者信息");
        }

        patientInfo.setId(myInfo.getId());
        boolean success = patientInfoService.updatePatientInfo(patientInfo);
        if (success) {
            // 患者更新自身基础信息后，需要记录 ABE 分组变化
            abeGroupChangeHistoryService.recordIfGroupChanged(myInfo.getId());
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    /**
     * 管理员删除患者信息
     */
    @DeleteMapping("/{id}")
    @RequireRole(admin = true)
    public Result<String> deletePatient(@PathVariable Long id) {
        boolean success = patientInfoService.deletePatient(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 管理员新增患者信息并创建登录账户
     */
    @PostMapping("/admin/add")
    @RequireRole(admin = true)
    public Result<String> adminAddPatient(@RequestBody PatientInfoDTO patientInfoDTO) {
        if (patientInfoDTO == null || patientInfoDTO.getName() == null || patientInfoDTO.getName().trim().isEmpty()) {
            return Result.error("患者姓名不能为空");
        }

        // 创建登录账户：用户名默认为姓名，密码默认 123456，角色为 PATIENT
        User user = new User();
        user.setUsername(patientInfoDTO.getName().trim());
        user.setRealName(patientInfoDTO.getName().trim());
        user.setPassword("123456");
        user.setRole("PATIENT");

        var registerResult = userService.register(user);
        if (!Boolean.TRUE.equals(registerResult.get("success"))) {
            Object message = registerResult.get("message");
            return Result.error(message != null ? message.toString() : "创建登录账户失败");
        }

        Object userIdObj = registerResult.get("userId");
        if (!(userIdObj instanceof Long) && !(userIdObj instanceof Integer)) {
            return Result.error("创建登录账户失败：用户ID异常");
        }
        Long userId = userIdObj instanceof Long ? (Long) userIdObj : ((Integer) userIdObj).longValue();

        PatientInfo patientInfo = new PatientInfo();
        BeanUtils.copyProperties(patientInfoDTO, patientInfo);
        patientInfo.setUserId(userId);

        boolean saved = patientInfoService.save(patientInfo);
        if (saved) {
            return Result.success("新增患者并创建账户成功（默认密码：123456）");
        }

        // 若患者信息保存失败，回滚已创建的用户账户
        userService.removeById(userId);
        return Result.error("保存患者信息失败，已回滚登录账户创建");
    }

    /**
     * 获取患者统计信息
     */
    @GetMapping("/statistics")
    @RequireRole(admin = true)
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalPatients", patientInfoService.getPatientCount());

        LambdaQueryWrapper<PatientInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(PatientInfo::getGoldGroup);
        List<PatientInfo> allPatients = patientInfoService.list(wrapper);

        long groupA = allPatients.stream().filter(p -> "A".equals(p.getGoldGroup())).count();
        long groupB = allPatients.stream().filter(p -> "B".equals(p.getGoldGroup())).count();
        long groupE = allPatients.stream().filter(p -> "E".equals(p.getGoldGroup())).count();

        Map<String, Long> goldGroupCount = new HashMap<>();
        goldGroupCount.put("A", groupA);
        goldGroupCount.put("B", groupB);
        goldGroupCount.put("E", groupE);

        statistics.put("goldGroupDistribution", goldGroupCount);
        return Result.success(statistics);
    }
}
