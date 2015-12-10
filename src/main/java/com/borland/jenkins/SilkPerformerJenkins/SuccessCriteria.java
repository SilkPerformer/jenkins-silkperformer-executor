package com.borland.jenkins.SilkPerformerJenkins;

import com.borland.jenkins.SilkPerformerJenkins.util.UserTypeItem;
import com.borland.jenkins.SilkPerformerJenkins.util.XMLReader.Measure;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.util.ListBoxModel;
import java.util.List;
import org.kohsuke.stapler.DataBoundConstructor;

public class SuccessCriteria extends AbstractDescribableImpl<SuccessCriteria>
{
  private static final int eTimerMPClass = 2;
  private static final int eTrans = 5;
  private static final int eForm = 6;
  private static final int eSummary = 10;
  private static final int ePageTimer = 19;

  private static final int MEASURE_TIMER_ResponseTime = 3;
  private static final int MEASURE_TRANS_TransExecOk = 8;
  private static final int MEASURE_FORM_RoundTrip = 15;
  private static final int MEASURE_AGENT_GEN_CountError = 84;
  private static final int MEASURE_PAGE_PageTime = 131;
  private static final int MEASURE_PAGE_ActionTime = 151;

  public enum ListOfMeasures
  {
    ERRORS(eSummary, "Summary Report", MEASURE_AGENT_GEN_CountError, "Errors", "Summary General"), // eSummary,
                                                                                                   // MEASURE_AGENT_GEN_CountError
    TRANSACATION_BUSY_OK(eTrans, "Transaction", MEASURE_TRANS_TransExecOk, "Transaction Busy Time", "#Overall Response Time# or <name>"), // eTrans,
                                                                                                                                          // MEASURE_TRANS_TransExecOk,
                                                                                                                                          // "Trans.(busy)
                                                                                                                                          // ok[s]"
    PAGE_TIME(ePageTimer, "Page and Action Timer", MEASURE_PAGE_PageTime, "Page Time", "#Overall Response Time# or <name>"), // ePageTimer,
                                                                                                                             // MEASURE_PAGE_PageTime,
                                                                                                                             // "Page
                                                                                                                             // time[s]"
    ACTION_TIME(ePageTimer, "Page and Action Timer", MEASURE_PAGE_ActionTime, "Action Time", "#Overall Response Time# or <name>"), // ePageTimer,
                                                                                                                                   // MEASURE_PAGE_ActionTime,
                                                                                                                                   // "Action
                                                                                                                                   // time[s]"
    ROUND_TRIP_TIME(eForm, "Web Form", MEASURE_FORM_RoundTrip, "Form Response Time", "<Form name>"), // eForm,
                                                                                                     // MEASURE_FORM_RoundTrip
                                                                                                     // ,
                                                                                                     // "Round
                                                                                                     // trip
                                                                                                     // time[s]"
    RESPONSE_TIME(eTimerMPClass, "Timer", MEASURE_TIMER_ResponseTime, "Custom Timer", "<Measure name>"); // eTimerMPClass,
                                                                                                         // MEASURE_TIMER_ResponseTime,
                                                                                                         // "Response
                                                                                                         // time[s]"

    private final int iMeasureClass;
    private final int iMeasureType;
    private final String measureClass;
    private final String measureType;
    private final String measureName;

    ListOfMeasures(int iMeasureClass, String measureClass, int iMeasureType, String measureType, String measureName)
    {
      this.iMeasureClass = iMeasureClass;
      this.measureClass = measureClass;
      this.iMeasureType = iMeasureType;
      this.measureType = measureType;
      this.measureName = measureName;
    }

    public int getMeasureClass()
    {
      return iMeasureClass;
    }

    public String getMeasureClassDescription()
    {
      return measureClass;
    }

    public int getMeasureType()
    {
      return iMeasureType;
    }

    public String getMeasureTypeDescription()
    {
      return measureType;
    }

    public String getMeasureName()
    {
      return measureName;
    }

    @Override
    public String toString()
    {
      StringBuilder sb = new StringBuilder();
      sb.append("\n\tSelected measure : ").append("\n\tMeasure Class - ").append(measureClass).append(" - ").append(iMeasureClass).append("\n\tMeasure Type - ").append(measureType)
          .append(" - ").append(iMeasureType);
      return sb.toString();
    }

    public String toStringDebug()
    {
      StringBuilder sb = new StringBuilder();
      sb.append("\n\tSelected measure : ").append("\n\tMeasure Class - ").append(iMeasureClass).append("\n\tMeasure Class Description - ").append(measureClass)
          .append("\n\tMeasure Type - ").append(iMeasureType).append("\n\tMeasure Type Dsscription - ").append(measureType).append("\n\tMeasure Name - ").append(measureName);
      return sb.toString();
    }
  }

  private final String userType;
  private final String transactionName;
  private final String measureType;
  private final String measureName;
  private final String valueType;
  private final String operatorType;
  private final String chosenValue;

  @DataBoundConstructor
  public SuccessCriteria(String userType, String transactionName, String measureType, String measureName, String valueType, String operatorType, String chosenValue)
  {
    this.userType = userType;
    this.transactionName = transactionName;
    this.measureType = measureType;
    this.measureName = measureName;
    this.valueType = valueType;
    this.operatorType = operatorType;
    this.chosenValue = chosenValue;
  }

  public String getUserType()
  {
    return userType;
  }

  public String getTransactionName()
  {
    return transactionName;
  }

  public String getMeasureType()
  {
    return measureType;
  }

  public String getMeasureName()
  {
    return measureName;
  }

  public String getValueType()
  {
    return valueType;
  }

  public String getOperatorType()
  {
    return operatorType;
  }

  public String getChosenValue()
  {
    return chosenValue;
  }

  public boolean isSelectedMeasure(Measure m, BuildListener listener)
  {
    ListOfMeasures mm = getSelectedMeasure();
    if (mm != null && measureName.toUpperCase().equals(m.getName().toUpperCase()) && mm.getMeasureClass() == m.getMeasureClass() && mm.getMeasureType() == m.getMeasureType())
    {
      return true;
    }

    return false;
  }

  private ListOfMeasures getSelectedMeasure()
  {
    for (ListOfMeasures m : ListOfMeasures.values())
    {
      if (m.getMeasureTypeDescription().equals(getMeasureType()))
      {
        return m;
      }
    }

    return null;
  }

  public String toStringLog()
  {
    ListOfMeasures m = getSelectedMeasure();
    return (m == null) ? "Cannot find selected measure for " + this.toString() : m.toString() + "\n\tMeasure Name: " + measureName;
  }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    sb.append("\n\tUser Type: ").append(userType).append("\n\tTransaction Name: ").append(transactionName).append("\n\tExpression: ").append(measureType).append("(")
        .append(valueType).append(")").append(" ").append(operatorType).append(" ").append(chosenValue);

    return sb.toString();
  }

  @Extension
  public static class DescriptorImpl extends Descriptor<SuccessCriteria>
  {

    private final List<String> transactions;
    private final List<UserTypeItem> userTypes;

    public DescriptorImpl()
    {
      super(SuccessCriteria.class);
      transactions = SilkPerformerBuilder.DESCRIPTOR.getTransactionList();
      userTypes = SilkPerformerBuilder.DESCRIPTOR.getUserTypeList();
    }

    @Override
    public String getDisplayName()
    {
      return "Success Criteria";
    }

    public ListBoxModel doFillUserTypeItems()
    {
      ListBoxModel items = new ListBoxModel();
      items.add("All", "all");
      for (UserTypeItem uti : userTypes)
      {
        items.add(uti.toString(), uti.toString());
      }
      return items;
    }

    public ListBoxModel doFillTransactionNameItems()
    {
      ListBoxModel items = new ListBoxModel();
      items.add("All", "all");
      for (String transaction : transactions)
      {
        items.add(transaction, transaction);
      }
      return items;
    }

    public ListBoxModel doFillMeasureTypeItems()
    {
      ListBoxModel items = new ListBoxModel();
      for (ListOfMeasures m : ListOfMeasures.values())
      {
        items.add(m.getMeasureTypeDescription(), m.getMeasureTypeDescription());
      }
      return items;
    }


    public ListBoxModel doFillOperatorTypeItems()
    {
      ListBoxModel items = new ListBoxModel();
      items.add("<", "<");
      items.add(">", ">");
      items.add("<=", "<=");
      items.add(">=", ">=");
      items.add("=", "=");
      return items;
    }

    public ListBoxModel doFillValueTypeItems()
    {
      ListBoxModel items = new ListBoxModel();
      items.add("Minimum Value", "Minimum Value");
      items.add("Maximum Value", "Maximum Value");
      items.add("Average Value", "Average Value");
      return items;
    }
  }

}
