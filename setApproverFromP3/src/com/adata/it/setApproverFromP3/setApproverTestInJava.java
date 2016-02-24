package com.adata.it.setApproverFromP3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import com.agile.api.APIException;
import com.agile.api.AgileSessionFactory;
import com.agile.api.CommonConstants;
import com.agile.api.IAgileClass;
import com.agile.api.IAgileSession;
import com.agile.api.IAttribute;
import com.agile.api.IChange;
import com.agile.api.IProperty;
import com.agile.api.IQualityChangeRequest;
import com.agile.api.ITableDesc;
import com.agile.api.IUser;
import com.agile.api.PropertyConstants;
import com.agile.api.TableTypeConstants;

public class setApproverTestInJava {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{
			HashMap<Integer, String> params = new HashMap<Integer, String>();
			params.put(AgileSessionFactory.USERNAME, "plmadmin01");
			params.put(AgileSessionFactory.PASSWORD, "agile933");
			params.put(AgileSessionFactory.URL,"http://plmtest03.adata.com/Agile");
			IAgileSession session = AgileSessionFactory.createSessionEx(params);
			System.out.println(session.getCurrentUser()+"login success.");

			String resultStr = "";
			
			//IQualityChangeRequest change = (IQualityChangeRequest)session.getObject(IQualityChangeRequest.OBJECT_TYPE, "CCAR-000019");
			IChange change = (IChange)session.getObject(IChange.OBJECT_TYPE, "CR-0000726");
			System.out.println(change);
			
			
			IAgileClass cls = change.getAgileClass();
			
			ArrayList result = new ArrayList();
			
			// Check if the class is abstract or concrete
			if (!cls.isAbstract()) {
				
				//20150112:CCAR
				String cls_apiname = cls.getAPIName();
				String v_reg = "Y";	//car上的「立案狀態」欄，若是N，就不把相關會簽人員帶入(但其它表單無此欄位，要預設Y)
				System.out.println(cls_apiname);
				//CCAR的api name : CustomerCorrectiveActionRequestApplicationForm
				if (cls_apiname.equals("CustomerCorrectiveActionRequestApplicationForm")){
					v_reg = change.getValue(CommonConstants.ATT_PAGE_THREE_LIST02).toString();
					if (v_reg.equals("不立案")){
						v_reg = "N";
					}
					System.out.println(v_reg);
				}
				
				
				//if (cls_apiname.equals("CustomerCorrectiveActionRequestApplicationForm") && v_reg.equals("Y") || (!(cls_apiname.equals("CustomerCorrectiveActionRequestApplicationForm")))){
					//***
					
					IAttribute[] attrs = null;

					//obj.logMonitor(cls.getName());	
					
					ArrayList identifyUsersList = new ArrayList();   
					
					//Get the attributes for Page Three
					ITableDesc page3 = cls.getTableDescriptor(TableTypeConstants.TYPE_PAGE_THREE);
					if (page3 != null) {
						attrs = page3.getAttributes();
						
						// 抓出下一關的關卡名稱 (name 欄位)
						//String next_status = change.getDefaultNextStatus().toString();
						
						//System.out.println(change.getStatus());
						//System.out.println(change.getDefaultNextStatus());
						
						resultStr = resultStr + change.getDefaultNextStatus();
						
						String v_next_status = "::" + change.getStatus().toString() + "::" ;
						//String v_next_status = "::" + change.getDefaultNextStatus().toString() + "::" ;
						
						for (int i = 0; i < attrs.length; i++) {
						
							IAttribute attr = attrs[i];				
							
							
							if (attr.isVisible()) {


								// 抓出 Description 欄位
								// 若Description 欄，為[SetApprover]開頭，表示此欄用來指定關卡簽核人員
								// [SetApprover]::REVIEW::	<--- 指定為REVIEW關卡的簽核人員
								// [SetApprover]::ME::PUR::	<--- 指定為ME和PUR關卡的簽核人員
								// 一個欄位可用來指定多關關卡
								
								IProperty v_desc = attr.getProperty(PropertyConstants.PROP_DESCRIPTION);							
								String v_desc_string = v_desc.toString();
								
								
								// 會簽關卡欄位，只會是LIST 、 multilist 二種型態之一					
								
								if ((attr.getDataType() == 4 || attr.getDataType() == 5 ) && v_desc_string.indexOf("[SetApprover]") != -1 && v_desc_string.indexOf(v_next_status) != -1) {
									//抓api name
															
									//抓出欄位值，當簽核人員
									attr.getId();
									String identifyUsers = change.getValue(attr.getId()).toString();
									
									System.out.println(attr.getId());

									
									 if(identifyUsers != null && identifyUsers.length() > 0)
									   {
										 
										 resultStr = resultStr + attr.getId().toString() + identifyUsers;
										 
										  StringTokenizer st = new StringTokenizer(identifyUsers,";");
										  while (st.hasMoreTokens()) {
											 //identifyUsersList.add(st.nextToken());
											  
											  
											  String v_userid = st.nextToken();
											  System.out.println(v_userid );
											  //20150120 把中文名去掉
											  v_userid = (v_userid.indexOf("(") >-1 ) ? v_userid.substring(0, v_userid.indexOf("(")-1) : v_userid;
											  System.out.println(v_userid );
											  
											  System.out.println(v_userid);
											  
											  //v_userid=v_userid.substring(v_userid.indexOf("(")+1);
											  //v_userid = v_userid.substring(0,v_userid.length()-1);
											  //System.out.println(v_userid);
											  
											  v_userid = v_userid.substring(0,v_userid.indexOf("|"));
											  
											  
											  
											  System.out.println(v_userid);

											  IUser user = (IUser)session.getObject(IUser.OBJECT_TYPE,v_userid);
											  IUser[] approvers = new IUser[]{user};
											  
											  try{
												  change.addApprovers(change.getStatus(), approvers, null, true,"");  
												  //change.addApprovers(change.getDefaultNextStatus(), approvers, null, true,"");
											  }catch(APIException e) {
												 // e.printStackTrace();
											  }
											  
											  //System.out.println(change.getStatus());
												
										  }
									   }   
									
								}
								
							
							}
							
						}
						
					}
					//***
					
				//}
			}
			
			IUser user_admin = (IUser)session.getObject(IUser.OBJECT_TYPE,"admin");
			  IUser[] approvers_admin = new IUser[]{user_admin};
			  
			  try{
				  //change.addApprovers(change.getStatus(), approvers, null, true,"");  
				  change.removeApprovers(change.getDefaultNextStatus(), approvers_admin, null, "custom : Remove admin from workflow");
				  
				  
				  
				  System.out.println(change.getDefaultNextStatus() + "admin: " + approvers_admin[0].getName().toString());
				  
			  }catch(APIException e) {
				  //e.printStackTrace();
			  }
			
			System.out.println(resultStr);
			
			session.close();
					
		} catch (APIException e){
			e.printStackTrace();
		}
	}

}
