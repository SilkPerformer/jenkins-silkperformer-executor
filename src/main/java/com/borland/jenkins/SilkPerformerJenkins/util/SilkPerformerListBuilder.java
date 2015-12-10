package com.borland.jenkins.SilkPerformerJenkins.util;

import com.segue.em.Projectfile;
import com.segue.em.SGExecutionManager;
import com.segue.em.projectfile.TransactionInfo;
import com.segue.em.projectfile.UserType;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class SilkPerformerListBuilder
{

  private final List<UserTypeItem> userTypeList;
  private final List<String> transactionList;

  public SilkPerformerListBuilder()
  {
    this.userTypeList = new ArrayList<UserTypeItem>();
    this.transactionList = new ArrayList<String>();
  }

  public List<UserTypeItem> getUserTypeList()
  {
    return userTypeList;
  }

  public List<String> getTransactionList()
  {
    return transactionList;
  }

  public String getInformation(String projectLoc)
  {
    String toRet = "";
    UserTypeItem currUserTypeItem;
    try
    {
      transactionList.clear();
      userTypeList.clear();

      Projectfile prjf = SGExecutionManager.openProject(projectLoc);
      if (prjf == null) {
        
        return toRet;
      } 
      String namewl = prjf.getActiveWorkload();
      prjf.setCurrentWorkload(namewl);
      UserType itut = prjf.getFirstUserType();
      while (itut != null)
      {
        currUserTypeItem = new UserTypeItem();
        currUserTypeItem.setProfileName(itut.getProfileName());
        currUserTypeItem.setScriptName(itut.getScriptName());
        currUserTypeItem.setUsergroupName(itut.getUsergroupName());
        userTypeList.add(currUserTypeItem);
        itut = prjf.getNextUserType();
      }
      Iterator<TransactionInfo> itti = prjf.getTransactionInfo();
      while (itti.hasNext())
      {
        TransactionInfo ti = itti.next();
        String currTransactionItem = ti.getTransactionName();
        transactionList.add(currTransactionItem);
      }
      prjf.close();
    }
    catch (Exception e)
    {
      toRet = e.getMessage() + " - " + projectLoc;
      transactionList.clear();
      userTypeList.clear();

      transactionList.add("Error!");
      currUserTypeItem = new UserTypeItem();
      currUserTypeItem.setProfileName("Error!");
      currUserTypeItem.setScriptName("");
      currUserTypeItem.setUsergroupName("");
      userTypeList.add(currUserTypeItem);
    }
    return toRet;
  }
}
