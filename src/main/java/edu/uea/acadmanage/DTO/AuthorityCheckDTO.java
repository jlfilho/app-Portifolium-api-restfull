package edu.uea.acadmanage.DTO;

import java.util.List;

public record AuthorityCheckDTO(
    String username,
    List<String> authorities
) {}

