package com.adata.it.setApproverFromP3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import com.agile.api.APIException;
import com.agile.api.AgileSessionFactory;
import com.agile.api.CommonConstants;
import com.agile.api.IAgileClass;
import com.agile.api.IAgileSession;
import com.agile.api.IAttribute;
import com.agile.api.IChange;
import com.agile.api.IDataObject;
import com.agile.api.IProperty;
import com.agile.api.IQualityChangeRequest;
import com.agile.api.IQuery;
import com.agile.api.IRow;
import com.agile.api.ITableDesc;
import com.agile.api.IUser;
import com.agile.api.PropertyConstants;
import com.agile.api.TableTypeConstants;
import com.agile.api.UserConstants;

public class setApproverTestInJava2 {

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
			

				
				String v_msg = "";
				
				//IDataObject affectedObject = info.getDataObject();
				//IChange change = (IChange)affectedObject;
				
				System.out.println("change No: " +  change);
				
				
				//IAgileClass cls = change.getAgileClass();
				
				//ArrayList result = new ArrayList();
				
				// Check if the class is abstract or concrete
				if (!cls.isAbstract()) {
					IAttribute[] attrs = null;

					//obj.logMonitor(cls.getName());	
					
					ArrayList identifyUsersList = new ArrayList();   
					
					//Get the attributes for Page Three
					ITableDesc page3 = cls.getTableDescriptor(TableTypeConstants.TYPE_PAGE_THREE);
					if (page3 != null) {
						attrs = page3.getAttributes();
						
						// 抓出下一關的關卡名稱 (name 欄位)
						//String v_next_status = "::" + change.getDefaultNextStatus().toString()+ "::";					
						//System.out.println(change.getDefaultNextStatus());					
						
						String v_next_status = "::" + change.getStatus().toString() + "::" ;
						System.out.println(change.getStatus());
						
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
										  StringTokenizer st = new StringTokenizer(identifyUsers,";");
										  while (st.hasMoreTokens()) {
											 //identifyUsersList.add(st.nextToken());
											  
											  
											  String v_userid = st.nextToken();
											  
											  String v_true_id = "";
											  
											  
											  //v_userid=v_userid.substring(v_userid.indexOf("(")+1);
											  //v_userid = v_userid.substring(0,v_userid.length()-1);
											  //System.out.println(v_userid);
											  
											  //20141216
											  //透過user.p2上的multilist01欄位，判斷屬於哪個帳號
											  //此欄位的維護，1對1 與 1對多，都要維護
											  
											  //IQuery query = (IQuery)session.createObject(IQuery.OBJECT_TYPE,
									          //          "SELECT [General Info.USER ID] " +
									          //          " FROM [Users] " 
									                    //" WHERE [] contains any '" +  v_userid + "'"
									                    //不判斷active，因為即使是inactive也可被加入，只是無法登入，再去抓問題
									                    //避免前端覺得選到了，卻沒人被加入會簽
									                    //" and [General Info.Status] == 'Active'"
									         //           );
											  
											 IQuery query = (IQuery)session.createObject(IQuery.OBJECT_TYPE, UserConstants.CLASS_USERS_CLASS); 										  
											 query.setCaseSensitive(false);
											 query.setCriteria("[2090] contains any '" +  v_userid + "'");
											 Iterator ii = query.execute().iterator();		        
											 if (ii.hasNext()) {
												 
												 System.out.println("got it");
												 IRow   row    = (IRow)ii.next(); 
												 v_true_id = row.getValue(UserConstants.ATT_GENERAL_INFO_USER_ID).toString();
												 System.out.println(v_true_id);
											 }
												  
											 System.out.println("x: "+ v_userid);
											 
											 //todo : 要再加判斷，如果此aduser對應不到帳號的err msg
											 
											 IUser user;
											 if (!(v_true_id.equals(""))){
												  
											   user = (IUser)session.getObject(IUser.OBJECT_TYPE,v_true_id);
											 }else{
												  user = (IUser)session.getObject(IUser.OBJECT_TYPE,"admin");
											 }
												 
											  IUser[] approvers = new IUser[]{user};
											  
											  try{
												  System.out.println("add " + user.toString() + " to " + change.getStatus().toString());
												  change.addApprovers(change.getStatus(), approvers, null, true,"");
												  v_msg = v_msg + "add [" + user.toString() + "] to [" + change.getStatus().toString() + "]; ";
												  if (v_true_id.equals("")){
													  v_msg = v_msg + "(因" + v_userid + "對應帳號不存在)" ;
												  }
												  
												  //System.out.println("add " + user.toString() + " to " + change.getDefaultNextStatus().toString());
												  //change.addApprovers(change.getDefaultNextStatus(), approvers, null, true,"");											  
												  //v_msg = v_msg + "add [" + user.toString() + "] to [" + change.getDefaultNextStatus().toString() + "]; ";
											  }catch(APIException e) {
												  System.out.println(e.getMessage());
												  v_msg = v_msg + e.getMessage();
												  
												  if(!(e.getMessage().indexOf("Duplicate name entered")>-1)){
													 // throw new Exception (e.getMessage());
													  System.out.println(e.getMessage());
												  }
												  
											  }
											  
											 
												 
											  
											  										  
										  }
										//***************
									   }   
									
									}
								
							
								}
							
							}
						
						}
					}
			
			
			
			
			System.out.println(resultStr);
			
			session.close();
					
		} catch (APIException e){
			e.printStackTrace();
		}
	}

}
