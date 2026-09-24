package com.bookstore.bus;

import com.bookstore.dao.MembershipRankDAO;
import com.bookstore.dto.MembershipRankDTO;

import java.util.List;

public class MembershipRankBUS {
    private final MembershipRankDAO rankDAO = new MembershipRankDAO();

    public MembershipRankDTO getRankByPoint(int point) {
        MembershipRankDTO rank = rankDAO.getRankByPoint(point);
        if (rank == null) {
            return new MembershipRankDTO(1, "Thành viên", 0, 0.0);
        }
        return rank;
    }

    public List<MembershipRankDTO> getAllRanks() {
        return rankDAO.getAllRanks();
    }

    public boolean addRank(MembershipRankDTO rank) {
        if (rankDAO.checkRankNameExists(rank.getRankName(), 0)) {
            return false;
        }

        return rankDAO.addRank(rank);
    }

    public boolean updateRank(MembershipRankDTO rank) {
        if (rankDAO.checkRankNameExists(rank.getRankName(), rank.getRankId())) {
            return false;
        }

        return rankDAO.updateRank(rank);
    }

    public String deleteRank(int rankId) {
        if (rankId == 1) {
            return "Không thể xóa hạng Thành viên mặc định của hệ thống!";
        }

        if (rankDAO.isRankInUse(rankId)) {
            return "Hạng này đang được áp dụng cho khách hàng. Vui lòng chuyển hạng cho khách trước khi xóa!";
        }

        if (rankDAO.deleteRank(rankId)) {
            return "Xóa hạng thành viên thành công!";
        }

        return "Xóa thất bại, đã có lỗi xảy ra từ cơ sở dữ liệu!";
    }
}