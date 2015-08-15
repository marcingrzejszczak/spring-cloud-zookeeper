/*
 * Copyright 2013-2015 the original author or authors.
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

package org.springframework.cloud.zookeeper;

import lombok.Data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Spencer Gibb
 */
@ConfigurationProperties(prefix = "endpoints.zookeeper", ignoreUnknownFields = false)
public class ZookeeperEndpoint extends AbstractEndpoint<ZookeeperEndpoint.ZookeeperData> {

	@Autowired
	public ZookeeperEndpoint() {
		super("zookeeper", false, true);
	}

	@Override
	public ZookeeperData invoke() {
		ZookeeperData data = new ZookeeperData();
		return data;
	}

	@Data
	public static class ZookeeperData {
	}
}
