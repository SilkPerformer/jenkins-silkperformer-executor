package com.borland.jenkins.SilkPerformerJenkins;

import com.borland.jenkins.SilkPerformerJenkins.util.Config;
import com.borland.jenkins.SilkPerformerJenkins.util.SilkPerformerListBuilder;
import com.borland.jenkins.SilkPerformerJenkins.util.SilkPerformerTestManager;
import com.borland.jenkins.SilkPerformerJenkins.util.UserTypeItem;
import com.borland.jenkins.SilkPerformerJenkins.util.XMLReader;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

public class SilkPerformerBuilder extends Builder
{

  @Extension
  public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

  private final String projectLoc;
  private final List<SuccessCriteria> successCriteria;

  @DataBoundConstructor
  public SilkPerformerBuilder(String projectLoc, List<SuccessCriteria> successCriteria)
  {
    this.projectLoc = projectLoc;
    this.successCriteria = successCriteria;
  }

  public String getProjectLoc()
  {
    return projectLoc;
  }

  public List<SuccessCriteria> getSuccessCriteria()
  {
    return successCriteria;
  }

  @Override
  public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener)
  {
    SilkPerformerTestManager sptm;
    XMLReader spxml;

    if (!DESCRIPTOR.isInstallationDirValid())
    {
      listener.getLogger().println("The installation directory is not set!\nPlease go to Jenkin's configuration page and set the Silk Performer installation directory.");
      return false;
    }

    try
    {
      String projectLocation;
      if ("hudson.scm.NullSCM".equals(build.getProject().getScm().getKey()))
      {
        projectLocation = projectLoc;
      }
      else
      {
        projectLocation = build.getWorkspace().getRemote() + File.separator + projectLoc;
      }

      File resultsPath = new File(Jenkins.getInstance().root + File.separator + "jobs" + File.separator + build.getProject().getDisplayName() + File.separator + "builds"
          + File.separator + build.getNumber() + File.separator + "Results" + File.separator);
      listener.getLogger().println("Project location : " + projectLocation + "!");
      listener.getLogger().println("Results directory : " + resultsPath.getPath() + File.separator + "!");
      listener.getLogger().println("Initializing the LoadTests!");
      sptm = new SilkPerformerTestManager(projectLocation, resultsPath.getPath() + File.separator, DESCRIPTOR.getInstallationDir());
      sptm.startTheLoadTest();
      listener.getLogger().println("Completed LoadTests!");
      listener.getLogger().println("Detailed report location : " + resultsPath.getPath() + File.separator + "detailedReport.xml!");
      listener.getLogger().println("Processing Results!");
      spxml = new XMLReader();
      List<XMLReader.Agent> agentsList = spxml.readResults(resultsPath.getPath() + File.separator + "detailedReport.xml");
      if (agentsList == null)
      {
        listener.getLogger().println("Could not read detailedReport.xml!");
        return false;
      }
      return spxml.processResults(agentsList, successCriteria, listener); // returning
                                                                          // false
                                                                          // will
                                                                          // make
                                                                          // the
                                                                          // build
                                                                          // a
                                                                          // failure
    }
    catch (Exception e)
    {
      e.printStackTrace(listener.getLogger());
      return false;
    }
  }

  public static class DescriptorImpl extends BuildStepDescriptor<Builder>
  {

    private String installationDir;
    private final SilkPerformerListBuilder splb;
    private boolean bIsInstallationDirValid;

    public List<UserTypeItem> getUserTypeList()
    {
      return splb.getUserTypeList();
    }

    public List<String> getTransactionList()
    {
      return splb.getTransactionList();
    }

    public DescriptorImpl()
    {
      bIsInstallationDirValid = false;
      load();
      if (isInstallationDirValid())
      {
        try
        {
          Config.CheckAndSetClassPath(installationDir);
        }
        catch (Exception ex)
        {
          Logger.getLogger(SilkPerformerBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
      splb = new SilkPerformerListBuilder();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public boolean isApplicable(Class<? extends AbstractProject> jobType)
    {
      return true;
    }

    @Override
    public String getDisplayName()
    {
      return "Execute Silk Performer Tests";
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws Descriptor.FormException
    {
      installationDir = formData.getString("installationDir");
      if (isInstallationDirValid())
      {
        try
        {
          Config.CheckAndSetClassPath(installationDir);
        }
        catch (Exception ex)
        {
          Logger.getLogger(SilkPerformerBuilder.class.getName()).log(Level.SEVERE, null, ex);
          throw new Descriptor.FormException(ex.getMessage(), "installationDir");
        }
      }
      save();
      return true;
    }

    public FormValidation doCheckInstallationDir(@QueryParameter String value)
    {
      return (value == null || value.trim().isEmpty() || checkValidInstallationDirectory(value)) ? FormValidation.ok()
          : FormValidation.error("Not a valid Silk Performer installation directory!");
    }

    public FormValidation doCheckProjectLoc(@QueryParameter String value, @AncestorInPath AbstractProject project)
    {
      if (value == null || value.isEmpty())
      {
        return FormValidation.error("Project location is required.");
      }
      String path;
      if ("hudson.scm.NullSCM".equals(project.getScm().getKey()))
      {
        path = value;
      }
      else
      {
        path = new StringBuilder(project.getRootDir().getAbsolutePath()).append(File.separator).append("workspace").append(File.separator).append(value).toString();
      }
      String msg = "Not a valid Silk Performer project location!\nThe path to project is : " + path;

      return (checkValidProjectLocation(path)) ? FormValidation.ok() : FormValidation.error(msg);
    }

    public String getInstallationDir()
    {
      return installationDir;
    }

    public boolean isInstallationDirValid()
    {
      if (!bIsInstallationDirValid)
      {
        bIsInstallationDirValid = checkValidInstallationDirectory(installationDir);
      }
      return bIsInstallationDirValid;
    }

    public String fillListBuilder(final String workplaceLoc, final String projectLoc)
    {
      String errMsg;
      try
      {
        Config.CheckAndSetClassPath(installationDir);
      }
      catch (Exception ex)
      {
        errMsg = "Error setting classpath!";
        return errMsg;
      }
      if (isInstallationDirValid())
      {
        File f = new File(projectLoc);
        String path = (f.isAbsolute() ? projectLoc : workplaceLoc + File.separator + projectLoc);
        errMsg = splb.getInformation(path);
      }
      else
      {
        errMsg = "No SilkPerformer installation directory is set!";
      }
      return errMsg;
    }

    private boolean checkValidInstallationDirectory(String installationDirectory)
    {
      File f = new File(installationDirectory + File.separator + "perfLtcAgent.exe");
      return f.exists();
    }

    private boolean checkValidProjectLocation(String path)
    {
      File f = new File(path);
      return f.exists();
    }
  }
}
