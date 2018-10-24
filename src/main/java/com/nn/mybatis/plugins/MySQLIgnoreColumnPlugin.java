package com.nn.mybatis.plugins;

import java.util.List;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.ShellRunner;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.PrimitiveTypeWrapper;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

public class MySQLIgnoreColumnPlugin extends PluginAdapter {

	@Override
	public boolean validate(List<String> list) {

		return true;
	}

	/**
	 * 为每个Example类添加limit和offset属性已经set、get方法
	 */
	@Override
	public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		PrimitiveTypeWrapper integerWrapper = FullyQualifiedJavaType.getIntInstance().getPrimitiveTypeWrapper();

		Field limit = new Field();
		limit.setName("limit");
		limit.setVisibility(JavaVisibility.PRIVATE);
		limit.setType(integerWrapper);
		topLevelClass.addField(limit);

		Method setLimit = new Method();
		setLimit.setVisibility(JavaVisibility.PUBLIC);
		setLimit.setName("setLimit");
		setLimit.addParameter(new Parameter(integerWrapper, "limit"));
		setLimit.addBodyLine("this.limit = limit;");
		topLevelClass.addMethod(setLimit);

		Method getLimit = new Method();
		getLimit.setVisibility(JavaVisibility.PUBLIC);
		getLimit.setReturnType(integerWrapper);
		getLimit.setName("getLimit");
		getLimit.addBodyLine("return limit;");
		topLevelClass.addMethod(getLimit);

		Field offset = new Field();
		offset.setName("offset");
		offset.setVisibility(JavaVisibility.PRIVATE);
		offset.setType(integerWrapper);
		topLevelClass.addField(offset);

		Method setOffset = new Method();
		setOffset.setVisibility(JavaVisibility.PUBLIC);
		setOffset.setName("setOffset");
		setOffset.addParameter(new Parameter(integerWrapper, "offset"));
		setOffset.addBodyLine("this.offset = offset;");
		topLevelClass.addMethod(setOffset);

		Method getOffset = new Method();
		getOffset.setVisibility(JavaVisibility.PUBLIC);
		getOffset.setReturnType(integerWrapper);
		getOffset.setName("getOffset");
		getOffset.addBodyLine("return offset;");
		topLevelClass.addMethod(getOffset);

		return true;
	}

	/**
	 * 为Mapper.xml的selectByExample添加limit
	 */
	@Override
	public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element,
			IntrospectedTable introspectedTable) {

		XmlElement ifLimitNotNullElement = new XmlElement("if");
		ifLimitNotNullElement.addAttribute(new Attribute("test", "limit != null"));

		XmlElement ifOffsetNotNullElement = new XmlElement("if");
		ifOffsetNotNullElement.addAttribute(new Attribute("test", "offset != null"));
		ifOffsetNotNullElement.addElement(new TextElement("limit ${offset}, ${limit}"));
		ifLimitNotNullElement.addElement(ifOffsetNotNullElement);

		XmlElement ifOffsetNullElement = new XmlElement("if");
		ifOffsetNullElement.addAttribute(new Attribute("test", "offset == null"));
		ifOffsetNullElement.addElement(new TextElement("limit ${limit}"));
		ifLimitNotNullElement.addElement(ifOffsetNullElement);

		element.addElement(ifLimitNotNullElement);

		return true;
	}

	@Override
	public boolean sqlMapUpdateByExampleSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {

		System.out.println(introspectedTable.getTableConfiguration().getProperties());

		return super.sqlMapUpdateByExampleSelectiveElementGenerated(element, introspectedTable);
	}


	@Override
	public boolean sqlMapUpdateByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return super.sqlMapUpdateByExampleWithBLOBsElementGenerated(element, introspectedTable);
	}

	@Override
	public boolean sqlMapUpdateByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return super.sqlMapUpdateByExampleWithoutBLOBsElementGenerated(element, introspectedTable);
	}

	@Override
	public boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return super.sqlMapUpdateByPrimaryKeySelectiveElementGenerated(element, introspectedTable);
	}

	@Override
	public boolean sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return super.sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(element, introspectedTable);
	}

	@Override
	public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return super.sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(element, introspectedTable);
	}

	public static void generate() {
		String config = MySQLIgnoreColumnPlugin.class.getClassLoader().getResource("mybatis-generator.xml").getFile();
		String[] arg = { "-configfile", config, "-overwrite" };
		ShellRunner.main(arg);
	}
	public static void main(String[] args) {
		generate();
	}
}
