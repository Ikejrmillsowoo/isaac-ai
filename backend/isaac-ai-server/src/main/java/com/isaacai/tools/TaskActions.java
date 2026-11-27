package com.isaacai.tools;

import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class TaskActions {
    private final JdbcTemplate jdbc;
    public TaskActions(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Map<String,Object>> tasks(UUID workspace) {
        return jdbc.queryForList("SELECT id,title,completed,created_at FROM assistant_tasks WHERE workspace_id=? ORDER BY created_at DESC", workspace);
    }

    public List<Map<String,Object>> history(UUID workspace) {
        return jdbc.queryForList("SELECT id,tool,argument,status,result,created_at FROM tool_actions WHERE workspace_id=? ORDER BY created_at DESC LIMIT 100", workspace);
    }

    public UUID propose(UUID workspace, String tool, String argument) {
        if (argument == null || argument.isBlank() || argument.length() > 300)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid argument");
        if (!Set.of("create_task","complete_task").contains(tool))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown action");
        if ("complete_task".equals(tool)) {
            UUID task = parseId(argument);
            if (!Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM assistant_tasks WHERE workspace_id=? AND id=?)", Boolean.class, workspace, task)))
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found");
        }
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO tool_actions(id,workspace_id,tool,argument,status) VALUES (?,?,?,?,'PENDING')", id, workspace, tool, argument.trim());
        return id;
    }

    @Transactional
    public String decide(UUID workspace, UUID id, boolean approve) {
        var rows = jdbc.queryForList("SELECT tool,argument,status FROM tool_actions WHERE workspace_id=? AND id=? FOR UPDATE", workspace,id);
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Action not found");
        var row = rows.getFirst();
        if (!"PENDING".equals(row.get("status"))) throw new ResponseStatusException(HttpStatus.CONFLICT,"Action already decided");
        String result = "Rejected";
        if (approve) {
            String argument = (String) row.get("argument");
            switch ((String) row.get("tool")) {
                case "create_task" -> {
                    UUID task = UUID.randomUUID();
                    jdbc.update("INSERT INTO assistant_tasks(id,workspace_id,title) VALUES (?,?,?)",task,workspace,argument);
                    result = "Created task " + task;
                }
                case "complete_task" -> {
                    if (jdbc.update("UPDATE assistant_tasks SET completed=true WHERE workspace_id=? AND id=?",workspace,parseId(argument)) != 1)
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Task not found");
                    result = "Completed task " + argument;
                }
                default -> throw new IllegalStateException("Unknown stored action");
            }
        }
        jdbc.update("UPDATE tool_actions SET status=?,result=? WHERE workspace_id=? AND id=?",approve?"EXECUTED":"REJECTED",result,workspace,id);
        return result;
    }

    private UUID parseId(String value) {
        try { return UUID.fromString(value.trim()); }
        catch (IllegalArgumentException e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid task ID"); }
    }
}
