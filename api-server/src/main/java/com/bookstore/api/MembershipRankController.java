package com.bookstore.api;

import com.bookstore.bus.MembershipRankBUS;
import com.bookstore.dto.MembershipRankDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/membership-ranks")
public class MembershipRankController {
    private final MembershipRankBUS rankBUS = new MembershipRankBUS();
    private final AuthSessionStore sessionStore;

    public MembershipRankController(AuthSessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @GetMapping
    public List<MembershipRankDTO> getAllRanks(@RequestHeader("Authorization") String authorization) {
        sessionStore.requireAnyView(authorization, "MANAGE_CUSTOMER");
        return rankBUS.getAllRanks();
    }

    @GetMapping("/by-points/{point}")
    public MembershipRankDTO getRankByPoint(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("point") int point
    ) {
        sessionStore.requireAnyView(authorization, "MANAGE_CUSTOMER", "MANAGE_SELLING");
        return rankBUS.getRankByPoint(point);
    }

    @PostMapping
    public ResponseEntity<Void> addRank(
            @RequestHeader("Authorization") String authorization,
            @RequestBody MembershipRankDTO rank
    ) {
        sessionStore.requireAction(authorization, "MANAGE_CUSTOMER");
        validateRank(rank);
        if (!rankBUS.addRank(rank)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tên hạng đã tồn tại hoặc không thể tạo hạng.");
        }
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateRank(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("id") int id,
            @RequestBody MembershipRankDTO rank
    ) {
        sessionStore.requireAction(authorization, "MANAGE_CUSTOMER");
        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã hạng thành viên không hợp lệ.");
        }
        validateRank(rank);
        rank.setRankId(id);
        if (!rankBUS.updateRank(rank)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Không thể cập nhật hạng thành viên.");
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRank(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("id") int id
    ) {
        sessionStore.requireAction(authorization, "MANAGE_CUSTOMER");
        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã hạng thành viên không hợp lệ.");
        }
        String result = rankBUS.deleteRank(id);
        if (!result.contains("thành công")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, result);
        }
        return ResponseEntity.noContent().build();
    }

    private void validateRank(MembershipRankDTO rank) {
        if (rank == null || rank.getRankName() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thông tin hạng thành viên không hợp lệ.");
        }
        String rankName = rank.getRankName().trim();
        if (!rankName.matches("^(?=.*\\p{L})[\\p{L}0-9\\s.'\\u2019\\u2018\\u0060\\u02BB\\u2013-]+$")
                || rank.getMinPoint() < 0
                || !Double.isFinite(rank.getDiscountPercent())
                || rank.getDiscountPercent() < 0 || rank.getDiscountPercent() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thông tin hạng thành viên không hợp lệ.");
        }
        rank.setRankName(rankName);
    }
}
