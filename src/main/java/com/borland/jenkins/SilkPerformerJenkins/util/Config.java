package com.borland.jenkins.SilkPerformerJenkins.util;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Config
{

  private static final String[] jarsList = new String[] { "\\ClassFiles\\core.jar", "\\ClassFiles\\sgem.jar" };

  public static boolean CheckAndSetClassPath(String installationDir) throws Exception
  {
    if (installationDir == null)
    {
      Logger.getLogger(Config.class.getName()).log(Level.SEVERE, "No SilkPerformer installation directory is set!");
      return false;
    }

    addJarsToClassPath(installationDir);
    addDirToLibraryPathUsingJNA(installationDir);
    addDirToLibraryPath(installationDir);
    Logger.getLogger(Config.class.getName()).log(Level.INFO, "java.library.path : {0}", System.getProperty("java.library.path"));
    Logger.getLogger(Config.class.getName()).log(Level.INFO, "java.class.path : {0}", System.getProperty("java.class.path"));

    return true;
  }

  private static void addDirToLibraryPathUsingJNA(String installationDir) throws NativePlatformAccessException
  {
    String path = NativePlatformAccess.INSTANCE.getEnvironmentVariable("Path");
    if (!path.contains(installationDir))
    {
      path = installationDir + System.getProperty("path.separator") + path;
      Logger.getLogger(Config.class.getName()).log(Level.INFO,
          "Silk Performer installation direcotry is prepend with {0} in Path environment variable (for indirectly loaded dlls)", installationDir);
      NativePlatformAccess.INSTANCE.setEnvironmentVariable("Path", path);
    }
  }

  private static void addDirToLibraryPath(String path) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException
  {
    String libraryPath = System.getProperty("java.library.path");
    if (!libraryPath.contains(path))
    {
      libraryPath = path + System.getProperty("path.separator") + libraryPath;
      System.setProperty("java.library.path", libraryPath);
    }

    // set sys_paths to null so that java.library.path will be re-evaluated next
    // time it is needed
    final Field sysPathsField;
    sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
    sysPathsField.setAccessible(true);
    sysPathsField.set(null, null);
  }

  public static void addLibraryPath(String pathToAdd) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException
  {
    final Field usrPathsField;
    usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
    usrPathsField.setAccessible(true);

    // get array of paths
    final String[] paths = (String[]) usrPathsField.get(null);

    // check if the path to add is already present
    for (String path : paths)
    {
      if (path.equals(pathToAdd))
      {
        return;
      }
    }

    // add the new path
    final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
    newPaths[newPaths.length - 1] = pathToAdd;
    usrPathsField.set(null, newPaths);
  }


  private static void addJarsToClassPath(String installationDir)
      throws MalformedURLException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
  {
    URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
    method.setAccessible(true);
    for (String jar : jarsList)
    {
      method.invoke(sysloader, new Object[] { new File(installationDir + jar).toURI().toURL() });
    }
  }
}
