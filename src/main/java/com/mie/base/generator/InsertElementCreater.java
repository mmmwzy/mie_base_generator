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
import org.mybatis.generator.config.Context;

public class InsertElementCreater {
	
	private IntrospectedTable introspectedTable;
	private Context context;
	private boolean isSimple;
	private String identity;
	private String column;
	
	public InsertElementCreater(IntrospectedTable introspectedTable, String identity, String column) {
		super();
		this.introspectedTable = introspectedTable;
		this.context = introspectedTable.getContext();
		this.identity = identity;
		this.column = column;
		this.isSimple =false;
	}
	
	public XmlElement createElement() {
		XmlElement answer = new XmlElement("insert"); //$NON-NLS-1$

        answer.addAttribute(new Attribute(
                "id", introspectedTable.getInsertStatementId())); //$NON-NLS-1$

        FullyQualifiedJavaType parameterType;
        if (isSimple) {
            parameterType = new FullyQualifiedJavaType(
                    introspectedTable.getBaseRecordType());
        } else {
            parameterType = introspectedTable.getRules()
                    .calculateAllFieldsClass();
        }

        answer.addAttribute(new Attribute("parameterType", //$NON-NLS-1$
                parameterType.getFullyQualifiedName()));

        context.getCommentGenerator().addComment(answer);

       
        String bindUUID = "<bind name=\"_uuid\" value=\"@com.mie.base.utils.uuid.UUIDGenerator@generateUUIDAndSetId(#this)\"/>";
    	if (!identity.equals("id")) {
			bindUUID = "<bind name=\"_uuid\" value=\"@com.mie.base.utils.uuid.UUIDGenerator@generateUUIDAndSetId(#this)\"/>";
		}
		TextElement uuidBindElement = new TextElement(bindUUID);
		answer.addElement(uuidBindElement);

        StringBuilder insertClause = new StringBuilder();
        StringBuilder valuesClause = new StringBuilder();

        insertClause.append("insert into "); //$NON-NLS-1$
        insertClause.append(introspectedTable
                .getFullyQualifiedTableNameAtRuntime());
        insertClause.append(" ("); //$NON-NLS-1$

        valuesClause.append("values ("); //$NON-NLS-1$

        List<String> valuesClauses = new ArrayList<String>();
        Iterator<IntrospectedColumn> iter = introspectedTable.getAllColumns()
                .iterator();
        while (iter.hasNext()) {
            IntrospectedColumn introspectedColumn = iter.next();
            insertClause.append(MyBatis3FormattingUtilities
            		.getEscapedColumnName(introspectedColumn));
            
            if (introspectedColumn.isIdentity() || introspectedColumn.getActualColumnName().equalsIgnoreCase(this.column)) {
            	valuesClause.append("#{_uuid}");
            	
            }else{
                valuesClause.append(MyBatis3FormattingUtilities
                        .getParameterClause(introspectedColumn));
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

        insertClause.append(')');
        answer.addElement(new TextElement(insertClause.toString()));

        valuesClause.append(')');
        valuesClauses.add(valuesClause.toString());

        for (String clause : valuesClauses) {
            answer.addElement(new TextElement(clause));
        }

        return answer;
	}

}
