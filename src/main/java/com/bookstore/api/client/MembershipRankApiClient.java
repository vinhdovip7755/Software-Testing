package com.bookstore.api.client;

import com.bookstore.dto.MembershipRankDTO;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class MembershipRankApiClient {
    public MembershipRankDTO getRankByPoint(int point) {
        return ApiClient.get("/api/membership-ranks/by-points/" + point, new TypeReference<>() {
        });
    }

    public List<MembershipRankDTO> getAllRanks() {
        return ApiClient.get("/api/membership-ranks", new TypeReference<>() {
        });
    }

    public void addRank(MembershipRankDTO rank) {
        ApiClient.postNoContent("/api/membership-ranks", rank);
    }

    public void updateRank(MembershipRankDTO rank) {
        ApiClient.putNoContent("/api/membership-ranks/" + rank.getRankId(), rank);
    }

    public void deleteRank(int rankId) {
        ApiClient.deleteNoContent("/api/membership-ranks/" + rankId);
    }
}
