package com.nn.mybatis.plugins;

import com.nn.mybatis.plugins.util.FieldUtils;
import com.nn.mybatis.plugins.util.ReflectionUtils;
import com.nn.mybatis.plugins.util.StringUtils;
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
 * select查询不进行忽略， insert和update忽略字段
 * <p>
 * 需要配置
 *
 * @author admin
 */
public class IgnoreColumnPlugin extends PluginAdapter {

    /**
     * 需要忽略的数据库列的配置
     */
    private static final String IGNORE_COLUMNS_FLAG = "igcIgnoreColumns";
    /**
     * 要忽略的数据库列
     */
    private Map<String, List<String>> ignoreColumns = new HashMap<>();
    /**
     * 要忽略的列的model名字
     */
    private Map<String, List<String>> ignoreColumnModelFields = new HashMap<>();

    public static void generate() {
        String config = IgnoreColumnPlugin.class.getClassLoader().getResource("mybatis-generator.xml").getFile();
        String[] arg = {"-configfile", config, "-overwrite"};
        ShellRunner.main(arg);
    }

    public static void main(String[] args) {
        generate();
    }

    @Override
    public boolean validate(List<String> list) {
        return true;
    }

    /**
     * 初始化阶段
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     */
    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        super.initialized(introspectedTable);
        // 初始化配置
        TableConfiguration tableConfiguration = introspectedTable.getTableConfiguration();
        Properties properties = tableConfiguration.getProperties();
        for (Map.Entry<Object, Object> entryEntry : properties.entrySet()) {
            if (entryEntry.getKey().equals(IGNORE_COLUMNS_FLAG)) {
                List<String> oneTableIgnoreColumns = Arrays.asList(entryEntry.getValue().toString().split(","));
                ignoreColumns.putIfAbsent(tableConfiguration.getTableName(), oneTableIgnoreColumns);
                List<String> modelNameList = new ArrayList<>();
                for (String oneModelName : oneTableIgnoreColumns) {
                    modelNameList.add(FieldUtils.getModelNameByField(oneModelName));
                }
                ignoreColumnModelFields.putIfAbsent(tableConfiguration.getTableName(), modelNameList);
            }
        }
    }

    @Override
    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        String tableName = introspectedTable.getTableConfiguration().getTableName();
        List<String> oneTableIgnore = ignoreColumns.get(tableName);
        if (oneTableIgnore == null || oneTableIgnore.size() == 0) {
            return super.modelSetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable, modelClassType);
        }
        // 过滤掉生成的对象的set方法
        String columnName = introspectedColumn.getActualColumnName();
        if (oneTableIgnore.contains(columnName)) {
            return false;
        }
        return super.modelSetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable, modelClassType);
    }

    @Override
    public boolean sqlMapInsertElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        insertIgnoreColumnsElementFilter(element.getElements(), introspectedTable);
        return super.sqlMapInsertElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapInsertSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        insertSelectIgnoreColumnsElementFilter(element.getElements(), introspectedTable);
        return super.sqlMapInsertSelectiveElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByExampleSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        updateIgnoreColumnsElementFilter(element.getElements(), introspectedTable);
        return super.sqlMapUpdateByExampleSelectiveElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        updateIgnoreColumnsElementFilter(element.getElements(), introspectedTable);
        return super.sqlMapUpdateByExampleWithBLOBsElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        updateIgnoreColumnsElementFilter(element.getElements(), introspectedTable);
        return super.sqlMapUpdateByExampleWithoutBLOBsElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        updateIgnoreColumnsElementFilter(element.getElements(), introspectedTable);
        return super.sqlMapUpdateByPrimaryKeySelectiveElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        updateIgnoreColumnsElementFilter(element.getElements(), introspectedTable);
        return super.sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        updateIgnoreColumnsElementFilter(element.getElements(), introspectedTable);
        return super.sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(element, introspectedTable);
    }

    /**
     * 忽略insert语句中的字段
     * insert mapper中类型有： 1. field_name, 2.#{modelName,jdbcType=*}, 3.长语句中包含上面两种
     */
    private void insertIgnoreColumnsElementFilter(List<Element> elements, IntrospectedTable introspectedTable) {
        String tableName = introspectedTable.getTableConfiguration().getTableName();
        List<String> oneTableIgnore = ignoreColumns.get(tableName);
        List<String> oneTableModelIgnore = ignoreColumnModelFields.get(tableName);
        if (oneTableIgnore == null || oneTableIgnore.size() == 0) {
            return;
        }
        if (oneTableModelIgnore == null || oneTableModelIgnore.size() == 0) {
            return;
        }
        if (oneTableModelIgnore.size() != oneTableIgnore.size()) {
            return;
        }

        for (Element ele : elements) {
            if (ele instanceof TextElement) {
                String text = ((TextElement) ele).getContent();
                for (String oneIgnore : oneTableModelIgnore) {
                    String replaceText = text.replaceAll("#\\{" + oneIgnore + ",jdbcType=\\S+},", "");
                    replaceText = replaceText.replaceAll("#\\{" + oneIgnore + ",jdbcType=\\S+}\\)", ")");
                    if (!replaceText.equals(text)) {
                        text = replaceText;
                        ReflectionUtils.setFieldValue("content", ele, replaceText);
                    }
                }
                // TODO 优化：要注意误替换！！！
                for (String oneIgnore : oneTableIgnore) {
                    String replaceText = text.replaceAll(oneIgnore + ",", "");
                    replaceText = replaceText.replaceAll(oneIgnore + "\\)", ")");
                    if (!replaceText.equals(text)) {
                        text = replaceText;
                        ReflectionUtils.setFieldValue("content", ele, replaceText);
                    }
                }
            }
        }
    }

    /**
     * 忽略insert select语句中的字段
     */
    private void insertSelectIgnoreColumnsElementFilter(List<Element> elements, IntrospectedTable introspectedTable) {
        String tableName = introspectedTable.getTableConfiguration().getTableName();
        List<String> oneTableIgnore = ignoreColumns.get(tableName);
        List<String> oneTableModelIgnore = ignoreColumnModelFields.get(tableName);
        if (oneTableIgnore == null || oneTableIgnore.size() == 0) {
            return;
        }
        if (oneTableModelIgnore == null || oneTableModelIgnore.size() == 0) {
            return;
        }
        if (oneTableModelIgnore.size() != oneTableIgnore.size()) {
            return;
        }

        for (Element ele : elements) {
            if (ele instanceof XmlElement) {
                List<Element> effectEles = ((XmlElement) ele).getElements();
                List<Element> needIgnore = new ArrayList<>();
                for (Element oneEle : effectEles) {

                    if (isXmlElementNeedIgnore(oneEle, oneTableModelIgnore, StatementTypeReplace.INSERT_SELECTIVE)) {
                        needIgnore.add(oneEle);
                    }

                    if (isXmlElementNeedIgnore(oneEle, oneTableIgnore, StatementTypeReplace.FIELD_PLUS_COMMA)) {
                        needIgnore.add(oneEle);
                    }
                }
                effectEles.removeAll(needIgnore);
            }
        }
    }

    private boolean isXmlElementNeedIgnore(Element ele, List<String> oneTableIgnore, StatementTypeReplace statementTypeReplace) {
        for (Element oneEle : ((XmlElement) ele).getElements()) {
            if (oneEle instanceof TextElement) {
                if (isTextElementNeedIgnore(oneEle, oneTableIgnore, statementTypeReplace)) {
                    return true;
                }
            }
            // 递归调用xml
            if (oneEle instanceof XmlElement) {
                return isXmlElementNeedIgnore(ele, oneTableIgnore, statementTypeReplace);
            }
        }
        return false;
    }

    private boolean isTextElementNeedIgnore(Element ele, List<String> oneTableIgnore, StatementTypeReplace statementTypeReplace) {
        String text = ((TextElement) ele).getContent().trim();
        for (String oneIgnore : oneTableIgnore) {
            if (StringUtils.isTextMatch(text, oneIgnore, statementTypeReplace)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 忽略更新的字段操作
     */
    private void updateIgnoreColumnsElementFilter(List<Element> elements, IntrospectedTable introspectedTable) {
        String tableName = introspectedTable.getTableConfiguration().getTableName();
        List<String> oneTableIgnore = ignoreColumns.get(tableName);
        if (oneTableIgnore == null || oneTableIgnore.size() == 0) {
            return;
        }
        // set fieldName = {*} 处理
        handleFirstSetElement(elements, oneTableIgnore);

        List<Element> needIgnore = new ArrayList<>();

        // 两种类型的ele处理，TextElement直接判断是否包含，XmlElement需要多层处理
        for (Element ele : elements) {
            if (ele instanceof TextElement) {
                if (isTextElementNeedIgnore(ele, oneTableIgnore, StatementTypeReplace.FIELD_PLUS_EQUAL)) {
                    needIgnore.add(ele);
                }
            }
            if (ele instanceof XmlElement) {
                xmlElementHandle(((XmlElement) ele).getElements(), oneTableIgnore);
            }
        }
        elements.removeAll(needIgnore);
    }

    /**
     * 处理set fieldName = {}
     */
    private void handleFirstSetElement(List<Element> elements, List<String> oneTableIgnore) {
        for (Element ele : elements) {
            if (ele instanceof TextElement) {
                String text = ((TextElement) ele).getContent();
                for (String oneIgnore : oneTableIgnore) {
                    if (StringUtils.isTextMatch(text, oneIgnore, StatementTypeReplace.UPDATE_SET_PLUS_EQUAL)) {
                        String replaceText = text.replaceAll(String.format(StatementTypeReplace.UPDATE_SET_PLUS_EQUAL.getRegex(), oneIgnore), "set ");
                        if (!replaceText.equals(text)) {
                            text = replaceText;
                            ReflectionUtils.setFieldValue("content", ele, replaceText);
                        }
                    }
                }
            }
        }
    }

    private void xmlElementHandle(List<Element> elements, List<String> oneTableIgnore) {
        List<Element> needIgnore = new ArrayList<>();
        for (Element ele : elements) {
            if (ele instanceof XmlElement) {
                if (isXmlElementNeedIgnore(ele, oneTableIgnore, StatementTypeReplace.FIELD_PLUS_EQUAL)) {
                    needIgnore.add(ele);
                }
            }
        }
        elements.removeAll(needIgnore);
    }
}
