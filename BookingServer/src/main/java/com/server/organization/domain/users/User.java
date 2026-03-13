package com.server.organization.domain.users;
//the true business entity or aggregate root (we can add rules, updates)
//does not know about db, spring & rest , only business logic

import com.server.organization.domain.enums.AccountStatus;
import com.server.organization.domain.enums.GlobalRole;
import lombok.Getter;
import org.springframework.util.Assert;

@Getter
public class User {

    private int id;
    private UserEmail email;
    private UserPassword password;
    private String fullName;
    private GlobalRole globalRole;
    private AccountStatus accountStatus;

    public User(int id, UserEmail email, UserPassword password, String fullName, GlobalRole globalRole) {
        this(id, email, password, fullName, globalRole, AccountStatus.ACTIVE);
    }

    public User(int id, UserEmail email, UserPassword password, String fullName, GlobalRole globalRole, AccountStatus accountStatus) {
        Assert.notNull(email, "email must not be null");
        Assert.notNull(password, "password must not be null");
        Assert.notNull(fullName, "Full name must not be null");
        Assert.notNull(globalRole, "Role must not be null");
        Assert.notNull(accountStatus, "Account status must not be null");
        this.id = id;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.globalRole = globalRole;
        this.accountStatus = accountStatus;
    }

    public boolean isActive() {
        return accountStatus == AccountStatus.ACTIVE;
    }

    public void deactivate() {
        if (accountStatus == AccountStatus.DEACTIVATED) {
            throw new IllegalStateException("User is already deactivated");
        }
        this.accountStatus = AccountStatus.DEACTIVATED;
    }

    public void reactivate() {
        if (accountStatus == AccountStatus.ACTIVE) {
            throw new IllegalStateException("User is already active");
        }
        this.accountStatus = AccountStatus.ACTIVE;
    }

    public void changeFullName(String fullName) {
        Assert.notNull(fullName, "Full name must not be null");
        this.fullName = fullName;
    }

    public void changeEmail(UserEmail email) {
        Assert.notNull(email, "Email must not be null");
        this.email = email;
    }

    public void changeRole(GlobalRole role) {
        Assert.notNull(role, "Role must not be null");
        this.globalRole = role;
    }
}