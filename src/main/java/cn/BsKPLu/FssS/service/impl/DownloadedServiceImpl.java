package cn.BsKPLu.FssS.service.impl;

import cn.BsKPLu.FssS.service.IDownloadedService;
import cn.BsKPLu.FssS.dao.DownloadedDAO;
import cn.BsKPLu.FssS.model.DownloadRecord;
import cn.BsKPLu.FssS.util.ServiceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author pantao
 * @since 2018/2/1
 */
@Service
public class DownloadedServiceImpl implements IDownloadedService {

    private final DownloadedDAO downloadDAO;

    @Autowired
    public DownloadedServiceImpl(DownloadedDAO downloadDAO) {
        this.downloadDAO = downloadDAO;
    }

    @Override
    public void insertDownload(int userId, long fileId) {
        downloadDAO.insertDownload(userId, fileId);
    }

    @Override
    public void removeByFileId(long fileId) {
        downloadDAO.removeByFileId(fileId);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DownloadRecord> list(String user, String file, String category, int offset) {
        return (List<DownloadRecord>) ServiceUtils.invokeFileFilter(downloadDAO, "listDownloadedBy", user, file,
                category, offset);
    }
}
