package com.jjangtrio.veteran.ServerApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Alias("locationdto")
public class LocationDTO {

    private Long locationId;
    private String locationName;

}