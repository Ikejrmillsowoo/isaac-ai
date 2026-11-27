package com.isaacai.tools;

import java.util.UUID;
import java.util.List;
import java.util.Map;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class WorkspaceTools {
    private final UUID workspace;
    private final TaskActions actions;
    public WorkspaceTools(UUID workspace, TaskActions actions) {
        this.workspace=workspace; this.actions=actions;
    }
    @Tool(description="Calculate two decimal numbers with +, -, * or /.")
    public String calculate(String left,String operator,String right) {
        try { return ToolCalculator.calculate(left,operator,right); }
        catch (IllegalArgumentException | ArithmeticException e) { return "Invalid calculation"; }
    }
    @Tool(description="List tasks in the current workspace, including their IDs.")
    public List<Map<String,Object>> listTasks() { return actions.tasks(workspace); }
    @Tool(description="Propose creating a task. Does not create it until the user approves in the Actions panel.")
    public String createTask(@ToolParam(description="Task title, maximum 300 characters") String title) {
        return "Pending user approval: " + actions.propose(workspace,"create_task",title);
    }
    @Tool(description="Propose marking an existing task complete. Requires user approval in the Actions panel.")
    public String completeTask(@ToolParam(description="Existing task UUID") String taskId) {
        return "Pending user approval: " + actions.propose(workspace,"complete_task",taskId);
    }
}
