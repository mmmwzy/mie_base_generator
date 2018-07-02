package com.mie.base.generator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

public class InsertBatchElementCreater{
	private String item = "item";  
	private IntrospectedTable introspectedTable;
	private String tableName;
	
	//private Context context;
	//private boolean isSimple;
	//private String identity;
	private String column;
	
	public InsertBatchElementCreater(IntrospectedTable introspectedTable, String tableName) {
		super();
		this.introspectedTable = introspectedTable;
		this.tableName = tableName;
	}
	
	public XmlElement createElement() {
		XmlElement answer = new XmlElement("insert"); //$NON-NLS-1$

        answer.addAttribute(new Attribute(
                "id", "insertBatch")); //$NON-NLS-1$

        FullyQualifiedJavaType parameterType;
       
            parameterType = introspectedTable.getRules()
                    .calculateAllFieldsClass();

        answer.addAttribute(new Attribute("parameterType", //$NON-NLS-1$
                parameterType.getFullyQualifiedName()));

        StringBuilder insertClause = new StringBuilder();
        StringBuilder valuesClause = new StringBuilder();

        insertClause.append("insert into "); //$NON-NLS-1$
        insertClause.append(introspectedTable
                .getFullyQualifiedTableNameAtRuntime());
        insertClause.append(" ("); //$NON-NLS-1$

        valuesClause.append("("); //$NON-NLS-1$

        List<String> valuesClauses = new ArrayList<String>();
        Iterator<IntrospectedColumn> iter = introspectedTable.getAllColumns()
                .iterator();
        while (iter.hasNext()) {
            IntrospectedColumn introspectedColumn = iter.next();
            insertClause.append(MyBatis3FormattingUtilities
            		.getEscapedColumnName(introspectedColumn));
            
            if (introspectedColumn.isIdentity() || introspectedColumn.getActualColumnName().equals(this.column)) {
            	valuesClause.append("#{_uuid}");
            	
            }else{
            	valuesClause.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, item + "."));
            }
            
            if (iter.hasNext()) {
                insertClause.append(", "); //$NON-NLS-1$
                valuesClause.append(", "); //$NON-NLS-1$
            }
            
            if (valuesClause.length() > 80) {
                answer.addElement(new TextElement(insertClause.toString()));
                insertClause.setLength(0);
                OutputUtilities.xmlIndent(insertClause, 1);

                valuesClauses.add(valuesClause.toString());
                valuesClause.setLength(0);
                OutputUtilities.xmlIndent(valuesClause, 1);
            }

            
        }

        insertClause.append(") values ");
        answer.addElement(new TextElement(insertClause.toString()));

        
        XmlElement foreachElement = NewForeachElement();
        valuesClause.append(')');
        valuesClauses.add(valuesClause.toString());
        for (String clause : valuesClauses) {
			foreachElement.addElement(new TextElement(clause));
		}
        answer.addElement(foreachElement);
        return answer;
	}
	/** 
     * @return 
     */  
    public XmlElement NewForeachElement(){  
        XmlElement foreachElement = new XmlElement("foreach");  
        foreachElement.addAttribute(new Attribute("collection", "list"));  
        foreachElement.addAttribute(new Attribute("item", item));  
        foreachElement.addAttribute(new Attribute("index", "index"));  
        foreachElement.addAttribute(new Attribute("separator", ","));  
        return foreachElement;  
    }  
}
