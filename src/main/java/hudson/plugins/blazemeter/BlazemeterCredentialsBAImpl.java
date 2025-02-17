/**
 * Copyright 2018 BlazeMeter Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hudson.plugins.blazemeter;

import com.blazemeter.api.explorer.User;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import javax.annotation.CheckForNull;
import javax.validation.constraints.NotNull;
import hudson.Extension;
import hudson.Util;
import hudson.plugins.blazemeter.utils.JenkinsBlazeMeterUtils;
import hudson.util.FormValidation;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.util.Objects;

@SuppressWarnings("unused") // read resolved by extension plugins
public class BlazemeterCredentialsBAImpl extends BaseStandardCredentials implements BlazemeterCredentials, StandardUsernamePasswordCredentials {

    public static BlazemeterCredentialsBAImpl EMPTY = new BlazemeterCredentialsBAImpl(CredentialsScope.GLOBAL, "", "", "", "");
    /**
     * The username.
     */
    @NotNull
    private final String username;

    /**
     * The password.
     */
    @NotNull
    private final Secret password;

    /**
     * Constructor.
     *
     * @param scope       the credentials scope
     * @param id          the ID or {@code null} to generate a new one.
     * @param description the description.
     * @param username    the username.
     * @param password    the password.
     */
    @DataBoundConstructor
    @SuppressWarnings("unused") // by stapler
    public BlazemeterCredentialsBAImpl(@CheckForNull CredentialsScope scope,
                                       @CheckForNull String id, @CheckForNull String description,
                                       @CheckForNull String username, @CheckForNull String password) {
        super(scope, id, description);
        this.username = Util.fixNull(username);
        this.password = Secret.fromString(password);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    public Secret getPassword() {
        return password;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    public String getUsername() {
        return username;
    }

    /**
     * {@inheritDoc}
     */
    @Extension(ordinal = 1)
    public static class DescriptorImpl extends BaseStandardCredentialsDescriptor {

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return Messages.BlazemeterCredential_DisplayName();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getIconClassName() {
            return "icon-credentials-userpass";
        }

        public Boolean getAdministerStatus() {
            return Objects.requireNonNull(Jenkins.getInstance()).hasPermission(Jenkins.ADMINISTER);
        }

        public Boolean getManageCredentialsStatus() {
            Jenkins jenkins = Objects.requireNonNull(Jenkins.getInstance());
            return jenkins.hasPermission(CredentialsProvider.CREATE) ||
                    jenkins.hasPermission(CredentialsProvider.UPDATE) ||
                    jenkins.hasPermission(CredentialsProvider.DELETE) ||
                    jenkins.hasPermission(CredentialsProvider.MANAGE_DOMAINS) ||
                    jenkins.hasPermission(CredentialsProvider.VIEW);
        }
        
        public Boolean getProjectLevelCredentialsStatus() {
            hudson.model.User currentUser = Objects.requireNonNull(hudson.model.User.current());
            return currentUser.hasPermission(CredentialsProvider.CREATE) ||
                    currentUser.hasPermission(CredentialsProvider.UPDATE) ||
                    currentUser.hasPermission(CredentialsProvider.DELETE) ||
                    currentUser.hasPermission(CredentialsProvider.MANAGE_DOMAINS) ||
                    currentUser.hasPermission(CredentialsProvider.VIEW);
        }

        public Boolean isPrivilegedUser() {
            return getAdministerStatus() || getManageCredentialsStatus() || getProjectLevelCredentialsStatus();
        }

        public FormValidation doValidate(@QueryParameter("username") final String username,
                                         @QueryParameter("password") final String password) {
            String decryptedPassword = Secret.fromString(password).getPlainText();
            try {
                if (isPrivilegedUser()) {
                    JenkinsBlazeMeterUtils utils = BlazeMeterPerformanceBuilderDescriptor.getBzmUtils(username, decryptedPassword);
                    User.getUser(utils);
                    return FormValidation.ok("Successfully validated credentials.");
                } else {
                    return FormValidation.error("You don't have required privileges to add/update credentials.");
                }
            } catch (Exception e) {
                return FormValidation.error(e.getMessage());
            }
        }


    }
}