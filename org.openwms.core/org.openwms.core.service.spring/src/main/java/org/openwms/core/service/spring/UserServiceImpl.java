/*
 * openwms.org, the Open Warehouse Management System.
 *
 * This file is part of openwms.org.
 *
 * openwms.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * openwms.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software. If not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.openwms.core.service.spring;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openwms.core.annotation.FireAfterTransaction;
import org.openwms.core.domain.system.usermanagement.Role;
import org.openwms.core.domain.system.usermanagement.SecurityObject;
import org.openwms.core.domain.system.usermanagement.SystemUser;
import org.openwms.core.domain.system.usermanagement.User;
import org.openwms.core.domain.system.usermanagement.UserDetails;
import org.openwms.core.domain.system.usermanagement.UserPassword;
import org.openwms.core.domain.system.usermanagement.UserPreference;
import org.openwms.core.exception.InvalidPasswordException;
import org.openwms.core.integration.SecurityObjectDao;
import org.openwms.core.integration.UserDao;
import org.openwms.core.service.ConfigurationService;
import org.openwms.core.service.UserService;
import org.openwms.core.service.exception.ServiceRuntimeException;
import org.openwms.core.service.exception.UserNotFoundException;
import org.openwms.core.util.event.UserChangedEvent;
import org.openwms.core.util.validation.AssertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * An UserServiceImpl is a Spring supported transactional implementation of a
 * general {@link UserService}. Using Spring 2 annotation support autowires
 * collaborators, therefore XML configuration becomes obsolete. This class is
 * marked with Springs {@link Service} annotation to benefit from Springs
 * exception translation intercepter. Traditional CRUD operations are delegated
 * to an {@link UserDao}.
 * <p>
 * This implementation can be autowired with the name {@value #COMPONENT_NAME}.
 * </p>
 * 
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 * @version $Revision$
 * @since 0.1
 * @see org.openwms.core.integration.UserDao
 */
@Transactional
@Service(UserServiceImpl.COMPONENT_NAME)
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
    @Autowired
    private UserDao dao;
    @Autowired
    @Qualifier("securityObjectDao")
    private SecurityObjectDao securityObjectDao;
    @Autowired
    private ConfigurationService confSrv;
    @Autowired
    private PasswordEncoder enc;
    @Autowired
    private SaltSource saltSource;
    @Value("#{ globals['system.user'] }")
    private String systemUsername;
    @Value("#{ globals['system.password'] }")
    private String systemPassword;
    /** Springs service name. */
    public static final String COMPONENT_NAME = "userService";

    /**
     * {@inheritDoc}
     * 
     * Implementation returns an empty list in case of no result.
     */
    @Override
    public List<User> findAll() {
        List<User> users = dao.findAll();
        if (users == null) {
            users = Collections.emptyList();
        }
        return users;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws UserNotFoundException
     *             when no User was found with this username.
     */
    @Override
    @FireAfterTransaction(events = { UserChangedEvent.class })
    public void uploadImageFile(String username, byte[] image) {
        User user = dao.findByUniqueId(username);
        if (user == null) {
            throw new UserNotFoundException("User with username [" + username + "] not found");
        }
        if (user.getUserDetails() == null) {
            user.setUserDetails(new UserDetails());
        }
        user.getUserDetails().setImage(image);
        dao.save(user);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws IllegalArgumentException
     *             when <code>user</code> is <code>null</code>
     */
    @Override
    @FireAfterTransaction(events = { UserChangedEvent.class })
    public User save(User user) {
        AssertUtils.notNull(user, "The instance of the User to be saved must not be null");
        if (user.isNew()) {
            dao.persist(user);
        }
        return dao.save(user);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws IllegalArgumentException
     *             when <code>user</code> is <code>null</code>
     */
    @Override
    @FireAfterTransaction(events = { UserChangedEvent.class })
    public void remove(User user) {
        AssertUtils.notNull(user, "The instance of the User to be removed is null");
        if (user.isNew()) {
            LOGGER.info("The User instance to be removed is not persist yet, no need to remove it");
        } else {
            dao.remove(dao.findById(user.getId()));
        }
    }

    /**
     * {@inheritDoc}
     * 
     * Marked as <code>readOnly</code> transactional method.
     */
    @Override
    @Transactional(readOnly = true)
    public User getTemplate(String username) {
        return new User(username);
    }

    /**
     * {@inheritDoc}
     * 
     * Marked as <code>readOnly</code> transactional method.
     */
    @Override
    @Transactional(readOnly = true)
    public SystemUser createSystemUser() {
        // CHECK [scherrer] : check this
        SystemUser sys = new SystemUser(systemUsername, systemPassword);
        Role role = new Role.Builder(SystemUser.SYSTEM_ROLE_NAME).withDescription("SuperUsers Role").asImmutable()
                .build();
        role.setGrants(new HashSet<SecurityObject>(securityObjectDao.findAll()));
        sys.addRole(role);
        return sys;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws IllegalArgumentException
     *             when <code>userPassword</code> is <code>null</code>
     * @throws ServiceRuntimeException
     *             when <code>userPassword</code> is not a valid password
     * @throws UserNotFoundException
     *             when no {@link User} exist
     */
    @Override
    @FireAfterTransaction(events = { UserChangedEvent.class })
    public void changeUserPassword(UserPassword userPassword) {
        AssertUtils.notNull(userPassword, "Error while changing the user password, new value is null");
        User entity = dao.findByUniqueId(userPassword.getUser().getUsername());
        if (entity == null) {
            throw new UserNotFoundException("User not found, probably not persisted before or has been removed");
        }
        try {
            entity.changePassword(enc.encodePassword(userPassword.getPassword(),
                    saltSource.getSalt(new UserWrapper(entity))));
            dao.save(entity);
        } catch (InvalidPasswordException ipe) {
            LOGGER.info(ipe.getMessage());
            throw new ServiceRuntimeException("Password does not match the defined pattern", ipe);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @throws IllegalArgumentException
     *             when <code>user</code> is <code>null</code>
     * 
     * @see org.openwms.core.service.UserService#saveUserProfile(org.openwms.core.domain.system.usermanagement.User,
     *      org.openwms.core.domain.system.usermanagement.UserPassword,
     *      org.openwms.core.domain.system.usermanagement.UserPreference[])
     */
    @Override
    @FireAfterTransaction(events = { UserChangedEvent.class })
    public User saveUserProfile(User user, UserPassword userPassword, UserPreference... prefs) {
        AssertUtils.notNull(user, "Could not save the user profile because the argument user is null");
        if (userPassword != null && StringUtils.isNotEmpty(userPassword.getPassword())) {
            try {
                user.changePassword(enc.encodePassword(userPassword.getPassword(),
                        saltSource.getSalt(new UserWrapper(user))));
            } catch (InvalidPasswordException ipe) {
                LOGGER.info(ipe.getMessage());
                throw new ServiceRuntimeException("Password does not match the defined pattern", ipe);
            }
        }
        for (UserPreference preference : prefs) {
            confSrv.save(preference);
        }
        return save(user);
    }
}