package com.adata.it.setApproverFromP3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import com.agile.api.APIException;
import com.agile.api.CommonConstants;
import com.agile.api.IAgileClass;
import com.agile.api.IAgileSession;
import com.agile.api.IAttribute;
import com.agile.api.IDataObject;
import com.agile.api.INode;
import com.agile.api.IProperty;
import com.agile.api.IQualityChangeRequest;
import com.agile.api.IQuery;
import com.agile.api.IRow;
import com.agile.api.ITableDesc;
import com.agile.api.IUser;
import com.agile.api.PropertyConstants;
import com.agile.api.TableTypeConstants;
import com.agile.api.UserConstants;
import com.agile.px.ActionResult;
import com.agile.px.EventActionResult;
import com.agile.px.IEventAction;
import com.agile.px.IEventInfo;
import com.agile.px.IObjectEventInfo;

public class setApproveEventQCR_LOV2ID  implements IEventAction {

	public EventActionResult doAction(IAgileSession session, INode actionNode, IEventInfo request) {
		IObjectEventInfo info = (IObjectEventInfo) request;
		
		
		try {
			String v_msg = "";
			
			IDataObject affectedObject = info.getDataObject();
			IQualityChangeRequest change = (IQualityChangeRequest) affectedObject;

			System.out.println("QCR No: " + change);
			
			
			IAgileClass cls = change.getAgileClass();
			
			ArrayList result = new ArrayList();
			
			// Check if the class is abstract or concrete
			if (!cls.isAbstract()) {
				
				//20150112:CCAR
				String cls_apiname = cls.getAPIName();
				String v_reg = "Y";	//car�W���u�߮ת��A�v��A�Y�ON�A�N��������|ñ�H���a�J(���䥦���L�����A�n�w�]Y)
				System.out.println(cls_apiname);
				//CCAR��api name : CustomerCorrectiveActionRequestApplicationForm
				if (cls_apiname.equals("CustomerCorrectiveActionRequestApplicationForm") || cls_apiname.equals("InternalQualityCorrectiveActionRequestApplicationForm") || cls_apiname.equals("VendorQualityCorrectiveActionRequestApplicationForm2")){
					v_reg = change.getValue(CommonConstants.ATT_PAGE_THREE_LIST02).toString();
					if (v_reg.equals("���߮�") || v_reg.equals("3D����")){
						v_reg = "N";
					}else{
						v_reg = "Y";
					}
					
					System.out.println(v_reg);
				}
				
				v_msg = v_msg + cls_apiname + ";" +v_reg; 
				
				if ((cls_apiname.equals("CustomerCorrectiveActionRequestApplicationForm") && v_reg.equals("Y")) || (cls_apiname.equals("InternalQualityCorrectiveActionRequestApplicationForm") && v_reg.equals("Y")) || (cls_apiname.equals("VendorQualityCorrectiveActionRequestApplicationForm2") && v_reg.equals("Y")) || (cls_apiname.equals("VendorQualityCorrectiveActionRequestApplicationForm"))){
					//***
					IAttribute[] attrs = null;

					//obj.logMonitor(cls.getName());	
					
					ArrayList identifyUsersList = new ArrayList();   
					
					//Get the attributes for Page Three
					ITableDesc page3 = cls.getTableDescriptor(TableTypeConstants.TYPE_PAGE_THREE);
					if (page3 != null) {
						attrs = page3.getAttributes();
						
						// ��X�U�@�������d�W�� (name ���)
						//String v_next_status = change.getDefaultNextStatus().toString();
						
						System.out.println(change.getStatus());
						
						String v_next_status = "::" + change.getStatus().toString() + "::" ;
						
						for (int i = 0; i < attrs.length; i++) {
						
							IAttribute attr = attrs[i];				
							
							
							if (attr.isVisible()) {


								// ��X Description ���
								// �YDescription ��A��[SetApprover]�}�Y�A��ܦ���Ψӫ��w���dñ�֤H��
								// [SetApprover]::REVIEW::	<--- ���w��REVIEW���d��ñ�֤H��
								// [SetApprover]::ME::PUR::	<--- ���w��ME�MPUR���d��ñ�֤H��
								// �@�����i�Ψӫ��w�h�����d
								
								IProperty v_desc = attr.getProperty(PropertyConstants.PROP_DESCRIPTION);							
								String v_desc_string = v_desc.toString();
								
								
								// �|ñ���d���A�u�|�OLIST �B multilist �G�ث��A���@					
								
								if ((attr.getDataType() == 4 || attr.getDataType() == 5 ) && v_desc_string.indexOf("[SetApprover]") != -1 && v_desc_string.indexOf(v_next_status) != -1) {
									//��api name
															
									//��X���ȡA��ñ�֤H��
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


												//todo : �n�A�[�P�_�A�p�G��aduser��������b����err msg
												 
												 IUser user;
												 if (v_true_id.equals("")){
													 user = (IUser)session.getObject(IUser.OBJECT_TYPE,"admin");  
												 }else{
													 user = (IUser)session.getObject(IUser.OBJECT_TYPE,v_true_id);
												 }
													  
												  
												 IUser[] approvers = new IUser[]{user};
												  
												  try{
													  System.out.println("add " + user.toString() + " to " + change.getStatus().toString());											  
													  if (v_true_id.equals("")){
														  v_msg = v_msg + "(�]" + v_userid + "�����b�����s�b),"; 
													  }
													  v_msg = v_msg + "add [" + user.toString() + "] to [" + change.getStatus().toString() + "]; ";
													  change.addApprovers(change.getStatus(), approvers, null, true,"");
													  
												  }catch(APIException e) {
													  System.out.println(e.getMessage());
													  v_msg = v_msg + e.getMessage();
													  
													  if(!(e.getMessage().indexOf("Duplicate name entered")>-1)){
														  throw new Exception (e.getMessage());  
													  }
													  
												  }
											  											
										  	}
									   }   
										
									}
									
								
								}
								
							}
							
						}
						//***	
				}
				
				
			}
				
				/*
				IUser user_admin = (IUser)session.getObject(IUser.OBJECT_TYPE,"admin");
				IUser[] approvers_admin = new IUser[]{user_admin};
				try{
					  
					  //change.removeApprovers(change.getDefaultNextStatus(), approvers_admin, null, "");
					  change.removeApprovers(change.getStatus(), approvers_admin, null, "");
					  
					  
				  }catch(APIException e) {
					  //e.printStackTrace();
				  }
				 */
				if (v_msg.equals(""))
					v_msg = "N/A";
				return new EventActionResult (info,new ActionResult(ActionResult.STRING,v_msg));
				
			}catch(Exception e){
				e.printStackTrace();
				return new EventActionResult (info,new ActionResult(ActionResult.EXCEPTION, e));
			}
	}

}
