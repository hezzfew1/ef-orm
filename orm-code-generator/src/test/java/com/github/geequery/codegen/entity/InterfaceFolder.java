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
@Table(name="interface_folder")
public class InterfaceFolder extends DataObject{


	@Id
	@Column(name="id",columnDefinition="char(14)",length=14,nullable=false)
	private String id;

	@Column(name="name",columnDefinition="varchar(50)",length=50)
	private String name;

	@Column(name="createTime",columnDefinition="timestamp")
	private Date createtime;

	@Column(name="moduleId",columnDefinition="char(14)",length=14)
	private String moduleid;

	@Column(name="projectId",columnDefinition="char(14)",length=14)
	private String projectid;

	public void setId(String obj){
		this.id = obj;
	}

	public String getId(){
		return id;
	}

	public void setName(String obj){
		this.name = obj;
	}

	public String getName(){
		return name;
	}

	public void setCreatetime(Date obj){
		this.createtime = obj;
	}

	public Date getCreatetime(){
		return createtime;
	}

	public void setModuleid(String obj){
		this.moduleid = obj;
	}

	public String getModuleid(){
		return moduleid;
	}

	public void setProjectid(String obj){
		this.projectid = obj;
	}

	public String getProjectid(){
		return projectid;
	}

	public InterfaceFolder(){
	}

	public InterfaceFolder(String id){
		this.id = id;
	}


   public enum Field implements jef.database.Field{id,name,createtime,moduleid,projectid}
}