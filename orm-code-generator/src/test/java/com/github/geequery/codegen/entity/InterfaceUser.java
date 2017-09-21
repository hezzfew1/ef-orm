package com.github.geequery.codegen.entity;

import javax.persistence.Table;
import jef.database.DataObject;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.Date;
import jef.codegen.support.NotModified;
/**
 * This class was generated by JEF according to the table in database.
 * You need to modify the type of primary key field, to the strategy your own.
 */
@NotModified
@Entity
@Table(name="interface_user")
public class InterfaceUser extends DataObject{


	@Id
	@Column(name="id",columnDefinition="char(14)",length=14,nullable=false)
	private String id;

	@Column(name="interfaceId",columnDefinition="char(14)",length=14)
	private String interfaceid;

	@Column(name="userId",columnDefinition="char(14)",length=14)
	private String userid;

	@Column(name="createTime",columnDefinition="timestamp")
	private Date createtime;

	@Column(name="email",columnDefinition="varchar(45)",length=45)
	private String email;

	public void setId(String obj){
		this.id = obj;
	}

	public String getId(){
		return id;
	}

	public void setInterfaceid(String obj){
		this.interfaceid = obj;
	}

	public String getInterfaceid(){
		return interfaceid;
	}

	public void setUserid(String obj){
		this.userid = obj;
	}

	public String getUserid(){
		return userid;
	}

	public void setCreatetime(Date obj){
		this.createtime = obj;
	}

	public Date getCreatetime(){
		return createtime;
	}

	public void setEmail(String obj){
		this.email = obj;
	}

	public String getEmail(){
		return email;
	}

	public InterfaceUser(){
	}

	public InterfaceUser(String id){
		this.id = id;
	}


   public enum Field implements jef.database.Field{id,interfaceid,userid,createtime,email}
}