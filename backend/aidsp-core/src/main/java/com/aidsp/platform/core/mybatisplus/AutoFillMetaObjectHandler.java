package com.aidsp.platform.core.mybatisplus;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 通用字段自动填充。
 * <p>依据 docs/database.md §4.2：
 * <ul>
 *     <li>{@code createdAt}：仅 INSERT 填充</li>
 *     <li>{@code updatedAt}：INSERT + UPDATE 填充</li>
 * </ul>
 * 仅当实体上对应字段标注 {@code @TableField(fill = INSERT/INSERT_UPDATE)} 时才生效。
 */
@Slf4j
@Component
public class AutoFillMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
        strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
        log.debug("[AutoFillMetaObjectHandler] insertFill {}", tableName(metaObject));
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        log.debug("[AutoFillMetaObjectHandler] updateFill {}", tableName(metaObject));
    }

    private String tableName(MetaObject metaObject) {
        if (metaObject == null) {
            return "<null>";
        }
        TableInfo info = TableInfoHelper.getTableInfo(metaObject.getOriginalObject().getClass());
        return info == null ? metaObject.getOriginalObject().getClass().getSimpleName() : info.getTableName();
    }
}
