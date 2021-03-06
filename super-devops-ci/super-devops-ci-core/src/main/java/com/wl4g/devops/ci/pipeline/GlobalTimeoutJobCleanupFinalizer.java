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
import com.wl4g.devops.dao.ci.TaskHistoryDao;
import com.wl4g.devops.support.cache.JedisService;
import com.wl4g.devops.support.concurrent.locks.JedisLockManager;
import com.wl4g.devops.support.task.GenericTaskRunner;
import com.wl4g.devops.support.task.RunnerProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import static com.wl4g.devops.common.constants.CiDevOpsConstants.KEY_FINALIZER_INTERVALMS;
import static com.wl4g.devops.common.utils.concurrent.ThreadUtils.sleepRandom;
import static java.util.Objects.nonNull;

import java.util.concurrent.locks.Lock;

/**
 * Global timeout job handler finalizer.
 * 
 * @author Wangl.sir &lt;Wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0.0 2019-10-15
 * @since
 */
public class GlobalTimeoutJobCleanupFinalizer extends GenericTaskRunner<RunnerProperties> {
	final public static long DEFAULT_MIN_WATCH_MS = 2_000L;

	final protected Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	protected ThreadPoolTaskScheduler scheduler;
	@Autowired
	protected ConfigurableEnvironment environment;
	@Autowired
	protected CiCdProperties config;
	@Autowired
	protected JedisLockManager lockManager;
	@Autowired
	protected JedisService jedisService;

	@Autowired
	protected TaskHistoryDao taskHistoryDao;

	public GlobalTimeoutJobCleanupFinalizer() {
		super(new RunnerProperties(true));
	}

	@Override
	public void run() {
		if (isActive()) {
			try {
				loopWatchTimeoutCleanupFinalizer(getCleanupFinalizerLockName());
			} catch (InterruptedException e) {
				log.error("Critical error!!! Global timeout cleanup watcher interrupted, timeout jobs will not be cleanup.", e);
			}
		}
	}

	/**
	 * Watch timeout jobs, updating their status to failure.
	 * 
	 * @param cleanupFinalizerLockName
	 * @throws InterruptedException
	 */
	private void loopWatchTimeoutCleanupFinalizer(String cleanupFinalizerLockName) throws InterruptedException {
		while (true) {
			long maxIntervalMs = getGlobalJobCleanMaxIntervalMs();
			sleepRandom(DEFAULT_MIN_WATCH_MS, maxIntervalMs);
			if (log.isInfoEnabled()) {
				log.info("Global jobs timeout finalizer cleaning up for global jobCleanMaxIntervalMs:{} ... ", maxIntervalMs);
			}

			Lock lock = lockManager.getLock(cleanupFinalizerLockName);
			try {
				// Cleanup timeout jobs on this node, nodes that do not
				// acquire lock are on ready in place.
				if (lock.tryLock()) {
					taskHistoryDao.updateStatus(config.getBuild().getJobTimeoutMs());
					log.info("Updated pipeline timeout jobs, with jobTimeoutMs:{}, global jobCleanMaxIntervalMs:{}",
							config.getBuild().getJobTimeoutMs(), maxIntervalMs);
				} else {
					log.info("Skip cleaning up ...");
				}
			} catch (Throwable ex) {
				log.error("Failed to timeout jobs cleanup", ex);
			} finally {
				lock.unlock();
			}
		}

	}

	/**
	 * Refresh global distributed {@link GlobalTimeoutJobCleanupFinalizer}
	 * watching intervalMs.
	 * 
	 * @param globalJobCleanMaxIntervalMs
	 * @return
	 */
	public Long refreshGlobalJobCleanMaxIntervalMs(Long globalJobCleanMaxIntervalMs) {
		// Distributed global intervalMs.
		jedisService.setObjectT(KEY_FINALIZER_INTERVALMS, globalJobCleanMaxIntervalMs, -1);
		if (log.isInfoEnabled()) {
			log.info("Refreshed global timeoutCleanupFinalizer of intervalMs:<%s>", globalJobCleanMaxIntervalMs);
		}

		// Get available global intervalMs.
		return getGlobalJobCleanMaxIntervalMs();
	}

	/**
	 * Get {@link GlobalTimeoutJobCleanupFinalizer} watcher locker name.
	 * 
	 * @return
	 */
	private String getCleanupFinalizerLockName() {
		return environment.getRequiredProperty("spring.application.name") + "."
				+ GlobalTimeoutJobCleanupFinalizer.class.getSimpleName();
	}

	/**
	 * Get global distributed {@link GlobalTimeoutJobCleanupFinalizer} watching
	 * intervalMs.
	 * 
	 * @return
	 */
	private Long getGlobalJobCleanMaxIntervalMs() {
		Long maxInternalMs = jedisService.getObjectT(KEY_FINALIZER_INTERVALMS, Long.class);
		return nonNull(maxInternalMs) ? maxInternalMs : config.getBuild().getJobCleanMaxIntervalMs();
	}

}