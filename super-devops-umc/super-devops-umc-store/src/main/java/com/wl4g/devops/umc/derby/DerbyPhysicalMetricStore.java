package com.wl4g.devops.umc.derby;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import com.wl4g.devops.common.bean.umc.model.physical.Cpu;
import com.wl4g.devops.common.bean.umc.model.physical.Disk;
import com.wl4g.devops.common.bean.umc.model.physical.Mem;
import com.wl4g.devops.common.bean.umc.model.physical.Net;
import com.wl4g.devops.umc.store.PhysicalMetricStore;

/**
 * Derby foundation store
 * 
 * @author wangl.sir
 * @version v1.0 2019年6月17日
 * @since
 */
public class DerbyPhysicalMetricStore implements PhysicalMetricStore {

	final JdbcTemplate jdbcTemplate;

	public DerbyPhysicalMetricStore(JdbcTemplate jdbcTemplate) {
		Assert.notNull(jdbcTemplate, "JdbcTemplate must not be null");
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public boolean save(Cpu cpu) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean save(Mem mem) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean save(Disk disk) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean save(Net net) {
		throw new UnsupportedOperationException();
	}

}