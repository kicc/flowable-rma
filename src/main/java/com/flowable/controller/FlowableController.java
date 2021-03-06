package com.flowable.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.flowable.service.FlowableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
public class FlowableController {


    @Autowired
    FlowableService flowableService;

    @CrossOrigin(origins = "http://bpmn.com")
    @PostMapping(value = "/api/v1/issue/create", produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public String createIssue(@RequestBody Map<String, Object> params) throws JsonProcessingException {
        if (flowableService.startProcess(params)) {
            return "{\"result_code\":1}";
        }
        return "{\"result_code\":0}";
    }

    @CrossOrigin(origins = "http://bpmn.com")
    @GetMapping(value = "/api/v1/task/{task_id}")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public String getTask(@PathVariable String task_id) throws JsonProcessingException {
        return flowableService.getTask(task_id);
    }

    @CrossOrigin(origins = "http://bpmn.com")
    @GetMapping(value = "/api/v1/task/list/{assignee}")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public String getTasks(@PathVariable String assignee) throws JsonProcessingException {
        return flowableService.getTasksAssignee(assignee);
    }

    @CrossOrigin(origins = "http://bpmn.com")
    @GetMapping(value = "/api/v1/task/list/all/{issue_id}")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public String getAllTasks(@PathVariable String issue_id) throws JsonProcessingException {
        return flowableService.getAllTasks(issue_id);
    }

    @CrossOrigin(origins = "http://bpmn.com")
    @GetMapping(value = "/api/v1/task/list/all")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public String getAllTasks() throws JsonProcessingException {
        return flowableService.getAllTasks();
    }

    @CrossOrigin(origins = "http://bpmn.com")
    @PostMapping(value = "/api/v1/task/complete/{id}")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public String completeTask(@PathVariable String id, @RequestBody(required = false) Map<String, Object> params) throws JsonProcessingException {
        if (params != null) {
            if (flowableService.completeTask(id, params)) {
                return "{\"result_code\":1}";
            }
        } else {
            if (flowableService.completeTask(id)) {
                return "{\"result_code\":1}";
            }
        }
        return "{\"result_code\":0}";
    }

    @CrossOrigin(origins = "http://bpmn.com")
    @PostMapping(value = "/api/v1/process/deployment", produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseBody
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public String processDeployment(@RequestParam("file") MultipartFile file) throws Exception {
        flowableService.createDeployment(file);
        return file.getOriginalFilename();
    }
}
