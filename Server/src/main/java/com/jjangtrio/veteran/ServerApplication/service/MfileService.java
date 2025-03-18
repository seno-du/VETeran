package com.jjangtrio.veteran.ServerApplication.service;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jjangtrio.veteran.ServerApplication.dao.MfileDAO;
import com.jjangtrio.veteran.ServerApplication.dto.MfileDTO;
import com.jjangtrio.veteran.ServerApplication.dto.PageDTO;

@Service
public class MfileService {

    @Autowired
    private MfileDAO mfileDAO;

    // mfile 단일조회
    public MfileDTO selectMfile(Long mfileNum) {
        MfileDTO mfile = mfileDAO.selectMfile(mfileNum);
        return mfile;
    }

    // mfile 전체 조회
    public List<MfileDTO> mfileList(PageDTO pageDTO) {

        return mfileDAO.mfileList(pageDTO);
    }

    public long totalCount() {
        return mfileDAO.totalCount();
    }

    //
    public void increaseDownloadCount(Long mfileNum) {
        mfileDAO.increaseDownloadCount(mfileNum);
    }

    // mfile 추가
    public void insertMfile(MfileDTO mfileDTO) {
        mfileDAO.insertMfile(mfileDTO);
    }

    // mfile 수정
    public void updateMfile(MfileDTO mfileDTO) {
        mfileDAO.updateMfile(mfileDTO);
    }

    // 파일 검색 (날짜, 카테고리, 담당자 기준)
    public List<MfileDTO> searchMfiles(Date mfileDate, Integer mfileCategory, String mfileUploader) {
        return mfileDAO.searchMfiles(mfileDate, mfileCategory, mfileUploader);
    }

    // mfile 활성화/비활성화
    public void statusMfile(MfileDTO mfileDTO) {
        mfileDAO.statusMfile(mfileDTO);
    }
}
