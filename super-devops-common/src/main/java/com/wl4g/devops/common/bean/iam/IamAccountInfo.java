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
package com.wl4g.devops.common.bean.iam;

import java.io.Serializable;

import org.springframework.util.Assert;

/**
 * IAM account information.
 * 
 * @author Wangl.sir &lt;Wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0.0 2018-04-31
 * @since
 */
public interface IamAccountInfo extends Serializable {

	/**
	 * Get account principal name.
	 * 
	 * @return
	 */
	String getPrincipal();

	/**
	 * Stored encrypted credentials
	 * 
	 * @return Encrypted credentials string
	 */
	String getStoredCredentials();

	/**
	 * Parameters for obtaining account information
	 * 
	 * @author wangl.sir
	 * @version v1.0 2019年1月8日
	 * @since
	 */
	public static interface Parameter extends Serializable {

	}

	/**
	 * SNS parameter definition
	 * 
	 * @author wangl.sir
	 * @version v1.0 2019年1月8日
	 * @since
	 */
	public static interface SnsParameter extends Parameter {

		/**
		 * If the provider is not empty, it means social network login. Provider
		 * is optional: qq/wechat/sina/google/github/twitter/facebook/dingtalk,
		 * etc.
		 * 
		 * @return
		 */
		String getProvider();

		/**
		 * Social networking services openId
		 * 
		 * @return
		 */
		String getOpenId();

		/**
		 * Social networking services unionId(optional)
		 * 
		 * @return
		 */
		String getUnionId();

	}

	/**
	 * Abstract based parameters definition
	 * 
	 * @author wangl.sir
	 * @version v1.0 2019年1月8日
	 * @since
	 */
	public static abstract class BasedParameter implements Parameter {
		private static final long serialVersionUID = -898874009263858359L;

		final private String principal;

		public BasedParameter(String principal) {
			Assert.hasText(principal, "'principal' must not be empty");
			this.principal = principal;
		}

		public String getPrincipal() {
			return principal;
		}

	}

	/**
	 * UsernamePassword sign-in parameter definition
	 * 
	 * @author wangl.sir
	 * @version v1.0 2019年1月8日
	 * @since
	 */
	public static class SimpleParameter extends BasedParameter {
		private static final long serialVersionUID = -7501007252263127579L;

		public SimpleParameter(String principal) {
			super(principal);
		}

	}

	/**
	 * SMS dynamic password sign-in parameter definition
	 * 
	 * @author wangl.sir
	 * @version v1.0 2019年1月8日
	 * @since
	 */
	public static class SmsParameter extends BasedParameter {
		private static final long serialVersionUID = -7501007252263557579L;

		public SmsParameter(String principal) {
			super(principal);
		}

	}

	/**
	 * SNS authorizing parameter definition
	 * 
	 * @author wangl.sir
	 * @version v1.0 2019年1月8日
	 * @since
	 */
	public static class SnsAuthorizingParameter implements SnsParameter {
		private static final long serialVersionUID = -898874019263858359L;

		/**
		 * If the provider is not empty, it means social network login. Provider
		 * is optional: qq/wechat/sina/google/github/twitter/facebook/dingtalk,
		 * etc.
		 */
		final private String provider;

		/**
		 * Social networking services openId
		 */
		final private String openId;

		/**
		 * Social networking services unionId
		 */
		final private String unionId;

		/**
		 * SnsAuthorizingParameter construction
		 * 
		 * @param provider
		 * @param openId
		 * @param unionId
		 *            May be empty, Unionid is possible only when WeChat or
		 *            Facebook public platforms
		 */
		public SnsAuthorizingParameter(String provider, String openId, String unionId) {
			Assert.notNull(provider, "'provider' must not be null");
			Assert.notNull(openId, "'openId' must not be null");
			this.provider = provider;
			this.openId = openId;
			this.unionId = unionId;
		}

		@Override
		public String getProvider() {
			return provider;
		}

		@Override
		public String getOpenId() {
			return openId;
		}

		@Override
		public String getUnionId() {
			return unionId;
		}

	}

}