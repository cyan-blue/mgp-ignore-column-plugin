package com.nn.mybatis.plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.ShellRunner;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.TableConfiguration;

import java.util.*;

/**
 * @author admin
 */
public class IgnoreColumnPlugin extends PluginAdapter {

    /**
     * 要忽略的列
     */
    private Map<String, List<String>> ignoreColumns = new HashMap<>();

    private static final String IGNORE_COLUMNS_FLAG = "ignoreColumns";

    @Override
    public boolean validate(List<String> list) {
        return true;
    }


    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // 初始化配置
        TableConfiguration tableConfiguration = introspectedTable.getTableConfiguration();
        Properties properties = tableConfiguration.getProperties();
        for (Map.Entry<Object, Object> entryEntry : properties.entrySet()) {
            if (entryEntry.getKey().equals(IGNORE_COLUMNS_FLAG)) {
                List<String> oneTableIgnoreColumns = Arrays.asList(entryEntry.getValue().toString().split(","));
                ignoreColumns.putIfAbsent(tableConfiguration.getTableName(), oneTableIgnoreColumns);
            }
        }
        return true;
    }

    @Override
    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        String tableName = introspectedTable.getTableConfiguration().getTableName();
        List<String> oneTableIgnore = ignoreColumns.get(tableName);
        if (oneTableIgnore == null || oneTableIgnore.size() == 0) {
            return super.modelSetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable, modelClassType);
        }
        String columnName = introspectedColumn.getActualColumnName();
        if (oneTableIgnore.contains(columnName)) {
            return false;
        }
        return super.modelSetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable, modelClassType);
    }

    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element,
                                                                     IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean sqlMapUpdateByExampleSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        ignoreColumnsElement(element.getElements(), introspectedTable);
        return super.sqlMapUpdateByExampleSelectiveElementGenerated(element, introspectedTable);
    }


    @Override
    public boolean sqlMapUpdateByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        ignoreColumnsElement(element.getElements(), introspectedTable);
        return super.sqlMapUpdateByExampleWithBLOBsElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        ignoreColumnsElement(element.getElements(), introspectedTable);
        return super.sqlMapUpdateByExampleWithoutBLOBsElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        ignoreColumnsElement(element.getElements(), introspectedTable);
        return super.sqlMapUpdateByPrimaryKeySelectiveElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        ignoreColumnsElement(element.getElements(), introspectedTable);
        return super.sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        ignoreColumnsElement(element.getElements(), introspectedTable);
        return super.sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(element, introspectedTable);
    }


    /**
     * 忽略更新的字段
     */
    private void ignoreColumnsElement(List<Element> elements, IntrospectedTable introspectedTable) {
        String tableName = introspectedTable.getTableConfiguration().getTableName();
        List<String> oneTableIgnore = ignoreColumns.get(tableName);
        if (oneTableIgnore == null || oneTableIgnore.size() == 0) {
            return;
        }
        List<Element> needIgnore = new ArrayList<>();
        for (Element ele : elements) {
            if (ele instanceof TextElement) {
                if (isTextElementNeedIgnore(ele, oneTableIgnore)) {
                    needIgnore.add(ele);
                }
            }
            if (ele instanceof XmlElement) {
                xmlElementFilter(((XmlElement) ele).getElements(), oneTableIgnore);
            }
        }
        elements.removeAll(needIgnore);
    }


    private void xmlElementFilter(List<Element> elements, List<String> oneTableIgnore) {
        List<Element> needIgnore = new ArrayList<>();
        for (Element ele : elements) {
            if (ele instanceof XmlElement) {
                if (isXmlElementNeedIgnore(ele, oneTableIgnore)) {
                    needIgnore.add(ele);
                }
            }
        }
        elements.removeAll(needIgnore);
    }

    private boolean isXmlElementNeedIgnore(Element ele, List<String> oneTableIgnore) {
        for (Element oneEle : ((XmlElement) ele).getElements()) {
            if (oneEle instanceof TextElement) {
                if (isTextElementNeedIgnore(oneEle, oneTableIgnore)) {
                    return true;
                }
            }

            if (oneEle instanceof XmlElement) {
                return isXmlElementNeedIgnore(ele, oneTableIgnore);
            }
        }
        return false;
    }


    private boolean isTextElementNeedIgnore(Element ele, List<String> oneTableIgnore) {
        String text = ((TextElement) ele).getContent().trim();
        for (String oneIgnore : oneTableIgnore) {
            if (text.matches("^" + oneIgnore + " =.*")) {
                return true;
            }
        }
        return false;
    }


    /**
     * 忽略更新的字段
     */
    private void ignoreColumnsFilter(IntrospectedTable introspectedTable) {
        List<IntrospectedColumn> baseColumns = introspectedTable.getBaseColumns();
        String tableName = introspectedTable.getTableConfiguration().getTableName();
        List<String> oneTableIgnore = ignoreColumns.get(tableName);

        List<IntrospectedColumn> needRemovedColumns = new ArrayList<>();

        for (IntrospectedColumn oneColumn : baseColumns) {
            if (oneTableIgnore.contains(oneColumn.getActualColumnName())) {
                needRemovedColumns.add(oneColumn);
            }

        }
        for (IntrospectedColumn oneCol : needRemovedColumns) {
            introspectedTable.getBaseColumns().remove(oneCol);
        }
    }

    public static void generate() {
        String config = IgnoreColumnPlugin.class.getClassLoader().getResource("mybatis-generator.xml").getFile();
        String[] arg = {"-configfile", config, "-overwrite"};
        ShellRunner.main(arg);
    }

    public static void main(String[] args) {
        generate();
    }
}
