/*
 * Janssen Project software is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2020, Janssen Project
 */

package io.jans.fido2.service.persist;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;

import io.jans.as.common.model.session.SessionId;
import io.jans.as.common.model.session.SessionIdState;
import io.jans.as.model.config.StaticConfiguration;
import io.jans.fido2.model.conf.AppConfiguration;
import io.jans.orm.PersistenceEntryManager;
import io.jans.orm.model.fido2.Fido2AuthenticationEntry;
import io.jans.orm.model.fido2.Fido2AuthenticationStatus;
import io.jans.orm.model.fido2.Fido2RegistrationEntry;
import io.jans.orm.model.fido2.Fido2RegistrationStatus;
import io.jans.service.CacheService;
import io.jans.util.StringHelper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Configure user session to confirm Fido2 device authentication
 *
 * @author Yuriy Movchan
 * @version August 9, 2017
 */
@ApplicationScoped
public class UserSessionIdService {

	public static final String AUTHENTICATED_USER = "auth_user";

    @Inject
    private Logger log;

    @Inject
    private AppConfiguration appConfiguration;

    @Inject
    private StaticConfiguration staticConfiguration;

    @Inject
    private CacheService cacheService;

    @Inject
    private PersistenceEntryManager persistenceEntryManager;

    public boolean isValidSessionId(String sessionId, String userName) {
        SessionId session = getSessionId(sessionId);
        if (session == null) {
            log.error("Specified session_id '{}' is invalid", sessionId);
            return false;
        }

        if (StringHelper.isNotEmpty(userName)) {
	        String sessionIdUser = session.getSessionAttributes().get(AUTHENTICATED_USER);
	        if (!StringHelper.equalsIgnoreCase(userName, sessionIdUser)) {
	            log.error("Username '{}' and session_id '{}' don't match", userName, sessionId);
	            return false;
	        }
        }

        return true;
    }

    public void updateUserSessionIdOnFinishRequest(String sessionId, String userInum, Fido2RegistrationEntry registrationEntry, boolean enroll) {
        SessionId entity = getSessionId(sessionId);
        if (entity == null) {
            return;
        }

        Map<String, String> sessionAttributes = entity.getSessionAttributes();
        if (Fido2RegistrationStatus.registered == registrationEntry.getRegistrationStatus()) {
            sessionAttributes.put("session_custom_state", "approved");
        } else {
            sessionAttributes.put("session_custom_state", "declined");
        }
        updateSessionId(entity);
    }

    public void updateUserSessionIdOnFinishRequest(String sessionId, String userInum, Fido2RegistrationEntry registrationEntry, Fido2AuthenticationEntry authenticationEntry, boolean enroll) {
        SessionId entity = getSessionId(sessionId);
        if (entity == null) {
            return;
        }

        Map<String, String> sessionAttributes = entity.getSessionAttributes();
        if (Fido2AuthenticationStatus.authenticated == authenticationEntry.getAuthenticationStatus()) {
            sessionAttributes.put("session_custom_state", "approved");
        } else {
            sessionAttributes.put("session_custom_state", "declined");
        }
        

        updateSessionId(entity);
    }


    public void updateUserSessionIdOnError(String sessionId) {
        SessionId entity = getSessionId(sessionId);
        if (entity == null) {
            return;
        }

        Map<String, String> sessionAttributes = entity.getSessionAttributes();
        sessionAttributes.put("session_custom_state", "declined");

        // TODO: Check if this not reset ttl and expiration. Check original SessionId service
        updateSessionId(entity);
    }

    public void updateSessionId(SessionId entity) {
        if (isTrue(appConfiguration.getSessionIdPersistInCache())) {
        	// Reuse existing expiration
        	int expirationInSeconds = (int) ((entity.getExpirationDate().getTime() - new Date().getTime()) / 1000);
        	
        	// Make sure that expiration is bigger than zero
        	if (expirationInSeconds <= 0) {
        		expirationInSeconds = CacheService.DEFAULT_EXPIRATION;
        	}
            cacheService.put(expirationInSeconds, entity.getDn(), entity);
        } else {
        	entity.setLastUsedAt(new Date());
	        persistenceEntryManager.merge(entity);
        }
	}

    private SessionId getSessionId(String sessionId) {
        if (StringHelper.isEmpty(sessionId)) {
            return null;
        }
        
        final SessionId entity;
        try {
        	String sessionDn = buildDn(sessionId);
            if (isTrue(appConfiguration.getSessionIdPersistInCache())) {
            	entity = (SessionId) cacheService.get(sessionDn);
            } else {
	            entity = persistenceEntryManager.find(SessionId.class, sessionDn);
	            if (entity == null) {
	                log.warn("Failed to load session id '{}'", sessionId);
	            }
            }
        } catch (Exception ex) {
            log.trace(ex.getMessage(), ex);
            return null;
        }

        if (entity == null) {
            return null;
        }

        if (!((SessionIdState.UNAUTHENTICATED == entity.getState())
        		|| (SessionIdState.AUTHENTICATED == entity.getState()))) {
            log.warn("Unexpected session id '{}' state: '{}'", sessionId, entity.getState());
            return null;
        }

        return entity;
    }

    private String buildDn(String sessionId) {
        return String.format("jansId=%s,%s", sessionId, staticConfiguration.getBaseDn().getSessions());
    }

}
