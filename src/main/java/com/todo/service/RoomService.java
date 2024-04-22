package com.todo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.todo.dto.RoomDto;
import com.todo.entity.Room;
import com.todo.vo.RoomVo;
import com.todo.vo.UserVo;

import java.util.List;

/**
* @author 28080
* @description 针对表【room(自习室表)】的数据库操作Service
* @createDate 2024-04-22 15:37:48
*/
public interface RoomService extends IService<Room> {

    void createRoom(RoomDto roomDto);

    String generateInvitationCode(RoomDto roomDto);

    void acceptInvitation(String invitationCode);

    List<UserVo> listUsers(Long roomId);

    List<RoomVo> listRooms();

    void removeUser(Long userId, Long id);

    void userExit(Long roomId);

    void deleteRoom(Long roomId);

    void updateRoom(RoomDto roomDto);
}
