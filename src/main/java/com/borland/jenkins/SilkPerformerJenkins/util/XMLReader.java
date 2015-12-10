package com.borland.jenkins.SilkPerformerJenkins.util;

import com.borland.jenkins.SilkPerformerJenkins.SuccessCriteria;
import hudson.model.BuildListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class XMLReader
{

  public List<Agent> readResults(String resultDir) throws XMLStreamException
  {
    List<Agent> agentsList = null;
    Agent currAgent = null;
    UserTypes currUserTypes = null;
    Measure currMeasure = null;
    String tagContent = null;
    InputStream xmldata = null;
    try
    {
      xmldata = Files.newInputStream(Paths.get(resultDir));
    }
    catch (Exception e)
    {
      return agentsList;
    }
    XMLInputFactory factory = XMLInputFactory.newInstance();
    XMLStreamReader reader = factory.createXMLStreamReader(xmldata);
    while (reader.hasNext())
    {
      int event = reader.next();
      switch (event)
      {
        case XMLStreamConstants.START_ELEMENT:
          if ("AgentList".equals(reader.getLocalName()))
          {
            agentsList = new ArrayList<Agent>();
          }
          if ("Agent".equals(reader.getLocalName()))
          {
            currAgent = new Agent();
            currAgent.mName = reader.getAttributeValue(0);
          }
          if ("UserTypeList".equals(reader.getLocalName()))
          {
            currAgent.mUserTypes = new ArrayList<UserTypes>();
          }
          if ("UserType".equals(reader.getLocalName()))
          {
            currUserTypes = new UserTypes();
            currUserTypes.mName = reader.getAttributeValue(0);
            currUserTypes.mProfile = reader.getAttributeValue(1);
            currUserTypes.mScript = reader.getAttributeValue(2);
          }
          if ("MeasureList".equals(reader.getLocalName()))
          {
            currUserTypes.mMeasure = new ArrayList<Measure>();
          }
          if ("Measure".equals(reader.getLocalName()))
          {
            currMeasure = new Measure();
            currMeasure.mMeasureClass = Integer.parseInt(reader.getAttributeValue(0));
            currMeasure.mMeasureType = Integer.parseInt(reader.getAttributeValue(1));
            currMeasure.mName = reader.getAttributeValue(2);
          }
          break;
        case XMLStreamConstants.CHARACTERS:
          tagContent = reader.getText().trim();
          break;
        case XMLStreamConstants.END_ELEMENT:
          if (reader.getLocalName().equals("Agent"))
          {
            agentsList.add(currAgent);
          }
          if (reader.getLocalName().equals("UserType"))
          {
            currAgent.mUserTypes.add(currUserTypes);
          }
          if (reader.getLocalName().equals("Measure"))
          {
            currMeasure.mAvg = round((currMeasure.mSum / currMeasure.mCountMeasured), 2);
            currUserTypes.mMeasure.add(currMeasure);
          }
          if (reader.getLocalName().equals("DisplayType"))
          {
            currMeasure.mDisplayType = tagContent;
          }
          if (reader.getLocalName().equals("Min"))
          {
            currMeasure.mMin = round(Double.parseDouble(tagContent), 2);
          }
          if (reader.getLocalName().equals("Max"))
          {
            currMeasure.mMax = round(Double.parseDouble(tagContent), 2);
          }
          if (reader.getLocalName().equals("Sum"))
          {
            currMeasure.mSum = round(Double.parseDouble(tagContent), 2);
            ;
          }
          if (reader.getLocalName().equals("CountMeasured"))
          {
            currMeasure.mCountMeasured = Integer.parseInt(tagContent);
          }
          if (reader.getLocalName().equals("Unit"))
          {
            currMeasure.mUnit = tagContent;

          }
          break;
        case XMLStreamConstants.START_DOCUMENT:
          // empList = new ArrayList<>();
          break;
      }
    }
    return agentsList;
  }

  private double round(double num, int places)
  {
    if (places < 0)
    {
      throw new IllegalArgumentException();
    }

    long factor = (long) Math.pow(10, places);
    num = num * factor;
    long tmp = Math.round(num);
    return (double) tmp / factor;
  }

  private boolean expressionReader(Double val1, String op, String val2)
  {
    if (op.equals("<"))
    {
      return (val1 < Double.valueOf(val2));
    }
    else if (op.equals(">"))
    {
      return (val1 > Double.valueOf(val2));
    }
    else if (op.equals("<="))
    {
      return (val1 <= Double.valueOf(val2));
    }
    else if (op.equals(">="))
    {
      return (val1 >= Double.valueOf(val2));
    }
    else if (op.equals("="))
    {
      return (val1.equals(Double.valueOf(val2)));
    }
    return false;
  }

  public boolean processResults(List<Agent> agentsList, List<SuccessCriteria> successCriteria, BuildListener listener)
  {
    if (successCriteria == null)
    {
      return true;
    }
    if (agentsList == null)
    {
      return false;
    }
    for (SuccessCriteria sc : successCriteria)
    {
      for (Agent ag : agentsList)
      {
        for (UserTypes ut : ag.mUserTypes)
        {
          if (sc.getUserType().equals(ut.mScript + "/" + ut.mName + "/" + ut.mProfile) || sc.getUserType().equals("all"))
          {
            if (!checkMeasures(sc, ut, listener))
            {
              return false;
            }
          }
        }
      }
    }
    return true;
  }

  private boolean checkMeasures(SuccessCriteria sc, UserTypes ut, BuildListener listener)
  {
    boolean bMeasureFound = false;
    for (Measure m : ut.mMeasure)
    {
      if (sc.getTransactionName().equals("all") || sc.getTransactionName().equals(m.mName))
      {
        if (sc.isSelectedMeasure(m, listener))
        {
          bMeasureFound = true;
          if (sc.getValueType().equals("Minimum Value"))
          {
            if (!expressionReader(m.mMin, sc.getOperatorType(), sc.getChosenValue()))
            {
              listener.getLogger().println(formatSuccessCriteria(ut, sc, m.mMin));
              return false;
            }
          }
          else if (sc.getValueType().equals("Maximum Value"))
          {
            if (!expressionReader(m.mMax, sc.getOperatorType(), sc.getChosenValue()))
            {
              listener.getLogger().println(formatSuccessCriteria(ut, sc, m.mMax));
              return false;
            }
          }
          else if (sc.getValueType().equals("Average Value"))
          {
            if (!expressionReader(m.mAvg, sc.getOperatorType(), sc.getChosenValue()))
            {
              listener.getLogger().println(formatSuccessCriteria(ut, sc, m.mAvg));
              return false;
            }
          }
          return true;
        }
      }
    }

    if (!bMeasureFound)
    {
      StringBuilder sb = new StringBuilder("Measure not found for :\n");
      sb.append(ut.toStringLog());
      sb.append(sc.toStringLog());
      listener.getLogger().println(sb.toString());
    }

    return bMeasureFound;
  }

  private String formatSuccessCriteria(UserTypes ut, SuccessCriteria sc, Double dFoundValue)
  {
    StringBuilder sb = new StringBuilder("Success Criteria failed!\n");
    sb.append(sc.toString());
    sb.append("\nFound value is : ").append(dFoundValue);
    return sb.toString();
  }

  public String printResults(List<Agent> agentsList)
  {
    StringBuilder result = new StringBuilder();

    result.append("Results: \n");
    for (Agent ag : agentsList)
    {
      result.append(ag.toString());
    }

    return result.toString();
  }

  public class Agent
  {

    private String mName;
    private List<UserTypes> mUserTypes;

    public String getName()
    {
      return mName;
    }

    public List<UserTypes> getUserTypes()
    {
      return mUserTypes;
    }

    @Override
    public String toString()
    {
      StringBuilder sb = new StringBuilder();

      sb.append("Agent: ").append(mName).append("\n");
      for (UserTypes ut : mUserTypes)
      {
        sb.append(ut.toString());
      }

      return sb.toString();
    }
  }

  public class UserTypes
  {

    private String mName;
    private String mProfile;
    private String mScript;
    private List<Measure> mMeasure;

    public String getName()
    {
      return mName;
    }

    public String getProfile()
    {
      return mProfile;
    }

    public String getScript()
    {
      return mScript;
    }

    public List<Measure> getMeasure()
    {
      return mMeasure;
    }

    @Override
    public String toString()
    {
      StringBuilder sb = new StringBuilder();

      sb.append("\tUserType: ");
      sb.append(mScript);
      sb.append("/");
      sb.append(mName);
      sb.append("/");
      sb.append(mProfile);
      sb.append("\n");
      for (Measure m : mMeasure)
      {
        sb.append("\t\t");
        sb.append(m.toString());
        sb.append("\n\n");
      }

      return sb.toString();
    }

    public String toStringLog()
    {
      StringBuilder sb = new StringBuilder();
      sb.append("\tUserType: ");
      sb.append(mScript);
      sb.append("/");
      sb.append(mName);
      sb.append("/");
      sb.append(mProfile);
      return sb.toString();
    }
  }

  public class Measure
  {

    private String mName;
    private String mDisplayType;
    private Double mMin;
    private Double mMax;
    private Double mAvg;
    private int mCountMeasured;
    private Double mSum;
    private String mUnit;
    private int mMeasureClass;
    private int mMeasureType;

    public String getName()
    {
      return mName;
    }

    public String getDisplayType()
    {
      return mDisplayType;
    }

    public Double getMin()
    {
      return mMin;
    }

    public Double getMax()
    {
      return mMax;
    }

    public Double getAvg()
    {
      return mAvg;
    }

    public String getUnit()
    {
      return mUnit;
    }

    public int getMeasureClass()
    {
      return mMeasureClass;
    }

    public int getMeasureType()
    {
      return mMeasureType;
    }

    @Override
    public String toString()
    {
      StringBuilder sb = new StringBuilder("Measure Name: ");
      sb.append(mName).append("\n\t\t\t Display Name: ").append(mDisplayType).append("\n\t\t\t Measure Class: ").append(mMeasureClass).append("\n\t\t\t Measure Type: ")
          .append(mMeasureType).append("\n\t\t\t Count Measured: ").append(mCountMeasured).append("\n\t\t\t Sum of Measures: ").append(mSum).append("\n\t\t\t Minimum Value: ")
          .append(mMin).append("\n\t\t\t Maximum Value: ").append(mMax).append("\n\t\t\t Average Value: ").append(mAvg).append("\n\t\t\t Unit of the values: ").append(mUnit);

      return sb.toString();
    }
  }
}
