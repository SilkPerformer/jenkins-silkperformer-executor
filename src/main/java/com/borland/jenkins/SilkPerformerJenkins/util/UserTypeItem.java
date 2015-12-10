package com.borland.jenkins.SilkPerformerJenkins.util;

public class UserTypeItem
{

  private String mProfileName;
  private String mScriptName;
  private String mUsergroupName;

  public String getProfileName()
  {
    return mProfileName;
  }

  public String getScriptName()
  {
    return mScriptName;
  }

  public String getUsergroupName()
  {
    return mUsergroupName;
  }

  public void setProfileName(String mProfileName)
  {
    this.mProfileName = mProfileName;
  }

  public void setScriptName(String mScriptName)
  {
    this.mScriptName = mScriptName;
  }

  public void setUsergroupName(String mUsergroupName)
  {
    this.mUsergroupName = mUsergroupName;
  }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    sb.append(mScriptName).append("/").append(mUsergroupName).append("/").append(mProfileName);

    return sb.toString();
  }
}
