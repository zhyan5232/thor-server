package com.thor.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thor.common.entity.ThorNodeGroup;
import com.thor.common.mapper.ThorNodeGroupMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/node-group")
public class NodeGroupController {

    @Autowired
    private ThorNodeGroupMapper nodeGroupMapper;

    @GetMapping("/list")
    public Map<String, Object> list(@RequestParam(required = false) Long appSystemId) {
        QueryWrapper<ThorNodeGroup> query = new QueryWrapper<>();
        if (appSystemId != null) {
            query.eq("app_system_id", appSystemId);
        }
        query.orderByDesc("create_time");

        List<ThorNodeGroup> list = nodeGroupMapper.selectList(query);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "success");
        result.put("result", list);
        return result;
    }

    @PostMapping
    public Map<String, Object> add(@RequestBody ThorNodeGroup nodeGroup) {
        if (nodeGroup.getAppSystemId() == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 400);
            error.put("message", "必须选择所属应用系统");
            return error;
        }

        Date now = new Date();
        nodeGroup.setCreateTime(now);
        nodeGroup.setUpdateTime(now);
        nodeGroup.setCreateBy("system");
        nodeGroup.setUpdateBy("system");

        nodeGroupMapper.insert(nodeGroup);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "新增成功");
        return result;
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody ThorNodeGroup nodeGroup) {
        nodeGroup.setId(id);
        nodeGroup.setUpdateTime(new Date());
        nodeGroup.setUpdateBy("system");

        nodeGroupMapper.updateById(nodeGroup);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "更新成功");
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        nodeGroupMapper.deleteById(id);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "删除成功");
        return result;
    }
}