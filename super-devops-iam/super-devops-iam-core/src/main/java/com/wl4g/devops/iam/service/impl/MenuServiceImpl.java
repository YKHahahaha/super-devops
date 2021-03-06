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
package com.wl4g.devops.iam.service.impl;

import com.wl4g.devops.common.bean.BaseBean;
import com.wl4g.devops.common.bean.iam.Group;
import com.wl4g.devops.common.bean.iam.GroupMenu;
import com.wl4g.devops.common.bean.iam.Menu;
import com.wl4g.devops.dao.iam.GroupMenuDao;
import com.wl4g.devops.dao.iam.MenuDao;
import com.wl4g.devops.iam.handler.UserUtil;
import com.wl4g.devops.iam.service.GroupService;
import com.wl4g.devops.iam.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static com.wl4g.devops.common.bean.BaseBean.DEFAULT_USER_ROOT;

/**
 * Menu service implements.
 * 
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @author vjay
 * @date 2019-10-30 15:48:00
 */
@Service
public class MenuServiceImpl implements MenuService {

	@Autowired
	private MenuDao menuDao;

	@Autowired
	private UserUtil userUtil;

	@Autowired
	private GroupService groupService;

	@Autowired
	private GroupMenuDao groupMenuDao;

	@Override
	public Map<String, Object> getMenuTree() {
		Map<String, Object> result = new HashMap<>();
		Set<Menu> menusSet = getMenusSet();
		List<Menu> menus = new ArrayList<>(menusSet);
		menus = set2Tree(menus);
		result.put("data", menus);
		result.put("data2", new ArrayList<>(menusSet));
		return result;
	}

	public Menu getParent(List<Menu> menus, Integer parentId) {
		for (Menu menu : menus) {
			if (parentId != null && menu.getId() != null && menu.getId().intValue() == parentId.intValue()) {
				return menu;
			}
		}
		return null;
	}

	@Override
	public List<Menu> getMenuList() {
		return menuDao.selectByUserId(userUtil.getCurrentLoginUserId());
	}

	@Override
	public void save(Menu menu) {
		if (menu.getId() != null) {
			update(menu);
		} else {
			insert(menu);
		}
	}

	@Override
	public void del(Integer id) {
		Assert.notNull(id, "id is null");
		Menu menu = new Menu();
		menu.setId(id);
		menu.setDelFlag(BaseBean.DEL_FLAG_DELETE);
		menuDao.updateByPrimaryKeySelective(menu);
	}

	@Override
	public Menu detail(Integer id) {
		return menuDao.selectByPrimaryKey(id);
	}

	private void insert(Menu menu) {
		menu.preInsert();
		menuDao.insertSelective(menu);

		// Add group , default add the first group to group_menu
		Set<Group> groupsSet = groupService.getGroupsSet();
		List<Group> top = new ArrayList<>();
		for (Group group : groupsSet) {
			Group parent = groupService.getParent(new ArrayList<>(groupsSet), group.getParentId());
			if (parent == null) {
				top.add(group);
			}
		}
		Assert.isTrue(CollectionUtils.isEmpty(groupsSet), "not found top group");
		Group group = top.get(0);
		GroupMenu groupMenu = new GroupMenu();
		groupMenu.preInsert();
		groupMenu.setGroupId(group.getId());
		groupMenu.setMenuId(menu.getId());
		groupMenuDao.insertSelective(groupMenu);
	}

	private void update(Menu menu) {
		menu.preUpdate();
		menuDao.updateByPrimaryKeySelective(menu);
	}

	private Set<Menu> getMenusSet() {
		Integer currentLoginUserId = userUtil.getCurrentLoginUserId();
		List<Menu> menus = null;
		String currentLoginUsername = userUtil.getCurrentLoginUsername();
		if (DEFAULT_USER_ROOT.equals(currentLoginUsername)) {
			menus = menuDao.selectByRoot();
		} else {
			menus = menuDao.selectByUserIdAccessGroup(currentLoginUserId);
		}
		Set<Menu> set = new HashSet<>();
		set.addAll(menus);
		return set;
	}

	private List<Menu> set2Tree(List<Menu> menus) {
		List<Menu> top = new ArrayList<>();
		for (Menu menu : menus) {
			Menu parent = getParent(menus, menu.getParentId());
			if (parent == null) {
				top.add(menu);
			}
		}
		for (Menu menu : top) {
			List<Menu> children = getChildren(menus, null, menu.getId());
			if (!CollectionUtils.isEmpty(children)) {
				menu.setChildren(children);
			}
		}
		return top;
	}

	private List<Menu> getChildren(List<Menu> menus, List<Menu> children, Integer parentId) {
		if (children == null) {
			children = new ArrayList<>();
		}
		for (Menu menu : menus) {
			if (menu.getParentId() != null && parentId != null && menu.getParentId().intValue() == parentId.intValue()) {
				children.add(menu);
			}
		}
		for (Menu menu : children) {
			List<Menu> children1 = getChildren(menus, null, menu.getId());
			if (!CollectionUtils.isEmpty(children1)) {
				menu.setChildren(children1);
			}
		}
		return children;
	}

}