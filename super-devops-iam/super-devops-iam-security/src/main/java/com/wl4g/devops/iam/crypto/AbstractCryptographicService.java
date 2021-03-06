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
package com.wl4g.devops.iam.crypto;

import static com.wl4g.devops.common.constants.IAMDevOpsConstants.CACHE_CRYPTO;
import static com.wl4g.devops.common.utils.codec.Encodes.toBytes;
import static com.wl4g.devops.common.utils.serialize.JacksonUtils.toJSONString;
import static com.wl4g.devops.common.utils.serialize.ProtostuffUtils.deserialize;
import static com.wl4g.devops.common.utils.serialize.ProtostuffUtils.serialize;
import static io.netty.util.internal.ThreadLocalRandom.current;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.exception.ExceptionUtils.wrapAndThrow;
import static org.springframework.util.Assert.notNull;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;

import com.wl4g.devops.iam.config.properties.CryptoProperties;
import com.wl4g.devops.support.cache.JedisService;
import com.wl4g.devops.support.concurrent.locks.JedisLockManager;

import redis.clients.jedis.JedisCluster;

/**
 * Abstract cryptographic service
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2019-08-30
 * @since
 */
public abstract class AbstractCryptographicService<K extends KeySpecWrapper> implements CryptographicService<K> {

	/**
	 * Default JIGSAW initialize image timeoutMs
	 */
	final public static long DEFAULT_KEY_INIT_TIMEOUTMS = 60_000L;

	final protected Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * KeySpec class.
	 */
	final protected Class<? extends KeySpecWrapper> keySpecClass;

	/**
	 * Simple lock manager.
	 */
	final protected Lock lock;

	/**
	 * Cryptic properties.
	 */
	@Autowired
	protected CryptoProperties config;

	/**
	 * JEDIS service.
	 */
	@Autowired
	protected JedisService jedisService;

	@SuppressWarnings("unchecked")
	public AbstractCryptographicService(JedisLockManager lockManager) {
		notNull(lockManager, "Crypto lockManager must not be null.");
		this.lock = lockManager.getLock(getClass().getSimpleName(), DEFAULT_KEY_INIT_TIMEOUTMS, TimeUnit.MILLISECONDS);

		ResolvableType resolveType = ResolvableType.forClass(getClass());
		this.keySpecClass = (Class<? extends KeySpecWrapper>) resolveType.getSuperType().getGeneric(0).resolve();
		notNull(keySpecClass, "KeySpecClass must not be null.");
	}

	/**
	 * Get random borrow generated key-pairs.
	 *
	 * @return
	 */
	public K borrow() {
		return borrow(-1);
	}

	/**
	 * Get random borrow JIGSAW image code.
	 *
	 * @param index
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public K borrow(int index) {
		if (index < 0 || index >= config.getKeyPairPools()) {
			int _index = current().nextInt(config.getKeyPairPools());
			if (log.isDebugEnabled()) {
				log.debug("Borrow keySpec index '{}' of out bound, used random index '{}'", index, _index);
			}
			index = _index;
		}

		// Load keySpec by index.
		JedisCluster jdsCluster = jedisService.getJedisCluster();
		byte[] keySpecBuf = jdsCluster.hget(CACHE_CRYPTO, toBytes(String.valueOf(index)));
		if (isNull(keySpecBuf)) { // Expired?
			try {
				if (lock.tryLock(DEFAULT_KEY_INIT_TIMEOUTMS / 2, TimeUnit.MILLISECONDS)) {
					initializingKeySpecPool();
				}
			} catch (Exception e) {
				wrapAndThrow(e);
			} finally {
				lock.unlock();
			}
			// Retry get.
			keySpecBuf = jdsCluster.hget(CACHE_CRYPTO, toBytes(String.valueOf(index)));
		}
		K keySpec = (K) deserialize(keySpecBuf, keySpecClass);
		Assert.notNull(keySpec, "Unable to borrow keySpec resource.");
		return keySpec;
	}

	/**
	 * Generate keySpec.
	 *
	 * @return
	 */
	protected abstract K generateKeySpec();

	/**
	 * Initialize keySpec pool.
	 *
	 * @return
	 */
	private synchronized void initializingKeySpecPool() {
		// Create generate cryptic keyPairs
		for (int index = 0; index < config.getKeyPairPools(); index++) {
			// Generate keySpec.
			K keySpec = generateKeySpec();
			// Storage to cache.
			jedisService.getJedisCluster().hset(CACHE_CRYPTO, toBytes(String.valueOf(index)), serialize(keySpec));
			jedisService.getJedisCluster().expire(CACHE_CRYPTO, config.getKeyPairExpireMs());
			if (log.isDebugEnabled()) {
				log.debug("Put keySpec to cache for index {}, keySpec => {}", index, toJSONString(keySpec));
			}
		}
		if (log.isInfoEnabled()) {
			log.info("Initialized keySpec total: {}", config.getKeyPairPools());
		}
	}

}