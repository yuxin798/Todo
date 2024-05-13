package com.todo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.todo.entity.UserRoom;
import org.apache.ibatis.annotations.Param;

/**
* @author 28080
* @description 针对表【user_room】的数据库操作Mapper
* @createDate 2024-04-17 17:09:33
* @Entity com.todo.entity.UserRoom
*/
public interface UserRoomMapper extends BaseMapper<UserRoom> {

    UserRoom selectBeforeInRoom(@Param("roomId") Long roomId, @Param("userId") Long userId);

    void updateDeleted(@Param("roomId") Long roomId, @Param("userId") Long userId);
}




