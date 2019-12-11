package cn.BsKPLu.FssS.dao.sqlprovider;

import cn.BsKPLu.FssS.modules.constant.ConfigConsts;
import cn.BsKPLu.FssS.FssSApplication;
import com.zhazhapan.modules.constant.ValueConsts;
import com.zhazhapan.util.Checker;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.stereotype.Component;

/**
 * @author pantao
 * @since 2018/1/19
 */
@Component
public class FileSqlProvider {

    public String updateAuthById() {
        return CommonSqlProvider.updateAuthById("file");
    }

    /**
     * 生成一条文件基本信息的查询语句
     *
     * @param userId 用户编号
     * @param fileId 文件编号
     *
     * @return SQL语句
     */
    public String getBasicBy(@Param("userId") int userId, @Param("fileId") long fileId, @Param("fileName") String
            fileName, @Param("categoryId") int categoryId, @Param("offset") int offset) {
        String sql = new SQL() {{
            SELECT("f.id,u.username,f.local_url,c.name category_name,f.visit_url,f.download_times," + "f" + "" + "" +
                    ".create_time");
            FROM("file f");
            JOIN("user u on f.user_id=u.id");
            JOIN("category c on f.category_id=c.id");
            if (userId > 0) {
                WHERE("f.user_id=#{userId}");
            }
            if (fileId > 0) {
                WHERE("f.id=#{fileId}");
            } else if (Checker.isNotEmpty(fileName)) {
                WHERE("f.local_url like '%" + fileName + "%'");
            }
            if (categoryId > 0) {
                WHERE("c.id=#{categoryId}");
            }
            ORDER_BY("f." + FssSApplication.settings.getStringUseEval(ConfigConsts.FILE_ORDER_BY_OF_SETTING));
        }}.toString();
        int size = FssSApplication.settings.getIntegerUseEval(ConfigConsts.FILE_PAGE_SIZE_OF_SETTING);
        return sql + " limit " + (offset * size) + "," + size;
    }

    private String getSqlEnds(int offset, String orderBy, String search) {
        int size = FssSApplication.settings.getIntegerUseEval(ConfigConsts.FILE_PAGE_SIZE_OF_SETTING);
        return getSearch(search) + " order by " + (Checker.isEmpty(orderBy) ? FssSApplication.settings
                .getStringUseEval(ConfigConsts.FILE_ORDER_BY_OF_SETTING) : orderBy) + " limit " + offset * size
                + "," + size;
    }

    public String getAll(@Param("offset") int offset, @Param("categoryId") int categoryId, @Param("orderBy") String
            orderBy, @Param("search") String search) {
        return getBaseSql(ValueConsts.FALSE) + " where f.is_visible=1" + (categoryId < 1 ? "" : "  and " +
                "category_id=#{categoryId}") + " and ((select a.is_visible from auth a where a.file_id=f.id and a" +
                ".user_id=#{userId}) is null or (a.user_id=#{userId} and a.is_visible=1))" + getSqlEnds(offset,
                orderBy, search);
    }

    public String getUserUploaded(@Param("offset") int offset, @Param("search") String search) {
        return getBaseSql(ValueConsts.FALSE) + " where f.is_visible=1 and (f.user_id=#{userId} or a.is_updatable=1 or" +
                " a.is_deletable=1)" + getSqlEnds(offset,
                ValueConsts.EMPTY_STRING, search);
    }

    public String getUserDownloaded(@Param("offset") int offset, @Param("search") String search) {
        return getBaseSql(ValueConsts.TRUE) + " where d.user_id=#{userId}" + getSqlEnds(offset, ValueConsts
                .EMPTY_STRING, search);
    }

    private String getSearch(String search) {
        if (Checker.isEmpty(search)) {
            return ValueConsts.EMPTY_STRING;
        } else {
            search = "'%" + search + "%'";
            return " and (f.name like " + search + " or f.visit_url like " + search + " or f.description like " +
                    search + " or f.tag like " + search + ")";
        }
    }

    public  String tagSearch(@Param("tag") String tag){

        String tag1="'%"+tag+"%'";
        return getBaseSql(ValueConsts.FALSE)+" where f.tag like"+tag1;
    }

    private String getBaseSql(boolean isDownloaded) {
        return new SQL() {{
            SELECT("distinct f.id,f.user_id,u.username,u.avatar,f.name file_name,f.size,f.create_time,c.name " +
                    "category_name,f"
                    + ".description,f.tag,f.check_times,f.download_times,f.visit_url,f.is_uploadable,f.is_deletable,"
                    + "f.is_updatable,f.is_downloadable,f.is_visible");
            if (isDownloaded) {
                SELECT("d.create_time download_time");
            }
            FROM("file f");
            JOIN("user u on u.id=f.user_id");
            JOIN("category c on c.id=f.category_id");
            if (isDownloaded) {
                JOIN("download d on d.file_id=f.id");
            } else {
                JOIN("auth a on a.file_id=f.id");
            }
        }}.toString();
    }

}
