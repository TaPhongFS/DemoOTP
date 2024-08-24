package com.example.demo.jpa.impl;

import com.example.demo.utils.BaseResultSelect;
import com.example.demo.utils.Utils;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Stream;

@Repository
@Slf4j
public class BaseRepositoryImpl {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public Boolean executeSqlDatabase(String queryString, Map<String, Object> mapParams) {
        boolean result = true;
        try {
            jdbcTemplate.update(queryString, mapParams);
        } catch (DataAccessException e) {
            log.error(e.getMessage());
            result = false;
        }
        return result;
    }

    public <T> T getFirstData(String queryString, Map<String, Object> mapParam, Class<T> className) {
        try {
            return (T) jdbcTemplate.queryForObject(queryString, mapParam, new BeanPropertyRowMapper(className));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public <T> T queryForObject(String queryString, Map<String, Object> mapParam, Class<T> className) {
        try {
            if (className.isAssignableFrom(Long.class) || className.isAssignableFrom(Integer.class) || className.isAssignableFrom(Double.class) || className.isAssignableFrom(String.class)) {
                return (T) jdbcTemplate.queryForObject(queryString, mapParam, className);
            } else {
                return (T) jdbcTemplate.queryForObject(queryString, mapParam, new BeanPropertyRowMapper(className));
            }
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public <T> List<T> getListData(String queryString, Map<String, Object> mapParam, Class<T> className) {
        try {
            if (className.isAssignableFrom(Long.class) || className.isAssignableFrom(Integer.class) || className.isAssignableFrom(Double.class) || className.isAssignableFrom(String.class)) {
                return jdbcTemplate.queryForList(queryString, mapParam, className);
            } else {
                return jdbcTemplate.query(queryString, mapParam, new BeanPropertyRowMapper(className));
            }
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }

    public <T> Stream<T> getDataForStream(String queryString, Map<String, Object> mapParam, Class<T> className) {
        return jdbcTemplate.queryForStream(queryString, mapParam, new BeanPropertyRowMapper<>(className));
    }

    public BaseResultSelect getListPagination(String queryString, Map<String, Object> mapParams, Integer startPage, Integer pageSize, Class<?> classOfT) {
        try {
            startPage = startPage == null || startPage < 0 ? 0 : startPage;
            pageSize = pageSize == null || pageSize < 0 ? 10 : pageSize;
            StringBuilder sqlPage = new StringBuilder();
            sqlPage.append(" SELECT * FROM ( ");
            sqlPage.append(queryString);
            sqlPage.append(" ) a ");
            sqlPage.append(String.format(" OFFSET %d ROWS FETCH NEXT %d ROWS ONLY ", startPage, pageSize));
            String sqlCount = "SELECT COUNT(1) FROM (" + queryString + ")";
            Long records = jdbcTemplate.queryForObject(sqlCount, mapParams, Long.class);
            List<Object> resultQuery = jdbcTemplate.query(sqlPage.toString(), mapParams, new BeanPropertyRowMapper(classOfT));
            BaseResultSelect result = new BaseResultSelect();
            if (resultQuery != null) {
                result.setListData(resultQuery);
            }
            result.setCount(records);
            return result;
        } catch (NumberFormatException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public <T> List<T> getListData(String queryString, Map<String, Object> mapParams, Integer startPage, Integer pageSize, Class<T> className) {
        try {
            startPage = startPage == null || startPage < 0 ? 0 : startPage;
            pageSize = pageSize == null || pageSize < 0 ? 10 : pageSize;
            StringBuilder sqlPage = new StringBuilder();
            sqlPage.append(" SELECT * FROM ( ");
            sqlPage.append(queryString);
            sqlPage.append(" ) a ");
            sqlPage.append(String.format(" OFFSET %d ROWS FETCH NEXT %d ROWS ONLY ", startPage, pageSize));
            return jdbcTemplate.query(sqlPage.toString(), mapParams, new BeanPropertyRowMapper(className));
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }

    public void updateBatch(List<List<Pair<String, Object>>> dataList, String sql) {
        List<Map<String, ?>> params = new ArrayList<>();
        dataList.forEach(items -> {
            Map<String, Object> paramMap = new HashMap<>();
            items.forEach(pair -> {
                paramMap.put(pair.getLeft(), pair.getRight());
            });
            params.add(paramMap);
        });
        jdbcTemplate.batchUpdate(sql, params.toArray(new Map[dataList.size()]));
    }

    public int updateBatch(Class className, List listObject, String userName) {
        try {
            if (listObject == null || listObject.isEmpty()) {
                return 0;
            }
            List<Field> fields = getAllModelFields(className);

            String tableName = getSQLTableName(className);
            String idColumnName = getIdColumnName(className);

            List<String> setList = new ArrayList<>();
            StringBuilder sql = new StringBuilder(" UPDATE ").append(tableName).append(" SET ");
            StringBuilder sqlWhere = new StringBuilder();

            for (Field field : fields) {
                field.setAccessible(true);
                String columnName = getColumnName(field);
                if (!StringUtils.isEmpty(columnName)
                        && (!StringUtils.equalsIgnoreCase(columnName, idColumnName))) {
                    if (StringUtils.equalsIgnoreCase(columnName, "LAST_UPDATED_BY")) {
                        setList.add("LAST_UPDATED_BY = '" + userName + "'");
                    } else if (StringUtils.equalsIgnoreCase(columnName, "LAST_UPDATE_DATE")) {
                        setList.add(" LAST_UPDATE_DATE = sysdate");
                    } else {
                        setList.add(columnName + "= :" + field.getName());
                    }
                } else if (StringUtils.equalsIgnoreCase(columnName, idColumnName)) {
                    sqlWhere.append(" WHERE " + idColumnName + "=:" + field.getName());
                }
            }
            String sqlUpdate = sql.append(StringUtils.join(setList, ",")).append(sqlWhere).toString();
            List<List<Object>> listPartition = Utils.partition(listObject, 999);
            int successRecord = 0;
            for (List<Object> list : listPartition) {
                Map<String, Object>[] batchOfInputs = new HashMap[list.size()];
                int count = 0;
                for (Object object : list) {
                    Map<String, Object> mapParam = new HashMap<>();
                    for (Field field : fields) {
                        field.setAccessible(true);
                        String columnName = getColumnName(field);
                        if (!StringUtils.isEmpty(columnName)) {
                            Object value = field.get(object);
                            mapParam.put(field.getName(), value);
                        }
                    }
                    batchOfInputs[count++] = mapParam;
                }
                log.info("sql {}", sqlUpdate);
                int[] result = jdbcTemplate.batchUpdate(sqlUpdate, batchOfInputs);
                for (int i = 0; i < result.length; i++) {
                    int record = result[i];
                    if (record == 1) {
                        successRecord++;
                    } else {
                        log.info("UPDATE FAIL|sqlUpdate=" + sqlUpdate + "|value={}" + batchOfInputs[i]);
                    }
                }
            }
            return successRecord;
        } catch (IllegalAccessException ex) {
            log.error("ERROR=" + ex.getMessage(), ex);
        }
        return 0;
    }

    public int insertBatch(Class className, List listObject, boolean isUsedSeq, String userName) {
        try {
            if (listObject == null || listObject.isEmpty()) {
                return 0;
            }
            List<Field> fields = getAllModelFields(className);

            String tableName = getSQLTableName(className);
            String idColumnName = getIdColumnName(className);

            StringBuilder insertTemplate;
            StringBuilder valueTemplate;
            if (isUsedSeq) {
                insertTemplate = new StringBuilder("INSERT INTO " + tableName + "(" + idColumnName + ", ");
                valueTemplate = new StringBuilder(" VALUES(" + getSequenceName(className) + ".nextval, ");
            } else {
                insertTemplate = new StringBuilder("INSERT INTO " + tableName + "(");
                valueTemplate = new StringBuilder(" VALUES(");
            }
            for (Field field : fields) {
                field.setAccessible(true);
                String columnName = getColumnName(field);
                if (!StringUtils.isEmpty(columnName)
                        && (!StringUtils.equalsIgnoreCase(columnName, idColumnName) || !isUsedSeq)) {
                    insertTemplate.append(columnName).append(",");
                    if (StringUtils.equalsIgnoreCase(columnName, "CREATED_BY")) {
                        valueTemplate.append("'" + userName + "'").append(",");
                    } else if (StringUtils.equalsIgnoreCase(columnName, "CREATE_DATE")) {
                        valueTemplate.append("sysdate").append(",");
                    } else {
                        valueTemplate.append(":").append(field.getName()).append(",");
                    }
                }
            }
            String sqlInsert = insertTemplate.toString();
            sqlInsert = sqlInsert.substring(0, sqlInsert.length() - 1) + ")";
            String values = valueTemplate.toString();
            values = values.substring(0, values.length() - 1) + ")";
            List<List<Object>> listPartition = Utils.partition(listObject, 999);
            int successRecord = 0;
            for (List<Object> list : listPartition) {
                Map<String, Object>[] batchOfInputs = new HashMap[list.size()];
                int count = 0;
                for (Object object : list) {
                    Map<String, Object> mapParam = new HashMap<>();
                    for (Field field : fields) {
                        field.setAccessible(true);
                        String columnName = getColumnName(field);
                        if (!StringUtils.isEmpty(columnName)
                                && (!StringUtils.equalsIgnoreCase(columnName, idColumnName) || !isUsedSeq)) {
                            Object value = field.get(object);
                            mapParam.put(field.getName(), value);
                        }
                    }
                    batchOfInputs[count++] = mapParam;
                }
                log.info("sql {}", sqlInsert + values);
                int[] result = jdbcTemplate.batchUpdate(sqlInsert + values, batchOfInputs);
                for (int i = 0; i < result.length; i++) {
                    int record = result[i];
                    if (record == 1) {
                        successRecord++;
                    } else {
                        log.info("INSERT FAIL|sqlInsert=" + sqlInsert + values + "|value={}" + batchOfInputs[i]);
                    }
                }
            }
            return successRecord;
        } catch (IllegalAccessException ex) {
            log.error("ERROR=" + ex.getMessage(), ex);
        }
        return 0;
    }

    private String getColumnName(Field f) {
        Column column = f.getAnnotation(Column.class);
        if (column != null) {
            return column.name();
        } else {
            return null;
        }
    }

    private String getSequenceName(Class className) {
        for (Field f : className.getDeclaredFields()) {
            SequenceGenerator seq = f.getAnnotation(SequenceGenerator.class);
            if (seq != null) {
                return seq.sequenceName();
            }
        }
        return null;
    }

    private List<Field> getAllModelFields(Class aClass) {
        List<Field> fields = new ArrayList<>();
        while (aClass != Object.class) {
            fields.addAll(Arrays.asList(aClass.getDeclaredFields()));
            aClass = aClass.getSuperclass();
        }
        return fields;
    }

    private String getSQLTableName(Class className) {
        Table table = (Table) className.getAnnotation(Table.class);
        return table.name();
    }

    private String getIdColumnName(Class className) {
        for (Field f : className.getDeclaredFields()) {
            Id id = f.getAnnotation(Id.class);
            if (id != null) {
                Column column = f.getAnnotation(Column.class);
                return column.name();
            }
        }
        return null;
    }
}

