package com.test.middleware;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Jenkins Webhook 控制器
 * 接收 Jenkins Pipeline 的回调通知
 */
@Slf4j
@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;

    /**
     * 接收 Jenkins Webhook
     * Jenkins Pipeline 中配置：
     * curl -X POST -H "Content-Type: application/json" \
     *      -d '{"name":"job-name", "buildNumber": 123, "status": "SUCCESS"}' \
     *      http://test-platform/api/webhook/jenkins
     */
    @PostMapping("/jenkins")
    public WebhookResponse handleJenkinsWebhook(@RequestBody JenkinsWebhookPayload payload,
                                                 @RequestHeader(value = "X-Jenkins-Token", required = false) String token) {
        log.info("Received Jenkins webhook: {}", payload);

        // 验证 token（如果配置了）
        if (!webhookService.validateToken(token)) {
            return WebhookResponse.error("Invalid token");
        }

        // 处理 webhook
        webhookService.processJenkinsWebhook(payload);

        return WebhookResponse.success("Webhook processed successfully");
    }

    /**
     * 接收 GitLab Webhook
     */
    @PostMapping("/gitlab")
    public WebhookResponse handleGitLabWebhook(@RequestBody Map<String, Object> payload,
                                                @RequestHeader(value = "X-Gitlab-Token", required = false) String token) {
        log.info("Received GitLab webhook");

        // 验证 token
        if (!webhookService.validateToken(token)) {
            return WebhookResponse.error("Invalid token");
        }

        // 解析 GitLab payload
        GitLabPayload gitLabPayload = parseGitLabPayload(payload);

        // 处理 webhook
        webhookService.processGitLabWebhook(gitLabPayload);

        return WebhookResponse.success("Webhook processed successfully");
    }

    /**
     * 接收 GitHub Webhook
     */
    @PostMapping("/github")
    public WebhookResponse handleGitHubWebhook(@RequestBody Map<String, Object> payload,
                                                @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature) {
        log.info("Received GitHub webhook");

        // 验证签名
        if (!webhookService.validateGitHubSignature(signature, payload)) {
            return WebhookResponse.error("Invalid signature");
        }

        // 解析 GitHub payload
        GitHubPayload gitHubPayload = parseGitHubPayload(payload);

        // 处理 webhook
        webhookService.processGitHubWebhook(gitHubPayload);

        return WebhookResponse.success("Webhook processed successfully");
    }

    /**
     * 通用 Webhook 接口
     */
    @PostMapping("/generic")
    public WebhookResponse handleGenericWebhook(@RequestBody GenericWebhookPayload payload) {
        log.info("Received generic webhook: {}", payload);

        // 处理 webhook
        webhookService.processGenericWebhook(payload);

        return WebhookResponse.success("Webhook processed successfully");
    }

    /**
     * 解析 GitLab payload
     */
    private GitLabPayload parseGitLabPayload(Map<String, Object> payload) {
        GitLabPayload gitLabPayload = new GitLabPayload();

        gitLabPayload.setProjectName(getString(payload, "project", "name"));
        gitLabPayload.setRef(getString(payload, "ref"));
        gitLabPayload.setBefore(getString(payload, "before"));
        gitLabPayload.setAfter(getString(payload, "after"));
        gitLabPayload.setCheckoutSha(getString(payload, "checkout_sha"));

        // 解析 committer
        Map<String, Object> committer = getMap(payload, "committer");
        if (committer != null) {
            gitLabPayload.setCommitterName(getString(committer, "name"));
            gitLabPayload.setCommitterEmail(getString(committer, "email"));
        }

        return gitLabPayload;
    }

    /**
     * 解析 GitHub payload
     */
    private GitHubPayload parseGitHubPayload(Map<String, Object> payload) {
        GitHubPayload gitHubPayload = new GitHubPayload();

        gitHubPayload.setRepository(getString(payload, "repository", "full_name"));
        gitHubPayload.setRef(getString(payload, "ref"));

        // 解析 head_commit
        Map<String, Object> headCommit = getMap(payload, "head_commit");
        if (headCommit != null) {
            gitHubPayload.setCommit(getString(headCommit, "id"));
            gitHubPayload.setCommitterName(getString(headCommit, "committer", "name"));
            gitHubPayload.setCommitterEmail(getString(headCommit, "committer", "email"));
        }

        return gitHubPayload;
    }

    private String getString(Map<String, Object> map, String... keys) {
        Object current = map;
        for (String key : keys) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(key);
            } else {
                return null;
            }
        }
        return current != null ? current.toString() : null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(Map<String, Object> map, String... keys) {
        Object current = map;
        for (String key : keys) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(key);
            } else {
                return null;
            }
        }
        return current instanceof Map ? (Map<String, Object>) current : null;
    }

    /**
     * Webhook 响应
     */
    @Data
    public static class WebhookResponse {
        private boolean success;
        private String message;
        private String timestamp;

        public static WebhookResponse success(String message) {
            WebhookResponse response = new WebhookResponse();
            response.setSuccess(true);
            response.setMessage(message);
            response.setTimestamp(java.time.LocalDateTime.now().toString());
            return response;
        }

        public static WebhookResponse error(String message) {
            WebhookResponse response = new WebhookResponse();
            response.setSuccess(false);
            response.setMessage(message);
            response.setTimestamp(java.time.LocalDateTime.now().toString());
            return response;
        }
    }

    /**
     * Jenkins Webhook Payload
     */
    @Data
    public static class JenkinsWebhookPayload {
        private String name;
        private int buildNumber;
        private String status;  // SUCCESS, FAILURE, UNSTABLE
        private String url;
        private String commitHash;
        private String branch;
    }

    /**
     * GitLab Webhook Payload
     */
    @Data
    public static class GitLabPayload {
        private String projectName;
        private String ref;
        private String before;
        private String after;
        private String checkoutSha;
        private String committerName;
        private String committerEmail;
    }

    /**
     * GitHub Webhook Payload
     */
    @Data
    public static class GitHubPayload {
        private String repository;
        private String ref;
        private String commit;
        private String committerName;
        private String committerEmail;
    }

    /**
     * 通用 Webhook Payload
     */
    @Data
    public static class GenericWebhookPayload {
        private String source;
        private String event;
        private String repository;
        private String branch;
        private String commitHash;
        private Map<String, Object> extraData;
    }
}

/**
 * Webhook 服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
class WebhookService {

    private final ConcurrentHashMap<String, WebhookHistory> webhookHistory = new ConcurrentHashMap<>();

    /**
     * 验证 token
     */
    public boolean validateToken(String token) {
        // 如果未配置 token，则跳过验证
        if (token == null || token.isEmpty()) {
            return true;
        }

        // TODO: 从配置文件读取预期的 token 进行比对
        String expectedToken = System.getenv("WEBHOOK_TOKEN");
        if (expectedToken == null || expectedToken.isEmpty()) {
            return true;  // 未配置时跳过验证
        }

        return expectedToken.equals(token);
    }

    /**
     * 验证 GitHub 签名
     */
    public boolean validateGitHubSignature(String signature, Map<String, Object> payload) {
        // 如果未配置密钥，则跳过验证
        if (signature == null || signature.isEmpty()) {
            return true;
        }

        // TODO: 实现 GitHub HMAC SHA256 签名验证
        log.debug("GitHub signature validation skipped (not implemented)");
        return true;
    }

    /**
     * 处理 Jenkins Webhook
     */
    public void processJenkinsWebhook(WebhookController.JenkinsWebhookPayload payload) {
        log.info("Processing Jenkins webhook: build={}, status={}",
                payload.getBuildNumber(), payload.getStatus());

        // 记录历史
        recordWebhook("jenkins", payload.getName(), payload.getStatus());

        // 根据状态触发不同操作
        if ("SUCCESS".equals(payload.getStatus())) {
            // 构建成功，可以触发测试或部署
            log.info("Jenkins build successful, triggering tests...");
        } else if ("FAILURE".equals(payload.getStatus())) {
            // 构建失败，发送通知
            log.warn("Jenkins build failed");
        }
    }

    /**
     * 处理 GitLab Webhook
     */
    public void processGitLabWebhook(WebhookController.GitLabPayload payload) {
        log.info("Processing GitLab webhook: project={}, ref={}",
                payload.getProjectName(), payload.getRef());

        // 记录历史
        recordWebhook("gitlab", payload.getProjectName(), "push");

        // 触发 CI/CD 流程
        log.info("GitLab push detected, triggering CI/CD pipeline...");
    }

    /**
     * 处理 GitHub Webhook
     */
    public void processGitHubWebhook(WebhookController.GitHubPayload payload) {
        log.info("Processing GitHub webhook: repo={}, ref={}",
                payload.getRepository(), payload.getRef());

        // 记录历史
        recordWebhook("github", payload.getRepository(), "push");

        // 触发 CI/CD 流程
        log.info("GitHub push detected, triggering CI/CD pipeline...");
    }

    /**
     * 处理通用 Webhook
     */
    public void processGenericWebhook(WebhookController.GenericWebhookPayload payload) {
        log.info("Processing generic webhook: source={}, event={}",
                payload.getSource(), payload.getEvent());

        // 记录历史
        recordWebhook(payload.getSource(), payload.getRepository(), payload.getEvent());
    }

    /**
     * 记录 Webhook 历史
     */
    private void recordWebhook(String source, String repository, String event) {
        String key = source + ":" + repository;
        webhookHistory.computeIfAbsent(key, k -> new WebhookHistory());

        WebhookHistory history = webhookHistory.get(key);
        history.addEvent(source, event, java.time.LocalDateTime.now());

        // 限制历史记录数量
        if (history.getEvents().size() > 100) {
            history.getEvents().remove(0);
        }
    }

    /**
     * 获取 Webhook 历史
     */
    public List<WebhookHistory> getWebhookHistory() {
        return new ArrayList<>(webhookHistory.values());
    }

    /**
     * Webhook 历史记录
     */
    @Data
    public static class WebhookHistory {
        private String repository;
        private List<WebhookEvent> events = new ArrayList<>();

        public void addEvent(String source, String event, java.time.LocalDateTime timestamp) {
            WebhookEvent webhookEvent = new WebhookEvent();
            webhookEvent.setSource(source);
            webhookEvent.setEvent(event);
            webhookEvent.setTimestamp(timestamp);
            events.add(webhookEvent);
        }
    }

    /**
     * Webhook 事件
     */
    @Data
    public static class WebhookEvent {
        private String source;
        private String event;
        private java.time.LocalDateTime timestamp;
    }
}
