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
package com.wl4g.devops.iam.service;

import com.wl4g.devops.common.bean.PageModel;
import com.wl4g.devops.common.bean.iam.Menu;
import com.wl4g.devops.common.bean.iam.User;

import java.util.Map;
import java.util.Set;

/**
 * @author vjay
 * @date 2019-10-28 16:38:00
 */
public interface UserService {

	User getUserById(Integer id);

	Set<Menu> getMenusByUserId(Integer userId);

	Map<String, Object> list(PageModel pm, String userName, String displayName);

	void save(User user);

	void del(Integer userId);

	User detail(Integer userId);

}