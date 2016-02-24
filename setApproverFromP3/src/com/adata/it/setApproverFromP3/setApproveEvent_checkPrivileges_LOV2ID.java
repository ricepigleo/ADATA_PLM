package com.adata.it.setApproverFromP3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import com.agile.api.IAgileClass;
import com.agile.api.IAgileSession;
import com.agile.api.IAttribute;
import com.agile.api.IChange;
import com.agile.api.IDataObject;
import com.agile.api.INode;
import com.agile.api.IProperty;
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

public class setApproveEvent_checkPrivileges_LOV2ID implements IEventAction {

	public EventActionResult doAction(IAgileSession session, INode actionNode, IEventInfo request) {
		IObjectEventInfo info = (IObjectEventInfo) request;

		try	{
			String v_msg = "";
			
			IDataObject affectedObject = info.getDataObject();
			IChange change = (IChange)affectedObject;
			
			System.out.println("check privileges : change No: " +  change);			
			
			IAgileClass cls = change.getAgileClass();
			
			
			// Check if the class is abstract or concrete
			if (!cls.isAbstract()) {
				IAttribute[] attrs = null;

				//obj.logMonitor(cls.getName());	
				
				ArrayList identifyUsersList = new ArrayList();   
				
				//Get the attributes for Page Three
				ITableDesc page3 = cls.getTableDescriptor(TableTypeConstants.TYPE_PAGE_THREE);
				if (page3 != null) {
					attrs = page3.getAttributes();				
					
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
							
							//if ((attr.getDataType() == 4 || attr.getDataType() == 5 ) && v_desc_string.indexOf("[SetApprover]") != -1 && v_desc_string.indexOf(v_next_status) != -1) {
							//���P�_���d
							if ((attr.getDataType() == 4 || attr.getDataType() == 5 ) && v_desc_string.indexOf("[SetApprover]") != -1 ) {
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
										  
										  //v_userid=v_userid.substring(v_userid.indexOf("(")+1);
										  //v_userid = v_userid.substring(0,v_userid.length()-1);
										  //System.out.println(v_userid);
										  
										  //20141216
										  //�z�Luser.p2�W��multilist01���A�P�_�ݩ���ӱb��
										  //����쪺���@�A1��1 �P 1��h�A���n���@
										  
										  IQuery query = (IQuery)session.createObject(IQuery.OBJECT_TYPE, UserConstants.CLASS_USERS_CLASS); 										  
											 query.setCaseSensitive(false);
											 query.setCriteria("[2090] contains any '" +  v_userid + "' and [General Info.Status] == 'Active'");
											 Iterator ii = query.execute().iterator();		        
											 if (ii.hasNext()) {
												 
												 System.out.println("got it");
												 IRow   row    = (IRow)ii.next(); 
												 v_true_id = row.getValue(UserConstants.ATT_GENERAL_INFO_USER_ID).toString();
												 System.out.println(v_true_id);
											 }else{
												//todo : �n�A�[�P�_�A�p�G��aduser��������b����err msg
												 throw new Exception (v_userid + " �����b�����s�b");
											 }
												  
											 
											


										  IUser user = (IUser)session.getObject(IUser.OBJECT_TYPE,v_true_id);
										  IUser[] approvers = new IUser[]{user};

										  if(user.hasPrivilege(UserConstants.PRIV_READ, change) && user.hasPrivilege(UserConstants.PRIV_DISCOVER  , change) && user.hasPrivilege(UserConstants.PRIV_APPROVEREJECT , change)) {
											  
										  }else{
											  throw new Exception (v_userid + " �����b�� " + v_true_id +  " �L�v��");
										  }
										  
									  }
								   }   
								
								}
							
						
							}
						
						}
					
					}
				}
			

				if (v_msg.equals(""))
					v_msg = "ok.";
				return new EventActionResult (info,new ActionResult(ActionResult.STRING,v_msg));
			
			}catch(Exception e){
				e.printStackTrace();
				return new EventActionResult (info,new ActionResult(ActionResult.EXCEPTION, e));
			}		
	}

}
