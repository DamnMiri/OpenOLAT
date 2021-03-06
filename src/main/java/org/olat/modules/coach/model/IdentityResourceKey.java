/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.coach.model;

/**
 * 
 * Initial date: 21.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityResourceKey {
	
	private final Long identityKey;
	private final Long resourceKey;
	
	public IdentityResourceKey(Long identityKey, Long resourceKey) {
		this.identityKey = identityKey;
		this.resourceKey = resourceKey;
	}
	
	public Long getIdentityKey() {
		return identityKey;
	}
	
	public Long getResourceKey() {
		return resourceKey;
	}
	
	@Override
	public int hashCode() {
		return (identityKey == null ? 98268 : identityKey.hashCode())
				+ (resourceKey == null ? -2634785 : resourceKey.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof IdentityResourceKey) {
			IdentityResourceKey key = (IdentityResourceKey)obj;
			return identityKey != null && identityKey.equals(key.getIdentityKey())
					&& resourceKey != null && resourceKey.equals(key.getResourceKey());
		}
		return false;
	}
}