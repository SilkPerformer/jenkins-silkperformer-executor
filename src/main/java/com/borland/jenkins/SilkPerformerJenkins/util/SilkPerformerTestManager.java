package com.borland.jenkins.SilkPerformerJenkins.util;

import com.segue.em.Ltc;
import com.segue.em.Projectfile;
import com.segue.em.SGExecutionManager;
import java.util.Iterator;
import com.segue.em.projectfile.TransactionInfo;
import com.segue.em.projectfile.UserType;
import com.segue.em.remote.ISGExecutionManager;

public class SilkPerformerTestManager
{

  private final String projectLoc;
  private final String resultDir;
  private final String installationDir;

  public SilkPerformerTestManager(String projectLoc, String resultDir, String installationDir) throws Exception
  {
    this.projectLoc = projectLoc;
    this.resultDir = resultDir;
    this.installationDir = installationDir;
  }

  public void startTheLoadTest() throws Exception
  {
    String configLoc = installationDir + "/sgExecManagerLtc.xml";
    Projectfile prjf = SGExecutionManager.openProject(projectLoc);
    prjf.setResultsDir(resultDir);
    prjf.save();
    prjf.close();
    Ltc ltc = SGExecutionManager.createController(configLoc);
    ltc.setDeployBuildOption(ISGExecutionManager.BUILD_METHOD_REBUILD);
    ltc.deployProject(projectLoc);
    ltc.start(-1);
    ltc.undeploy();
    ltc.destroy();
    SGExecutionManager.destroyController(ltc);
  }

  public String getInformation() throws Exception
  {
    StringBuilder result = new StringBuilder();
    Projectfile prjf = SGExecutionManager.openProject(projectLoc);
    String namewl = prjf.getActiveWorkload();
    prjf.setCurrentWorkload(namewl);
    UserType itut = prjf.getFirstUserType();
    while (itut != null)
    {
      result.append(itut.getScriptName());
      result.append("/");
      result.append(itut.getUsergroupName());
      result.append("/");
      result.append(itut.getProfileName());
      result.append("\n");
      itut = prjf.getNextUserType();
    }
    Iterator<TransactionInfo> itti = prjf.getTransactionInfo();
    while (itti.hasNext())
    {
      TransactionInfo ti = itti.next();
      result.append(ti.getTransactionName());
      result.append("\n");
    }
    prjf.close();
    return result.toString();
  }
}
