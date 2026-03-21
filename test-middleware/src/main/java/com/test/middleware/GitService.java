package com.test.middleware;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Git 服务
 * 使用 JGit 进行代码拉取和管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GitService {

    /**
     * 克隆仓库
     *
     * @param repoUrl    仓库 URL
     * @param targetDir  目标目录
     * @param branch     分支名称
     * @param credentialsProvider 凭证提供者（可选）
     * @return Git 实例
     */
    public Git cloneRepository(String repoUrl, String targetDir, String branch,
                               CredentialsProvider credentialsProvider) throws GitAPIException {
        log.info("Cloning repository: {} to {}", repoUrl, targetDir);

        try {
            // 创建目标目录
            Path targetPath = Paths.get(targetDir);
            Files.createDirectories(targetPath);

            // 配置克隆命令
            var cloneCommand = Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(targetPath.toFile())
                    .setCloneAllBranches(true);

            if (branch != null && !branch.isEmpty()) {
                cloneCommand.setBranch(branch);
            }

            if (credentialsProvider != null) {
                cloneCommand.setCredentialsProvider(credentialsProvider);
            }

            // 执行克隆
            return cloneCommand.call();

        } catch (IOException e) {
            log.error("Failed to create target directory", e);
            throw new RuntimeException("Failed to create target directory", e);
        }
    }

    /**
     * 克隆仓库（使用用户名密码认证）
     */
    public Git cloneRepository(String repoUrl, String targetDir, String branch,
                               String username, String password) throws GitAPIException {
        CredentialsProvider provider = new UsernamePasswordCredentialsProvider(username, password);
        return cloneRepository(repoUrl, targetDir, branch, provider);
    }

    /**
     * 克隆仓库（简单模式）
     */
    public Git cloneRepository(String repoUrl, String targetDir) throws GitAPIException {
        return cloneRepository(repoUrl, targetDir, null, null);
    }

    /**
     * 拉取最新代码
     */
    public void pull(String repoDir, CredentialsProvider credentialsProvider) throws GitAPIException {
        log.info("Pulling latest changes from {}", repoDir);

        try (Git git = Git.open(new File(repoDir))) {
            var pullCommand = git.pull();

            if (credentialsProvider != null) {
                pullCommand.setCredentialsProvider(credentialsProvider);
            }

            var result = pullCommand.call();
            log.info("Pull result: {}", result.getFetchResult());

        } catch (IOException e) {
            log.error("Failed to open git repository", e);
            throw new RuntimeException("Failed to open git repository", e);
        }
    }

    /**
     * 拉取最新代码（使用用户名密码认证）
     */
    public void pull(String repoDir, String username, String password) throws GitAPIException {
        CredentialsProvider provider = new UsernamePasswordCredentialsProvider(username, password);
        pull(repoDir, provider);
    }

    /**
     * 拉取最新代码（简单模式）
     */
    public void pull(String repoDir) throws GitAPIException {
        pull(repoDir, (CredentialsProvider) null);
    }

    /**
     *  checkout 分支
     */
    public void checkout(String repoDir, String branchName) throws GitAPIException {
        log.info("Checking out branch: {} in {}", branchName, repoDir);

        try (Git git = Git.open(new File(repoDir))) {
            git.checkout().setName(branchName).call();
            log.info("Successfully checked out branch: {}", branchName);

        } catch (IOException e) {
            log.error("Failed to open git repository", e);
            throw new RuntimeException("Failed to open git repository", e);
        }
    }

    /**
     * 获取当前分支
     */
    public String getCurrentBranch(String repoDir) throws IOException, GitAPIException {
        try (Git git = Git.open(new File(repoDir))) {
            return git.getRepository().getBranch();
        }
    }

    /**
     * 获取所有分支
     */
    public List<String> getBranches(String repoDir, boolean localOnly) throws GitAPIException {
        List<String> branches = new ArrayList<>();

        try (Git git = Git.open(new File(repoDir))) {
            if (localOnly) {
                List<Ref> refs = git.branchList().call();
                for (Ref ref : refs) {
                    branches.add(ref.getName().replace("refs/heads/", ""));
                }
            } else {
                List<Ref> refs = git.branchList()
                        .setListMode(org.eclipse.jgit.api.ListBranchCommand.ListMode.REMOTE)
                        .call();
                for (Ref ref : refs) {
                    branches.add(ref.getName().replace("refs/remotes/origin/", ""));
                }
            }
        } catch (IOException e) {
            log.error("Failed to open git repository", e);
            throw new RuntimeException("Failed to open git repository", e);
        }

        return branches;
    }

    /**
     * 获取最新提交 Hash
     */
    public String getLatestCommit(String repoDir) throws IOException, GitAPIException {
        try (Git git = Git.open(new File(repoDir))) {
            Ref head = git.getRepository().findRef("HEAD");
            if (head != null && head.getObjectId() != null) {
                return head.getObjectId().getName();
            }
            return null;
        }
    }

    /**
     * 创建凭证提供者
     */
    public CredentialsProvider createCredentialsProvider(String username, String password) {
        return new UsernamePasswordCredentialsProvider(username, password);
    }

    /**
     * 配置 SSH 密钥 - 简化版本
     */
    public void configureSshKey(String privateKeyPath, String passphrase) {
        log.info("SSH key configured: {}", privateKeyPath);
        // 注意：完整 SSH 支持需要 JGit sshd 依赖
    }

    /**
     * 验证仓库可访问性
     */
    public boolean validateRepository(String repoUrl, CredentialsProvider credentialsProvider) {
        try {
            // 尝试 ls-remote 验证
            var lsRemote = Git.lsRemoteRepository()
                    .setRemote(repoUrl)
                    .call();
            log.info("Repository validation successful: {} refs found", lsRemote.size());
            return true;

        } catch (Exception e) {
            log.error("Repository validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 清理仓库目录
     */
    public void cleanup(String repoDir) throws IOException {
        Path path = Paths.get(repoDir);
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            log.error("Failed to delete: {}", p, e);
                        }
                    });
            log.info("Repository directory cleaned up: {}", repoDir);
        }
    }

    /**
     * 获取仓库信息
     */
    public RepositoryInfo getRepositoryInfo(String repoDir) throws IOException, GitAPIException {
        try (Git git = Git.open(new File(repoDir))) {
            RepositoryInfo info = new RepositoryInfo();
            info.setBranch(git.getRepository().getBranch());
            info.setLatestCommit(getLatestCommit(repoDir));
            info.setLocalBranches(getBranches(repoDir, true));
            info.setRemoteBranches(getBranches(repoDir, false));
            return info;
        }
    }

    /**
     * 仓库信息
     */
    @lombok.Data
    public static class RepositoryInfo {
        private String branch;
        private String latestCommit;
        private List<String> localBranches = new ArrayList<>();
        private List<String> remoteBranches = new ArrayList<>();
    }
}
