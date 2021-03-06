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
package com.wl4g.devops.ci.vcs;

import java.util.List;

/**
 * VCS API operator.
 *
 * @author Wangl.sir
 * @version v1.0 2019年8月2日
 * @since
 */
public interface VcsOperator {

	/**
	 * Vcs type definition.
	 * 
	 * @return
	 */
	String vcsType();

	/**
	 * Get VCS remote branch names.
	 *
	 * @param projectId
	 * @return
	 */
	List<String> getRemoteBranchNames(int projectId);

	/**
	 * Get VCS remote tag names.
	 *
	 * @param projectId
	 * @return
	 */
	List<String> getRemoteTags(int projectId);

	/**
	 * Find remote project ID by project name.
	 *
	 * @param projectName
	 * @return
	 */
	Integer findRemoteProjectId(String projectName);

	/**
	 * VCS type definitions.
	 * 
	 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
	 * @version v1.0 2019年11月5日
	 * @since
	 */
	public final static class VcsType {

		/** Vcs for GITLAB. */
		final public static String GITLAB = "VcsWithGitlab";

		/** Vcs for github. */
		final public static String GITHUB = "VcsWithGithub";

		/** Vcs for gitee. */
		final public static String GITEE = "VcsWithGitee";

		/** Vcs for alicode. */
		final public static String ALICODE = "VcsWithAlicode";

		/** Vcs for bitbucket. */
		final public static String BITBUCKET = "VcsWithBitbucket";

		/** Vcs for coding. */
		final public static String CODING = "VcsWithCoding";
	}

}