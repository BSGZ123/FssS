package cn.BsKPLu.FssS.service.impl;

import cn.BsKPLu.FssS.dao.UploadedDAO;
import cn.BsKPLu.FssS.model.UploadedRecord;
import cn.BsKPLu.FssS.service.IUploadedService;
import cn.BsKPLu.FssS.util.ServiceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author pantao
 * @since 2018/2/28
 */
@Service
public class UploadedServiceImpl implements IUploadedService {

    private final UploadedDAO uploadedDAO;

    @Autowired
    public UploadedServiceImpl(UploadedDAO uploadedDAO) {this.uploadedDAO = uploadedDAO;}

    @SuppressWarnings("unchecked")
    @Override
    public List<UploadedRecord> list(String user, String file, String category, int offset) {
        return (List<UploadedRecord>) ServiceUtils.invokeFileFilter(uploadedDAO, "listUploadedBy", user, file,
                category, offset);
    }
}
