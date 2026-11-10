package com.easynutri.dto;

import java.util.List;

public record HouseholdDto(
    Long id,
    String name,
    Long ownerUserId,
    String ownerUsername,
    int membersCount,
    List<HouseholdMemberDto> members) {}
