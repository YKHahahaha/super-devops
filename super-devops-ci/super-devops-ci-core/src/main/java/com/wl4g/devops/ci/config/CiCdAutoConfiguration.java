/*
 * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wl4g.devops.ci.config;

import com.wl4g.devops.ci.console.CiCdConsole;
import com.wl4g.devops.ci.core.DefaultPipelineManager;
import com.wl4g.devops.ci.core.PipelineManager;
import com.wl4g.devops.ci.core.context.PipelineContext;
import com.wl4g.devops.ci.core.PipelineJobExecutor;
import com.wl4g.devops.ci.pipeline.*;
import com.wl4g.devops.ci.pipeline.deploy.DjangoStandardPipeDeployer;
import com.wl4g.devops.ci.pipeline.deploy.DockerNativePipeDeployer;
import com.wl4g.devops.ci.pipeline.deploy.GolangPipeDeployer;
import com.wl4g.devops.ci.pipeline.deploy.MvnAssembleTarPipeDeployer;
import com.wl4g.devops.ci.pipeline.deploy.NpmViewPipeDeployer;
import com.wl4g.devops.ci.pipeline.deploy.SpringExecutableJarPipeDeployer;
import com.wl4g.devops.ci.pipeline.timing.TimingPipelineManager;
import com.wl4g.devops.ci.vcs.CompositeVcsOperateAdapter;
import com.wl4g.devops.ci.vcs.VcsOperator;
import com.wl4g.devops.ci.vcs.alicode.AlicodeVcsOperator;
import com.wl4g.devops.ci.vcs.bitbucket.BitbucketVcsOperator;
import com.wl4g.devops.ci.vcs.coding.CodingVcsOperator;
import com.wl4g.devops.ci.vcs.gitee.GiteeVcsOperator;
import com.wl4g.devops.ci.vcs.github.GithubVcsOperator;
import com.wl4g.devops.ci.vcs.gitlab.GitlabV4VcsOperator;
import com.wl4g.devops.ci.pipeline.timing.TimingPipelineJob;
import com.wl4g.devops.common.bean.ci.Project;
import com.wl4g.devops.common.bean.ci.Task;
import com.wl4g.devops.common.bean.ci.TaskDetail;
import com.wl4g.devops.common.bean.ci.TaskHistoryDetail;
import com.wl4g.devops.common.bean.ci.Trigger;
import com.wl4g.devops.common.bean.share.AppInstance;
import com.wl4g.devops.support.beans.prototype.DelegateAlias;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.List;

/**
 * CICD auto configuration.
 *
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0 2019年5月21日
 * @since
 */
@Configuration
public class CiCdAutoConfiguration {

	// --- BASIC ---

	@Bean
	@ConfigurationProperties(prefix = "pipeline")
	public CiCdProperties ciCdProperties() {
		return new CiCdProperties();
	}

	@Bean
	public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
		return new ThreadPoolTaskScheduler();
	}

	@Bean
	public PipelineJobExecutor pipelineJobExecutor(CiCdProperties config) {
		return new PipelineJobExecutor(config);
	}

	@Bean
	public PipelineManager defualtPipelineManager() {
		return new DefaultPipelineManager();
	}

	@Bean
	public GlobalTimeoutJobCleanupFinalizer globalTimeoutJobCleanFinalizer() {
		return new GlobalTimeoutJobCleanupFinalizer();
	}

	// --- CONSOLE ---

	@Bean
	public CiCdConsole cicdConsole() {
		return new CiCdConsole();
	}

	// --- VCS ---

	@Bean
	public VcsOperator gitlabV4VcsOperator() {
		return new GitlabV4VcsOperator();
	}

	@Bean
	public VcsOperator githubV4VcsOperator() {
		return new GithubVcsOperator();
	}

	@Bean
	public VcsOperator bitbucketVcsOperator() {
		return new BitbucketVcsOperator();
	}

	@Bean
	public VcsOperator codingVcsOperator() {
		return new CodingVcsOperator();
	}

	@Bean
	public VcsOperator giteeVcsOperator() {
		return new GiteeVcsOperator();
	}

	@Bean
	public VcsOperator alicodeVcsOperator() {
		return new AlicodeVcsOperator();
	}

	@Bean
	public CompositeVcsOperateAdapter compositeVcsOperateAdapter(List<VcsOperator> operators) {
		return new CompositeVcsOperateAdapter(operators);
	}

	// --- Pipeline provider's. ---

	@Bean
	@DelegateAlias({ PipelineType.MVN_ASSEMBLE_TAR })
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public MvnAssembleTarPipelineProvider mvnAssembleTarPipelineProvider(PipelineContext info) {
		return new MvnAssembleTarPipelineProvider(info);
	}

	@Bean
	@DelegateAlias({ PipelineType.SPRING_EXECUTABLE_JAR })
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SpringExecutableJarPipelineProvider springExecutableJarPipelineProvider(PipelineContext info) {
		return new SpringExecutableJarPipelineProvider(info);
	}

	@Bean
	@DelegateAlias({ PipelineType.DOCKER_NATIVE })
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public DockerNativePipelineProvider dockerNativePipelineProvider(PipelineContext info) {
		return new DockerNativePipelineProvider(info);
	}

	@Bean
	@DelegateAlias({ PipelineType.NPM_VIEW })
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public NpmViewPipelineProvider npmViewPipelineProvider(PipelineContext info) {
		return new NpmViewPipelineProvider(info);
	}

	@Bean
	@DelegateAlias({ PipelineType.DJANGO_STANDARD })
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public DjangoStandardPipelineProvider djangoStandardPipelineProvider(PipelineContext info) {
		return new DjangoStandardPipelineProvider(info);
	}

	@Bean
	@DelegateAlias({ PipelineType.GOLANG_STANDARD })
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public GolangPipelineProvider golangPipelineProvider(PipelineContext info) {
		return new GolangPipelineProvider(info);
	}

	// --- Pipeline deployer's. ---

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public MvnAssembleTarPipeDeployer mvnAssembleTarPipeDeployer(MvnAssembleTarPipelineProvider provider, AppInstance instance,
			List<TaskHistoryDetail> taskHistoryDetails) {
		return new MvnAssembleTarPipeDeployer(provider, instance, taskHistoryDetails);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public DockerNativePipeDeployer dockerNativePipeDeployer(DockerNativePipelineProvider provider, AppInstance instance,
			List<TaskHistoryDetail> taskHistoryDetails) {
		return new DockerNativePipeDeployer(provider, instance, taskHistoryDetails);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public NpmViewPipeDeployer npmViewPipeDeployer(NpmViewPipelineProvider provider, AppInstance instance,
			List<TaskHistoryDetail> taskHistoryDetails) {
		return new NpmViewPipeDeployer(provider, instance, taskHistoryDetails);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SpringExecutableJarPipeDeployer springExecutableJarPipeDeployer(SpringExecutableJarPipelineProvider provider,
			AppInstance instance, List<TaskHistoryDetail> taskHistoryDetails) {
		return new SpringExecutableJarPipeDeployer(provider, instance, taskHistoryDetails);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public DjangoStandardPipeDeployer djangoStandardPipeDeployer(DjangoStandardPipelineProvider provider, AppInstance instance,
			List<TaskHistoryDetail> taskHistoryDetails) {
		return new DjangoStandardPipeDeployer(provider, instance, taskHistoryDetails);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public GolangPipeDeployer golangPipeDeployer(DjangoStandardPipelineProvider provider, AppInstance instance,
			List<TaskHistoryDetail> taskHistoryDetails) {
		return new GolangPipeDeployer(provider, instance, taskHistoryDetails);
	}

	// --- TIMING SCHEDULE ---

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public TimingPipelineJob timingPipelineJob(Trigger trigger, Project project, Task task, List<TaskDetail> taskDetails) {
		return new TimingPipelineJob(trigger, project, task, taskDetails);
	}

	@Bean
	public TimingPipelineManager timingPipelineManager() {
		return new TimingPipelineManager();
	}

}