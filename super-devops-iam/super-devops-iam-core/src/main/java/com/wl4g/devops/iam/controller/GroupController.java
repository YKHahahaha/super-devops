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
package com.wl4g.devops.iam.controller;

import com.wl4g.devops.common.bean.iam.Group;
import com.wl4g.devops.common.web.RespBase;
import com.wl4g.devops.iam.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author vjay
 * @date 2019-10-29 16:19:00
 */
@RestController
@RequestMapping("/group")
public class GroupController {

	@Autowired
	private GroupService groupService;

	// @RequiresPermissions("iam:group:tree")
	@RequestMapping(value = "/getGroupsTree")
	public RespBase<?> getGroupsTree() {
		RespBase<Object> resp = RespBase.create();
		List<Group> groupsTree = groupService.getGroupsTree();
		resp.buildMap().put("data", groupsTree);
		return resp;
	}

	@RequestMapping(value = "/save")
	public RespBase<?> save(@RequestBody Group group) {
		RespBase<Object> resp = RespBase.create();
		groupService.save(group);
		return resp;
	}

	@RequestMapping(value = "/del")
	public RespBase<?> del(Integer id) {
		RespBase<Object> resp = RespBase.create();
		groupService.del(id);
		return resp;
	}

	@RequestMapping(value = "/detail")
	public RespBase<?> detail(Integer id) {
		RespBase<Object> resp = RespBase.create();
		Group group = groupService.detail(id);
		resp.buildMap().put("data", group);
		return resp;
	}

}