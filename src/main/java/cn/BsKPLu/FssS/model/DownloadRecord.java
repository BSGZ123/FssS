package cn.BsKPLu.FssS.model;

import cn.BsKPLu.FssS.util.BeanUtils;

import java.sql.Timestamp;

/**
 * @author pantao
 * @since 2018/1/19
 */
public class DownloadRecord {

    private long id;

    private int userId;

    private long fileId;

    private String username;

    private String email;

    private String fileName;

    private String categoryName;

    private String visitUrl;

    private Timestamp createTime;

    public DownloadRecord(long id, int userId, long fileId, String username, String email, String fileName, String
            categoryName, String visitUrl, Timestamp createTime) {
        this.id = id;
        this.userId = userId;
        this.fileId = fileId;
        this.username = username;
        this.email = email;
        this.fileName = fileName;
        this.categoryName = categoryName;
        this.visitUrl = visitUrl;
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return BeanUtils.toPrettyJson(this);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public long getFileId() {
        return fileId;
    }

    public void setFileId(long fileId) {
        this.fileId = fileId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getVisitUrl() {
        return visitUrl;
    }

    public void setVisitUrl(String visitUrl) {
        this.visitUrl = visitUrl;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }
}
