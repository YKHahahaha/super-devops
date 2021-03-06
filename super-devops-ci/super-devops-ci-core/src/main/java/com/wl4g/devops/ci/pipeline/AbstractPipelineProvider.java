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
package com.wl4g.devops.ci.pipeline;

import com.wl4g.devops.ci.config.CiCdProperties;
import com.wl4g.devops.ci.core.PipelineJobExecutor;
import com.wl4g.devops.ci.core.context.PipelineContext;
import com.wl4g.devops.ci.service.DependencyService;
import com.wl4g.devops.ci.service.TaskHistoryService;
import com.wl4g.devops.common.bean.share.AppInstance;
import com.wl4g.devops.common.exception.ci.InvalidCommandScriptException;
import com.wl4g.devops.common.utils.cli.SSH2Utils.CommandResult;
import com.wl4g.devops.common.utils.codec.AES;
import com.wl4g.devops.dao.ci.ProjectDao;
import com.wl4g.devops.dao.ci.TaskHisBuildCommandDao;
import com.wl4g.devops.dao.ci.TaskSignDao;
import com.wl4g.devops.support.concurrent.locks.JedisLockManager;
import com.wl4g.devops.support.cli.ProcessManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static com.wl4g.devops.ci.utils.LogHolder.logDefault;
import static com.wl4g.devops.common.utils.cli.SSH2Utils.executeWithCommand;
import static com.wl4g.devops.common.utils.lang.Collections2.safeList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.containsAny;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.replace;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notNull;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.io.File;
import java.util.List;

/**
 * Abstract based deploy provider.
 *
 * @author Wangl.sir <983708408@qq.com>
 * @author vjay
 * @date 2019-05-05 17:17:00
 */
public abstract class AbstractPipelineProvider implements PipelineProvider {

	final protected Logger log = LoggerFactory.getLogger(getClass());

	/** Pipeline context wrapper. */
	final protected PipelineContext context;

	@Autowired
	protected CiCdProperties config;
	@Autowired
	protected PipelineJobExecutor jobExecutor;
	@Autowired
	protected BeanFactory beanFactory;
	@Autowired
	protected JedisLockManager lockManager;
	@Autowired
	protected ProcessManager processManager;

	@Autowired
	protected DependencyService dependencyService;
	@Autowired
	protected TaskHistoryService taskHistoryService;
	@Autowired
	protected TaskHisBuildCommandDao taskHisBuildCommandDao;
	@Autowired
	protected ProjectDao projectDao;
	@Autowired
	protected TaskSignDao taskSignDao;

	/**
	 * Pull project source from VCS files fingerprint.
	 */
	private String sourceFingerprint;

	/**
	 * Build project assets files fingerprint.
	 */
	private String assetsFingerprint;

	public AbstractPipelineProvider(PipelineContext context) {
		notNull(context, "Pipeline context must not be null.");
		this.context = context;
	}

	/**
	 * Pipeline context.
	 */
	public PipelineContext getContext() {
		return context;
	}

	// --- Fingerprints. ---

	/**
	 * Get pull project source from VCS files fingerprint.
	 */
	@Override
	public String getSourceFingerprint() {
		return sourceFingerprint;
	}

	/**
	 * Get build project assets files fingerprint.
	 */
	@Override
	public String getAssetsFingerprint() {
		return assetsFingerprint;
	}

	/**
	 * Setup pull project source from VCS files fingerprint.
	 * 
	 * @param sourceFingerprint
	 */
	protected void setupSourceFingerprint(String sourceFingerprint) {
		hasText(sourceFingerprint, "sourceFingerprint must not be empty.");
		this.sourceFingerprint = sourceFingerprint;
	}

	/**
	 * Setup build project assets files fingerprint.
	 * 
	 * @param assetsFingerprint
	 */
	protected void setupAssetsFingerprint(String assetsFingerprint) {
		hasText(assetsFingerprint, "assetsFingerprint must not be empty.");
		this.assetsFingerprint = assetsFingerprint;
	}

	// --- Functions. ---

	/**
	 * Execution remote commands
	 * 
	 * @param remoteHost
	 * @param user
	 * @param command
	 * @param sshkey
	 * @return
	 * @throws Exception
	 */
	@Override
	public void doRemoteCommand(String remoteHost, String user, String command, String sshkey) throws Exception {
		hasText(command, "Commands must not be empty.");

		// Remote timeout(Ms)
		long timeoutMs = config.getRemoteCommandTimeoutMs(getContext().getInstances().size());
		logDefault("Transfer remote execution for %s@%s, timeout(%s) => command(%s)", user, remoteHost, timeoutMs, command);
		// Execution command.
		CommandResult result = executeWithCommand(remoteHost, user, getUsableCipherSshKey(sshkey), command, timeoutMs);

		logDefault("\n%s@%s -> [stdout]\n", user, remoteHost);
		if (!isBlank(result.getMessage())) {
			logDefault(result.getMessage());
		}
		logDefault("\n%s@%s -> [stderr]\n", user, remoteHost);
		if (!isBlank(result.getErrmsg())) {
			logDefault(result.getErrmsg());
		}

	}

	/**
	 * Deciphering usable cipher SSH2 key.
	 * 
	 * @param sshkey
	 * @return
	 * @throws Exception
	 */
	@Override
	public char[] getUsableCipherSshKey(String sshkey) throws Exception {
		// Obtain text-plain privateKey(RSA)
		String cipherKey = config.getDeploy().getCipherKey();
		char[] sshkeyPlain = new AES(cipherKey).decrypt(sshkey).toCharArray();
		if (log.isInfoEnabled()) {
			log.info("Transfer plain sshkey: {} => {}", cipherKey, "******");
		}

		logDefault("Transfer plain sshkey: %s => %s", cipherKey, "******");
		return sshkeyPlain;
	}

	/**
	 * Execution distribution transfer to remote instances for executable file.
	 */
	protected void doExecuteTransferToRemoteInstances() {
		// Creating transfer instances jobs.
		List<Runnable> jobs = safeList(getContext().getInstances()).stream().map(i -> newDeployer(i)).collect(toList());

		// Submit jobs for complete.
		if (!isEmpty(jobs)) {
			if (log.isInfoEnabled()) {
				log.info("Transfer jobs starting...  for instances({}), {}", jobs.size(), getContext().getInstances());
			}
			jobExecutor.submitForComplete(jobs, config.getDeploy().getTransferTimeoutMs());
		}

	}

	/**
	 * Create pipeline transfer job.
	 * 
	 * @param instance
	 * @return
	 */
	protected abstract Runnable newDeployer(AppInstance instance);

	/**
	 * Resolve commands placeholder variables.
	 * 
	 * @param commands
	 * @return
	 */
	protected String resolveCmdPlaceholderVariables(String commands) {
		return new PlaceholderVariableResolver(commands).resolve().get();
	}

	/**
	 * Placeholder variables resolver.
	 * 
	 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
	 * @version v1.0 2019年11月5日
	 * @since
	 */
	class PlaceholderVariableResolver {

		/** Placeholder for projectDir. */
		final public static String PH_PROJECT_DIR = "{pipe.projectDir}";

		/** Placeholder for workspaceDir. */
		final public static String PH_WORKSPACE_DIR = "{pipe.workspaceDir}";

		/** Placeholder for temporary scripts file. */
		final public static String PH_TMP_SCRIPT_FILE = "{pipe.tmpScriptFile}";

		/** Placeholder for backupDir. */
		final public static String PH_BACKUP_DIR = "{pipe.backupDir}";

		/** Placeholder for logPath. */
		final public static String PH_LOG_FILE = "{pipe.logFile}";

		/** Placeholder for remoteTmpDir. */
		final public static String PH_REMOTE_TMP_DIR = "{pipe.remoteTmpDir}";

		/** Resolving commands. */
		private String commands;

		public PlaceholderVariableResolver(String commands) {
			hasText(commands, "Resolving pipeline commands must not be empty.");
			this.commands = commands;
		}

		/**
		 * Resolving placeholder variables.
		 * 
		 * @param context
		 * @param command
		 * @return
		 */
		PlaceholderVariableResolver resolve() {
			// Replace for workspace.
			commands = replace(commands, PH_WORKSPACE_DIR, config.getWorkspace());

			// Replace for projectDir.
			String projectDir = config.getProjectSourceDir(getContext().getProject().getProjectName()).getAbsolutePath();
			commands = replace(commands, PH_PROJECT_DIR, projectDir);

			// Replace for backupDir.
			File tmpScriptFile = config.getJobTmpCommandFile(getContext().getTaskHistory().getId(),
					getContext().getProject().getId());
			commands = replace(commands, PH_TMP_SCRIPT_FILE, tmpScriptFile.getAbsolutePath());

			// Replace for backupDir.
			File backupDir = config.getJobBackup(getContext().getTaskHistory().getId());
			commands = replace(commands, PH_BACKUP_DIR, backupDir.getAbsolutePath());

			// Replace for logPath.
			File logFile = config.getJobLog(getContext().getTaskHistory().getId());
			commands = replace(commands, PH_LOG_FILE, logFile.getAbsolutePath());

			// Replace for remoteTmpDir.
			String remoteTmpDir = config.getDeploy().getRemoteHomeTmpDir();
			commands = replace(commands, PH_REMOTE_TMP_DIR, remoteTmpDir);

			return this;
		}

		/**
		 * Get safety asserted commands.
		 * 
		 * @return
		 */
		String get() {
			// Invalid placeholder ?
			if (containsAny(commands, "{", "}")) {
				if (contains(commands, "{") && contains(commands, "}")) {
					String invalidVar = commands.substring(commands.indexOf("{"), commands.indexOf("}") + 1);
					throw new InvalidCommandScriptException(String.format(
							"Invalid placeholder '%s' in commands script. See:https://github.com/wl4g/super-devops/blob/master/super-devops-ci/README.md",
							invalidVar));
				} else {
					throw new InvalidCommandScriptException(String.format("Invalid commands script for: %s", commands));
				}
			}

			return commands;
		}

	}

	public static void main(String[] args) {
		String s = "aabb111b {pipe.remoteTmpDir} \n echo 1111 \n start...";
		System.out.println(s);
		if (containsAny(s, "{", "}")) {
			System.out.println(s.substring(s.indexOf("{"), s.indexOf("}") + 1));
		}
		System.out.println();
	}

}