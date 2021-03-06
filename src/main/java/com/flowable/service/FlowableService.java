package com.flowable.service;

import com.google.gson.Gson;
import org.flowable.engine.*;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class FlowableService {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    HistoryService historyService;

    @Autowired
    ProcessEngine processEngine;

    public FlowableService() {
//        processEngine = ProcessEngines.getDefaultProcessEngine();
//        repositoryService = processEngine.getRepositoryService();
//        System.out.println("Number of process definitions : " + repositoryService.createProcessDefinitionQuery().count());
//        System.out.println("Number of tasks : " + taskService.createTaskQuery().count());
//        System.out.println("Number of tasks after process start: " + taskService.createTaskQuery().count());
    }

    public boolean startProcess(Map<String, Object> params) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("barcode", params.get("barcode"));
        variables.put("order_id", params.get("order_id"));
        variables.put("apartment_id", params.get("apartment_id"));
        variables.put("product_id", params.get("product_id"));
        variables.put("drawer_id", params.get("drawer_id"));
        variables.put("accessories_id", params.get("accessories_id"));
        variables.put("issue_type", params.get("issue_type"));
        variables.put("issue_type_description", params.get("issue_type_description"));
        variables.put("issue_level", params.get("issue_level"));
        variables.put("issue_level_description", params.get("issue_level_description"));
        System.out.println("Start process: " + variables);
        runtimeService.startProcessInstanceByKey("autoProcess", variables);
        return true;
    }

    public HistoricTaskInstance findPreviousTask(String processInstanceId) {
        return historyService.createHistoricTaskInstanceQuery().
                processInstanceId(processInstanceId).orderByHistoricTaskInstanceEndTime().desc().list().get(0);
    }

    private Map<String, Object> getTaskVariables(Task task) {
        Map<String, Object> variables = new HashMap<>();
        HistoricTaskInstance previousTask = findPreviousTask(task.getProcessInstanceId());
        Map<String, Object> processVariables = taskService.getVariables(task.getId());
        processVariables.remove("task_id");
        variables.put("task_id", task.getId());
        variables.put("task_name", task.getName());
        variables.put("task_previous_id", previousTask.getId());
        variables.put("task_previous_name", previousTask.getName());
        variables.put("task_description", task.getDescription());
        variables.put("assignee", task.getAssignee());
        variables.put("parent_task_id", task.getParentTaskId());
        variables.put("process_definition_id", task.getProcessDefinitionId());
        variables.put("process_instance_id", task.getProcessInstanceId());
        variables.putAll(processVariables);
        return variables;
    }

    private List<Map<String, Object>> getTasksVariables(List<Task> tasks) {
        List<Map<String, Object>> listTaskVariables = new ArrayList<>();
        for (Task task : tasks
                ) {
            listTaskVariables.add(getTaskVariables(task));
        }
        System.out.println("Get tasks: " + listTaskVariables);
        return listTaskVariables;
    }

    private List<Map<String, Object>> getTasksVariablesByProcess(List<ProcessInstance> processInstances) {
        List<Map<String, Object>> listTaskVariables = new ArrayList<>();
        for (ProcessInstance processInstance : processInstances
                ) {
            List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).list();
            listTaskVariables.addAll(getTasksVariables(tasks));
        }
        System.out.println("Get tasks: " + listTaskVariables);
        return listTaskVariables;
    }

    public String getTask(String task_id) {
        Task task = taskService.createTaskQuery().taskId(task_id).singleResult();
        return new Gson().toJson(getTaskVariables(task));
    }

    public String getTasksAssignee(String assignee) {
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(assignee).list();
        return new Gson().toJson(getTasksVariables(tasks));
    }

    public String getAllTasks(String issueId) {
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(issueId).list();
        return new Gson().toJson(getTasksVariables(tasks));
    }

    public boolean completeTask(String taskId, Map<String, Object> params) {
        if (params.size() == 0) {
            taskService.complete(taskId);
        } else {
            taskService.complete(taskId, params);
        }
        return true;
    }

    public boolean completeTask(String taskId) {
        taskService.complete(taskId);
        return true;
    }

    public String getAllTasks() {
        List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
        return new Gson().toJson(getTasksVariablesByProcess(processInstances));
    }

    public void createDeployment(MultipartFile file) throws Exception {
        repositoryService.createDeployment().addInputStream(file.getOriginalFilename(), file.getInputStream()).deploy();
    }
}
