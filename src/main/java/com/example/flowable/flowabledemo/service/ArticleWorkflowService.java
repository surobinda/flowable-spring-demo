package com.example.flowable.flowabledemo.service;

import com.example.flowable.flowabledemo.domain.model.Approval;
import com.example.flowable.flowabledemo.domain.model.Article;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ArticleWorkflowService {
    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Transactional
    public void startProcess(Article article) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("author", article.author());
        variables.put("url", article.url());
        runtimeService.startProcessInstanceByKey("articleReview", variables);
        taskService.createTaskQuery().list().forEach(task -> {
            System.out.println(task.getId());
            System.out.println(task.getName());
            System.out.println(task.getAssignee());
        });
    }

    @Transactional
    public List<Article> getTasks(String assignee) {
        List<Task> tasks = taskService.createTaskQuery()
                .active()
                .list();
        return tasks.stream()
                .map(task -> {
                    Map<String, Object> variables = taskService.getVariables(task.getId());
                    return new Article(task.getId(), (String) variables.get("author"), (String) variables.get("url"));
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void submitReview(Approval approval) {
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("approved", approval.status());
        taskService.complete(approval.id(), variables);
    }
}
