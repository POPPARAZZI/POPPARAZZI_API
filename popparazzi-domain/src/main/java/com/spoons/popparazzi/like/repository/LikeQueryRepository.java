package com.spoons.popparazzi.like.repository;

import java.util.List;

public interface LikeQueryRepository {
    List<String> findLikedMoimCodes(String memberCode, List<String> moimCodes);
}
