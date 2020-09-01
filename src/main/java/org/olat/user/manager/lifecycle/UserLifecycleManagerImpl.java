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
package org.olat.user.manager.lifecycle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.SearchIdentityParams;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.basesecurity.manager.IdentityDAO;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.session.UserSessionManager;
import org.olat.repository.RepositoryDeletionModule;
import org.olat.user.UserDataDeletable;
import org.olat.user.UserLifecycleManager;
import org.olat.user.UserManager;
import org.olat.user.UserModule;
import org.olat.user.ui.admin.lifecycle.UserAdminLifecycleConfigurationController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 2 juin 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class UserLifecycleManagerImpl implements UserLifecycleManager {
	
	private static final Logger log = Tracing.createLoggerFor(UserLifecycleManagerImpl.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private UserModule userModule;
	@Autowired
	private IdentityDAO identityDao;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private UserSessionManager userSessionManager;
	@Autowired
	private RepositoryDeletionModule repositoryDeletionModule;
	

	public List<Identity> getReadyToInactivateIdentities(Date loginDate) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select ident from ").append(IdentityImpl.class.getName()).append(" as ident")
		  .append(" inner join fetch ident.user as user")
		  .append(" where ident.status in (:statusList) and ((ident.lastLogin = null and ident.creationDate < :lastLogin) or ident.lastLogin < :lastLogin)")
		  .append(" and ident.inactivationEmailDate is null");
		
		List<Integer> statusList = Arrays.asList(Identity.STATUS_ACTIV, Identity.STATUS_PENDING, Identity.STATUS_LOGIN_DENIED);
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("statusList", statusList)
				.setParameter("lastLogin", loginDate, TemporalType.TIMESTAMP)
				.getResultList();
	}
	
	public List<Identity> getIdentitiesToInactivate(Date loginDate, Date emailBeforeDate) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select ident from ").append(IdentityImpl.class.getName()).append(" as ident")
		  .append(" inner join fetch ident.user as user")
		  .append(" where ident.status in (:statusList) and ((ident.lastLogin = null and ident.creationDate < :lastLogin) or ident.lastLogin < :lastLogin)");
		if(emailBeforeDate != null) {
			sb.append(" and ident.inactivationEmailDate<:emailDate");	
		}

		List<Integer> statusList = Arrays.asList(Identity.STATUS_ACTIV, Identity.STATUS_PENDING, Identity.STATUS_LOGIN_DENIED);
		TypedQuery<Identity> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("statusList", statusList)
				.setParameter("lastLogin", loginDate, TemporalType.TIMESTAMP);
		if(emailBeforeDate != null) {
			query.setParameter("emailDate", emailBeforeDate);	
		}
		return query.getResultList();
	}
	
	public List<Identity> getReadyToDeleteIdentities(Date inactivationDate) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select ident from ").append(IdentityImpl.class.getName()).append(" as ident")
		  .append(" inner join fetch ident.user as user")
		  .append(" where ident.status=:status and ident.inactivationDate<:inactivationDate")
		  .append(" and ident.deletionEmailDate is null");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("status", Identity.STATUS_INACTIVE)
				.setParameter("inactivationDate", inactivationDate, TemporalType.TIMESTAMP)
				.getResultList();
	}
	
	public List<Identity> getIdentitiesToDelete(Date inactivationDate, Date emailBeforeDate) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select ident from ").append(IdentityImpl.class.getName()).append(" as ident")
		  .append(" inner join fetch ident.user as user")
		  .append(" where ident.status=:status and ident.inactivationDate<:inactivationDate");
		if(emailBeforeDate != null) {
			sb.append(" and ident.deletionEmailDate<:emailDate");	
		}

		TypedQuery<Identity> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("status", Identity.STATUS_INACTIVE)
				.setParameter("inactivationDate", inactivationDate, TemporalType.TIMESTAMP);
		if(emailBeforeDate != null) {
			query.setParameter("emailDate", emailBeforeDate);	
		}
		return query.getResultList();
	}

	public Identity setIdentityAsInactive(Identity identity) {
		return securityManager.saveIdentityStatus(identity, Identity.STATUS_INACTIVE, null);
	}
	
	public Identity setIdentityInactivationMail(Identity identity) {
		((IdentityImpl)identity).setInactivationEmailDate(new Date());
		return identityDao.saveIdentity(identity);
	}
	
	public Identity setIdentityDeletionMail(Identity identity) {
		((IdentityImpl)identity).setDeletionEmailDate(new Date());
		return identityDao.saveIdentity(identity);
	}

	@Override
	public void inactivateIdentities(Set<Identity> vetoed) {
		int numOfDaysBeforeDeactivation = userModule.getNumberOfInactiveDayBeforeDeactivation();
		int numOfDaysBeforeEmail = userModule.getNumberOfDayBeforeDeactivationMail();
		boolean sendMailBeforeDeactivation = userModule.isMailBeforeDeactivation() && numOfDaysBeforeEmail > 0;
		if(sendMailBeforeDeactivation) {
			int days = numOfDaysBeforeDeactivation - numOfDaysBeforeEmail;
			Date lastLoginDate = getDate(days);
			List<Identity> identities = getReadyToInactivateIdentities(lastLoginDate);
			if(!identities.isEmpty()) {
				for(Identity identity:identities) {
					sendEmail(identity, "mail.before.deactivation.subject", "mail.before.deactivation.body", "before deactiviation");
					identity = setIdentityInactivationMail(identity);
					vetoed.add(identity);
				}
			}
		}

		Date lastLoginDate = getDate(numOfDaysBeforeDeactivation);
		List<Identity> identities;
		if(sendMailBeforeDeactivation) {
			identities = getIdentitiesToInactivate(lastLoginDate, null);
		} else {
			Date emailBeforeDate = getDate(numOfDaysBeforeEmail);
			identities = getIdentitiesToInactivate(lastLoginDate, emailBeforeDate);
		}
		
		for(Identity identity:identities) {
			if(!vetoed.contains(identity)) {
				identity = setIdentityAsInactive(identity);
				if(userModule.isMailAfterDeactivation()) {
					sendEmail(identity, "mail.after.deactivation.subject", "mail.after.deactivation.body", "after deactiviation");
				}
				vetoed.add(identity);
			}
		}
		
		dbInstance.commitAndCloseSession();
	}
	
	@Override
	public void deleteIdentities(Set<Identity> vetoed) {
		int numOfDaysBeforeDeletion = userModule.getNumberOfInactiveDayBeforeDeletion();
		int numOfDaysBeforeEmail = userModule.getNumberOfDayBeforeDeletionMail();
		boolean sendMailBeforeDeletion = userModule.isMailBeforeDeletion() && numOfDaysBeforeEmail > 0;
		if(sendMailBeforeDeletion) {
			int days = numOfDaysBeforeDeletion - numOfDaysBeforeEmail;
			Date lastLoginDate = getDate(days);
			List<Identity> identities = getReadyToDeleteIdentities(lastLoginDate);
			if(!identities.isEmpty()) {
				for(Identity identity:identities) {
					sendEmail(identity, "mail.before.deletion.subject", "mail.before.deletion.body", "before deletion");
					identity = setIdentityDeletionMail(identity);
					vetoed.add(identity);
				}
			}
		}

		Date lastLoginDate = getDate(numOfDaysBeforeDeletion);
		Date emailBeforeDate = null;
		if(sendMailBeforeDeletion) {
			emailBeforeDate = getDate(numOfDaysBeforeEmail);
		}
		List<Identity> identities = getIdentitiesToDelete(lastLoginDate, emailBeforeDate);
		int numberOfIdentities = countActiveAndInactiveIdentities();
		double procentToDelete = ((double)identities.size() / numberOfIdentities) * 100.0d;
		if(procentToDelete < userModule.getUserAutomaticDeletionUsersPercentage()) {
			for(Identity identity:identities) {
				if(!vetoed.contains(identity)) {
					if(userModule.isMailAfterDeletion()) {
						sendEmail(identity, "mail.after.deletion.subject", "mail.after.deletion.body", "after deletion");
					}
					deleteIdentity(identity, null);
				}
			}
		} else {
			log.error("Security thresold to delete users: number of users to delete {}, total number of users: {}, max. percent: {}",
					identities.size(), numberOfIdentities, userModule.getUserAutomaticDeletionUsersPercentage());
		}
		
		dbInstance.commitAndCloseSession();
	}
	
	/**
	 * @return The number of not deleted identities
	 */
	private int countActiveAndInactiveIdentities() {
		SearchIdentityParams params = new SearchIdentityParams();
		List<Integer> statusList = Arrays.asList(Identity.STATUS_ACTIV, Identity.STATUS_PERMANENT,
				Identity.STATUS_PENDING, Identity.STATUS_INACTIVE, Identity.STATUS_LOGIN_DENIED);
		params.setExactStatusList(statusList);
		return securityManager.countIdentitiesByPowerSearch(params);
	}
	
	@Override
	public boolean deleteIdentity(Identity identity, Identity doer) {
		log.info("Start deleteIdentity for identity={}", identity);		
		if(Identity.STATUS_PERMANENT.equals(identity.getStatus())) {
			log.info("Aborted deletion of identity={}, identity is flagged as PERMANENT", identity);					
			return false;
		}
		// Logout user and start with delete process
		userSessionManager.signOffAndClearAll(identity);
		// set some data
		identity = securityManager.saveDeletedByData(identity, doer);
		dbInstance.commit();
		
		
		// Delete data of modules that implement the user data deletable
		String anonymisedIdentityName = "del_" + identity.getKey().toString();
		Map<String,UserDataDeletable> userDataDeletableResourcesMap = CoreSpringFactory.getBeansOfType(UserDataDeletable.class);
		List<UserDataDeletable> userDataDeletableResources = new ArrayList<>(userDataDeletableResourcesMap.values());
		// Start with high priorities (900: user manager), then continue with
		// others. Default priority is 500. End with low priorities (100: base
		// security)
		Collections.sort(userDataDeletableResources, new UserDataDeletableComparator());
		for (UserDataDeletable element : userDataDeletableResources) {
			try {
				log.info("UserDataDeletable-Loop for identity::{} and element::{}", identity.getKey(), element.getClass().getSimpleName());
				element.deleteUserData(identity, anonymisedIdentityName);				
			} catch (Exception e) {
				log.error("Error while deleting identity::{} data for and element::{}. Aboring delete process, user partially deleted, but not yet marked as deleted",
						identity.getKey(), element.getClass().getSimpleName(), e);
				dbInstance.rollbackAndCloseSession();
				return false;
			}
		}

		// Done with all modules that keep user data, now finish delete process
		dbInstance.commit();
		
		// Remove identity from all remaining groups and remove roles
		int count = groupDao.removeMemberships(identity);
		log.info("Delete {} group memberships/roles for identity::{}", count, identity.getKey());

		// Anonymise identity to conform with data privacy law. The username is removed
		// by default and replaced with an anonymous database key. The identity
		// object itself must remain in the database since there are referenced
		// objects such as undeletable forum entries linked to it
		identity = securityManager.saveIdentityName(identity, anonymisedIdentityName, null);
		log.info("Replaced username with database key for identity::{}", identity.getKey());

		// Finally mark user as deleted and we are done
		identity = securityManager.saveIdentityStatus(identity, Identity.STATUS_DELETED, doer);
		log.info("Data of identity deleted and state of identity::{} changed to 'deleted'", identity.getKey());

		dbInstance.commit();
		log.info(Tracing.M_AUDIT, "User-Deletion: Deleted identity::{}", identity.getKey());
		return true;
	}	
	
	private void sendEmail(Identity identity, String subjectI18nKey, String bodyI18nKey, String type) {
		String language = identity.getUser().getPreferences().getLanguage();
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(language);
		Translator translator = Util.createPackageTranslator(UserAdminLifecycleConfigurationController.class, locale);
		
		String subject = translator.translate(subjectI18nKey);
		String body = translator.translate(bodyI18nKey);
		LifecycleMailTemplate template = new LifecycleMailTemplate(subject, body, locale);
		
		sendUserEmailTo(identity, template, type);
	}
	
	private void sendUserEmailTo(Identity identity, MailTemplate template, String type) {
		// for backwards compatibility
		template.addToContext("responseTo", repositoryDeletionModule.getEmailResponseTo());

		MailerResult result = new MailerResult();
		MailBundle bundle = mailManager.makeMailBundle(null, identity, template, null, null, result);
		if(bundle != null) {
			mailManager.sendMessage(bundle);
		}
		log.info(Tracing.M_AUDIT, "User lifecycle {} send to identity={} with email={}", type, identity.getKey(), identity.getUser().getProperty(UserConstants.EMAIL, null));
	}
	
	private Date getDate(int days) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -days);
		Date date = cal.getTime();
		return CalendarUtils.startOfDay(date);
	}
	
	private final class LifecycleMailTemplate extends MailTemplate {
		
		private final Locale locale;
		
		public LifecycleMailTemplate(String subject, String body, Locale locale) {
			super(subject, body, null);
			this.locale = locale;
		}

		@Override
		public void putVariablesInMailContext(VelocityContext vContext, Identity recipient) {
			if(recipient != null) {
				User user = recipient.getUser();
				
				vContext.put("firstname", user.getProperty(UserConstants.FIRSTNAME, null));
				vContext.put(UserConstants.FIRSTNAME, user.getProperty(UserConstants.FIRSTNAME, null));
				vContext.put("lastname", user.getProperty(UserConstants.LASTNAME, null));
				vContext.put(UserConstants.LASTNAME, user.getProperty(UserConstants.LASTNAME, null));
				String fullName = userManager.getUserDisplayName(recipient);
				vContext.put("fullname", fullName);
				vContext.put("fullName", fullName); 
				vContext.put("mail", userManager.getUserDisplayEmail(user, locale));
				vContext.put("email", userManager.getUserDisplayEmail(user, locale));
				String loginName = securityManager.findAuthenticationName(recipient);
				if(!StringHelper.containsNonWhitespace(loginName)) {
					loginName = recipient.getName();
				}
				vContext.put("username", loginName);
			}
		}
	}
	
	private static final class UserDataDeletableComparator implements Comparator<UserDataDeletable> {
		
		@Override
		public int compare(UserDataDeletable o1, UserDataDeletable o2) {
			int p1 = o1.deleteUserDataPriority();
			int p2 = o2.deleteUserDataPriority();
			return -Integer.compare(p1, p2);
		}
	}

}
