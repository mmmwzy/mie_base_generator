package com.mie.base.generator.plugins;

import java.util.List;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.xml.XmlElement;

import com.mie.base.generator.InsertElementCreater;
import com.mie.base.generator.InsertSelectiveElementCreater;

public class UUIDGeneratePlugin extends PluginAdapter {
	
	private String identity;
	private String column;

	@Override
	public boolean validate(List<String> warnings) {
		column = this.getProperties().getProperty("column");
		identity = this.getProperties().getProperty("identity");
		return true;
	}

	@Override
	public boolean sqlMapInsertElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		if (!this.isCreateUUID(introspectedTable)) {
			return super.sqlMapInsertElementGenerated(element, introspectedTable);
		}
		
		InsertElementCreater creater = new InsertElementCreater(introspectedTable, identity, column);
		
		element.getElements().clear();
		element.getElements().addAll(creater.createElement().getElements());
		return super.sqlMapInsertSelectiveElementGenerated(element, introspectedTable);
	}

	@Override
	public boolean sqlMapInsertSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		if (!this.isCreateUUID(introspectedTable)) {
			return super.sqlMapInsertElementGenerated(element, introspectedTable);
		}

		InsertSelectiveElementCreater creater = new InsertSelectiveElementCreater(introspectedTable, identity, column);

		element.getElements().clear();
		element.getElements().addAll(creater.createElement().getElements());
		return super.sqlMapInsertSelectiveElementGenerated(element, introspectedTable);
	}
	
	private boolean isCreateUUID(IntrospectedTable introspectedTable){
		IntrospectedColumn iColumn = introspectedTable.getColumn(column);
		boolean isCreateUUID = true;
//		if (iColumn.getJdbcType() != Types.VARCHAR && iColumn.getJdbcType() != Types.NVARCHAR) {
//			System.out.println("【警告】uuid 主键生成异常:主键的类型不是字符串,");
//			isCreateUUID = false;
//		}
		
		if (iColumn.getLength() < 32) {
			System.out.println("【警告】uuid 主键生成异常:UUID的长度必须大于32,");
			isCreateUUID = false;
		}
		
		return isCreateUUID;
	}

}
