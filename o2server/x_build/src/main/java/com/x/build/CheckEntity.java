package com.x.build;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.openjpa.persistence.jdbc.ContainerTable;
import org.junit.Test;

import com.x.base.core.entity.JpaObject;
import com.x.base.core.entity.annotation.ContainerEntity;
import com.x.base.core.project.annotation.FieldDescribe;
import com.x.base.core.project.annotation.Module;
import com.x.base.core.project.gson.XGsonBuilder;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

public class CheckEntity {

	/*
	 * 检查数据库字段名是否是ColumnNamePrefix + fieldName
	 */
	@Test
	public void checkColumnName() throws Exception {
		try (ScanResult scanResult = new ClassGraph().enableAllInfo().scan()) {
			List<ClassInfo> classInfos = scanResult.getClassesWithAnnotation(ContainerEntity.class.getName());
			List<Class<?>> list = new ArrayList<>();
			for (ClassInfo info : classInfos) {
				list.add(Class.forName(info.getName()));
			}
			for (Class<?> cls : list) {
				List<Field> fields = FieldUtils.getAllFieldsList(cls);
				for (Field field : fields) {
					Column col = field.getAnnotation(Column.class);
					if (null != col) {
						if (StringUtils.equals(JpaObject.ColumnNamePrefix + field.getName(), col.name())) {
							// System.out.println(cls + ":" + field.getName() + ":" + col.name() + ":" +
							// col.length());
						} else {
							System.err.println(cls + ":" + field.getName() + ":" + col.name() + ":" + col.length());
						}
					}
				}
			}
		}
	}

	/*
	 * 检查是否有将Lob类型字段增加索引
	 */
	@Test
	public void checkLobIndex() throws Exception {
		try (ScanResult scanResult = new ClassGraph().enableAllInfo().scan()) {
			List<ClassInfo> classInfos = scanResult.getClassesWithAnnotation(ContainerEntity.class.getName());
			List<Class<?>> list = new ArrayList<>();
			for (ClassInfo info : classInfos) {
				list.add(Class.forName(info.getName()));
			}
			for (Class<?> cls : list) {
				List<Field> fields = FieldUtils.getAllFieldsList(cls);
				for (Field field : fields) {
					Lob lob = field.getAnnotation(Lob.class);
					Index index = field.getAnnotation(Index.class);
					if ((null != lob) && (null != index)) {
						System.err.println(cls + ":" + field.getName());
					} else {
						// System.out.println(cls + ":" + field.getName() ;
					}
				}
			}
		}
	}

	/*
	 * 检查是否有将Lob类型字段增加索引
	 * 
	 * @FieldDescribe("群组的个人成员.存放个人 ID.")
	 * 
	 * @PersistentCollection(fetch = FetchType.EAGER)
	 * 
	 * @OrderColumn(name = ORDERCOLUMNCOLUMN)
	 * 
	 * @ContainerTable(name = TABLE + ContainerTableNameMiddle +
	 * personList_FIELDNAME, joinIndex = @Index(name = TABLE + IndexNameMiddle +
	 * personList_FIELDNAME + JoinIndexNameSuffix))
	 * 
	 * @ElementColumn(length = JpaObject.length_id, name = ColumnNamePrefix +
	 * personList_FIELDNAME)
	 * 
	 * @ElementIndex(name = TABLE + IndexNameMiddle + personList_FIELDNAME +
	 * ElementIndexNameSuffix)
	 * 
	 * @CheckPersist(allowEmpty = true, citationExists = @CitationExist(type =
	 * Person.class)) private List<String> personList;
	 */
	@Test
	public void checkListFieldContainerTableName() throws Exception {
		try (ScanResult scanResult = new ClassGraph().enableAllInfo().scan()) {
			List<ClassInfo> classInfos = scanResult.getClassesWithAnnotation(ContainerEntity.class.getName());
			List<Class<?>> list = new ArrayList<>();
			for (ClassInfo info : classInfos) {
				list.add(Class.forName(info.getName()));
			}
			for (Class<?> cls : list) {
				List<Field> fields = FieldUtils.getAllFieldsList(cls);
				for (Field field : fields) {
					if (List.class.isAssignableFrom(field.getType())) {
						ContainerTable containerTable = field.getAnnotation(ContainerTable.class);
						if (null != containerTable) {
							String name = FieldUtils.readStaticField(cls, "TABLE", true).toString()
									+ JpaObject.ContainerTableNameMiddle + field.getName();
							if (!StringUtils.equals(name, containerTable.name())) {
								System.err.println(cls.getName() + ":" + field.getName());
							}
						}
					}
				}
			}
		}
	}

	/*
	 * 检查是否将@FieldDescribe注解到static字段上,如果是意味着上下行搞错了
	 */
	@Test
	public void checkFieldDescribeOnStatic() throws Exception {
		try (ScanResult scanResult = new ClassGraph().enableAllInfo().scan()) {
			List<ClassInfo> classInfos = scanResult.getClassesWithAnnotation(ContainerEntity.class.getName());
			List<Class<?>> list = new ArrayList<>();
			for (ClassInfo info : classInfos) {
				list.add(Class.forName(info.getName()));
			}
			for (Class<?> cls : list) {
				List<Field> fields = FieldUtils.getFieldsListWithAnnotation(cls, FieldDescribe.class);
				for (Field field : fields) {
					if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
						System.out.println(cls + ":" + field.getName());
					}
				}
			}
		}
	}

	/*
	 * 检查约束名中的table名称和entity类中的TABLE名称是否一致
	 */
	@Test
	public void checkTableNameUniqueConstraintName() throws Exception {
		try (ScanResult scanResult = new ClassGraph().enableAllInfo().scan()) {
			List<ClassInfo> classInfos = scanResult.getClassesWithAnnotation(ContainerEntity.class.getName());
			List<Class<?>> list = new ArrayList<>();
			for (ClassInfo info : classInfos) {
				list.add(Class.forName(info.getName()));
			}
			for (Class<?> cls : list) {
				Table table = cls.getAnnotation(Table.class);
				String name = Objects.toString(FieldUtils.readStaticField(cls, "TABLE", true));
				if (!StringUtils.equals(table.name(), name)) {
					System.out.println("table name not match:" + cls);
				}
				for (UniqueConstraint u : table.uniqueConstraints()) {
					if (!StringUtils.startsWith(u.name(), table.name())) {
						System.out.println("uniqueConstraint name not match:" + cls);
					}
				}
			}
		}
	}

	/*
	 * 检查类中是否有在createTime,updateTime和sequence上的索引,这几个索引已经用约束在类上了
	 */
	@Test
	public void checkIdCreateTimeUpdateTimeSequenceIndex() throws Exception {
		try (ScanResult scanResult = new ClassGraph().enableAllInfo().scan()) {
			List<ClassInfo> classInfos = scanResult.getClassesWithAnnotation(ContainerEntity.class.getName());
			List<Class<?>> list = new ArrayList<>();
			for (ClassInfo info : classInfos) {
				list.add(Class.forName(info.getName()));
			}
			for (Class<?> cls : list) {
				Field idField = FieldUtils.getField(cls, JpaObject.id_FIELDNAME, true);
				Field createTimeField = FieldUtils.getField(cls, JpaObject.createTime_FIELDNAME, true);
				Field updateTimeField = FieldUtils.getField(cls, JpaObject.updateTime_FIELDNAME, true);
				Field sequenceField = FieldUtils.getField(cls, JpaObject.sequence_FIELDNAME, true);
				if ((null != idField.getAnnotation(Index.class)) || (null != createTimeField.getAnnotation(Index.class))
						|| (null != updateTimeField.getAnnotation(Index.class))
						|| (null != sequenceField.getAnnotation(Index.class))) {
					System.out.println(cls.getName() + " has IdCreateTimeUpdateTimeSequenceIndex");
				}
			}
		}
	}

	/*
	 * 检查entity是否有重复的字段
	 */
	@Test
	public void checkMutiField() throws Exception {
		try (ScanResult scanResult = new ClassGraph().enableAllInfo().scan()) {
			List<ClassInfo> classInfos = scanResult.getClassesWithAnnotation(ContainerEntity.class.getName());
			List<Class<?>> list = new ArrayList<>();
			for (ClassInfo info : classInfos) {
				list.add(Class.forName(info.getName()));
			}
			for (Class<?> cls : list) {
				Object o = cls.newInstance();
				XGsonBuilder.toJson(o);
			}
		}
	}

	/*
	 * 检查entity是否有重复的字段
	 */
	@Test
	public void checkEnum() throws Exception {
		try (ScanResult scanResult = new ClassGraph().enableAllInfo().scan()) {
			List<ClassInfo> classInfos = scanResult.getClassesWithAnnotation(ContainerEntity.class.getName());
			List<Class<?>> list = new ArrayList<>();
			for (ClassInfo info : classInfos) {
				list.add(Class.forName(info.getName()));
			}
			for (Class<?> cls : list) {
				List<Field> fields = FieldUtils.getFieldsListWithAnnotation(cls, FieldDescribe.class);
				for (Field field : fields) {
					if (field.getType().isEnum()) {
						Enumerated enumerated = field.getAnnotation(Enumerated.class);
						Column column = field.getAnnotation(Column.class);
						if (null == enumerated || (!Objects.equals(EnumType.STRING, enumerated.value()))
								|| (null == column) || column.length() > 200) {
							System.out.println(cls + ":" + field.getName());
						}
					}
				}
			}
		}
	}

	/*
	 * 检查entity是否有重复的字段
	 */
	@Test
	public void checkContainerEntities() throws Exception {
		try (ScanResult scanResult = new ClassGraph().enableAllInfo().scan()) {
			List<ClassInfo> classInfos = scanResult.getClassesWithAnnotation(Module.class.getName());
			List<Class<?>> list = new ArrayList<>();
			for (ClassInfo info : classInfos) {
				list.add(Class.forName(info.getName()));
			}
			for (Class<?> cls : list) {
				List<String> containerEntities = (List<String>) FieldUtils.readStaticField(cls, "containerEntities");
				for (String str : containerEntities) {
					try {
						Class clazz = Class.forName(str);
					} catch (Exception e) {
						System.out.println(cls.getName() + " error " + str);
					}
				}
			}
		}
	}
}
