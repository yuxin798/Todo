package com.todo.vo;

import com.todo.entity.Room;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomVo {
    private Long roomId;
    private String roomName;
    private String roomAvatar;

    public RoomVo(Room room) {
        this.roomId = room.getRoomId();
        this.roomName = room.getRoomName();
        this.roomAvatar = room.getRoomAvatar();
    }
}
