package com.thor.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thor.common.entity.ThorAppSystem;
import com.thor.common.mapper.ThorAppSystemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
     * 对接前端 AppSystemManage.vue
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

    @PostMapping
    public Map<String, Object> add(@RequestBody ThorAppSystem appSystem) {
        appSystemMapper.insert(appSystem);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "新增成功");
        return result;
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody ThorAppSystem appSystem) {
        appSystem.setId(id);
        appSystemMapper.updateById(appSystem);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "更新成功");
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        appSystemMapper.deleteById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "删除成功");
        return result;
    }
}