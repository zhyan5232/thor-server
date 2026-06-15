package com.thor.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thor.common.entity.ThorAppSystem;
import com.thor.common.mapper.ThorAppSystemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/app-system")
public class AppSystemController {

    @Autowired
    private ThorAppSystemMapper appSystemMapper;

    /**
     * 获取应用系统列表
     */
    @GetMapping("/list")
    public Map<String, Object> list() {
        List<ThorAppSystem> list = appSystemMapper.selectList(new QueryWrapper<ThorAppSystem>().orderByDesc("create_time"));

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "success");
        result.put("result", list);
        return result;
    }

    /**
     * 新增应用系统
     */
    @PostMapping
    public Map<String, Object> add(@RequestBody ThorAppSystem appSystem) {
        Date now = new Date();
        appSystem.setCreateTime(now);
        appSystem.setUpdateTime(now);
        appSystem.setCreateBy("system");   // 暂时默认值，后续改为登录用户
        appSystem.setUpdateBy("system");

        appSystemMapper.insert(appSystem);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "新增成功");
        return result;
    }

    /**
     * 修改应用系统
     */
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody ThorAppSystem appSystem) {
        appSystem.setId(id);
        appSystem.setUpdateTime(new Date());
        appSystem.setUpdateBy("system");   // 暂时默认值

        appSystemMapper.updateById(appSystem);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "更新成功");
        return result;
    }

    /**
     * 删除应用系统
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        appSystemMapper.deleteById(id);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "删除成功");
        return result;
    }
}