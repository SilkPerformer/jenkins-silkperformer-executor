package com.borland.jenkins.SilkPerformerJenkins.util;

import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.W32Errors;

public class NativePlatformAccess
{
  public static final NativePlatformAccess INSTANCE = new NativePlatformAccess();

  private NativePlatformAccess()
  {

  }

  public void setEnvironmentVariable(String key, String value) throws NativePlatformAccessException
  {
    if (Platform.isWindows())
    {
      if (!Kernel32.INSTANCE.SetEnvironmentVariable(key, value))
      {
        throw new NativePlatformAccessException(getErrorMessageFromWinErrorCode(Kernel32.INSTANCE.GetLastError()));
      }
    }
  }

  private String getErrorMessageFromWinErrorCode(int errorCode)
  {
    return "Windows responded with the following error: \"" + Kernel32Util.formatMessage(W32Errors.HRESULT_FROM_WIN32(errorCode)) + "\" [Error:" + errorCode + ']';
  }

  public String getEnvironmentVariable(String key)
  {
    return Kernel32Util.getEnvironmentVariable(key);
  }
}
