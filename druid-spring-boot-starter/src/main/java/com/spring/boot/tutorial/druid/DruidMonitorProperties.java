package com.spring.boot.tutorial.druid;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author cheny.huang
 * @date 2018-08-15 10:27
 */
@ConfigurationProperties(prefix = "spring.druid.monitor")
public class DruidMonitorProperties {

   private String druidStatView;
   private String druidWebStatFilter;

   private String allow;
   private String deny;
   private String loginUsername;
   private String loginPassword;

   private String exclusions;
   private String resetEnable;

    public String getDruidStatView() {
        return druidStatView;
    }

    public void setDruidStatView(String druidStatView) {
        this.druidStatView = druidStatView;
    }

    public String getDruidWebStatFilter() {
        return druidWebStatFilter;
    }

    public void setDruidWebStatFilter(String druidWebStatFilter) {
        this.druidWebStatFilter = druidWebStatFilter;
    }

    public String getAllow() {
        return allow;
    }

    public void setAllow(String allow) {
        this.allow = allow;
    }

    public String getDeny() {
        return deny;
    }

    public void setDeny(String deny) {
        this.deny = deny;
    }

    public String getLoginUsername() {
        return loginUsername;
    }

    public void setLoginUsername(String loginUsername) {
        this.loginUsername = loginUsername;
    }

    public String getLoginPassword() {
        return loginPassword;
    }

    public void setLoginPassword(String loginPassword) {
        this.loginPassword = loginPassword;
    }

    public String getExclusions() {
        return exclusions;
    }

    public void setExclusions(String exclusions) {
        this.exclusions = exclusions;
    }

    public String getResetEnable() {
        return resetEnable;
    }

    public void setResetEnable(String resetEnable) {
        this.resetEnable = resetEnable;
    }
}
