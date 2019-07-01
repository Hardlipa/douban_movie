package org.humingk.movie.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.humingk.movie.entity.ResourcelistMoviefrom;

public interface ResourcelistMoviefromMapper {
    int deleteByPrimaryKey(@Param("resourcelistId") Integer resourcelistId, @Param("moviefromId") Integer moviefromId);

    int insert(ResourcelistMoviefrom record);

    List<ResourcelistMoviefrom> selectAll();
}