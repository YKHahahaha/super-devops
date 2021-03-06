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
package com.wl4g.devops.ci.core;

import com.wl4g.devops.common.utils.io.FileIOUtils.ReadResult;

/**
 * CICD pipeline core entry processor.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @author vjay
 * @date 2019-05-16 14:45:00
 */
public interface PipelineManager {

	/**
	 * Startup new pipeline task job.
	 * 
	 * @param taskId
	 */
	void newPipeline(Integer taskId,Integer trackId,Integer trackType,String remark);

	/**
	 * Roll-back pipeline task job.
	 * 
	 * @param taskId
	 */
	void rollbackPipeline(Integer taskId);

	/**
	 * Hook pipeline task job.
	 * 
	 * @param projectName
	 * @param branchName
	 * @param url
	 */
	void hookPipeline(String projectName, String branchName, String url);

	/**
	 * Reader pipeline task job logs.
	 * 
	 * @param taskHisId
	 * @param index
	 * @param size
	 * @return
	 */
	ReadResult logfile(Integer taskHisId, Integer index, Integer size);

}