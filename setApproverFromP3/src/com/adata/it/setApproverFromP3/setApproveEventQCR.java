package com.adata.it.setApproverFromP3;

import java.util.ArrayList;
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
import com.agile.api.ITableDesc;
import com.agile.api.IUser;
import com.agile.api.PropertyConstants;
import com.agile.api.TableTypeConstants;
import com.agile.px.ActionResult;
import com.agile.px.EventActionResult;
import com.agile.px.IEventAction;
import com.agile.px.IEventInfo;
import com.agile.px.IObjectEventInfo;

public class setApproveEventQCR  implements IEventAction {

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
											  
											  v_userid=v_userid.substring(v_userid.indexOf("(")+1);
											  v_userid = v_userid.substring(0,v_userid.length()-1);
											  System.out.println(v_userid);
											  


											  IUser user = (IUser)session.getObject(IUser.OBJECT_TYPE,v_userid);
											  IUser[] approvers = new IUser[]{user};
											  
											  try{
												  System.out.println("add " + user.toString() + " to " + change.getStatus().toString());
												  change.addApprovers(change.getStatus(), approvers, null, true,""); 
												  v_msg = v_msg + "add [" + user.toString() + "] to [" + change.getStatus().toString() + "]; ";
											  }catch(APIException e) {													  
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
