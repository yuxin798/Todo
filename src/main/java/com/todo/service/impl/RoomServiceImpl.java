package com.todo.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.todo.constant.RedisConstant;
import com.todo.dto.RoomDto;
import com.todo.entity.Room;
import com.todo.entity.User;
import com.todo.entity.UserRoom;
import com.todo.mapper.RoomMapper;
import com.todo.mapper.UserMapper;
import com.todo.mapper.UserRoomMapper;
import com.todo.service.RoomService;
import com.todo.util.UserContextUtil;
import com.todo.vo.RoomVo;
import com.todo.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
* @author 28080
* @description 针对表【room(自习室表)】的数据库操作Service实现
* @createDate 2024-04-22 15:37:48
*/
@Service
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room>
    implements RoomService {

    @Autowired
    private UserRoomMapper userRoomMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void createRoom(RoomDto roomDto) {
        User user = UserContextUtil.getUser();

        // 添加 room
        Room room = new Room(user.getUserId(), roomDto.getRoomName(), roomDto.getRoomAvatar());
        baseMapper.insert(room);

        // 添加关系
        userRoomMapper.insert(new UserRoom(user.getUserId(), room.getRoomId()));
    }

    @Override
    public String generateInvitationCode(Long roomId) {
        User user = UserContextUtil.getUser();
        Room room = baseMapper.selectById(roomId);

        // roomDto的信息用户是可以篡改的
        if (!user.getUserId().equals(room.getUserId())) {
            throw new RuntimeException("没有权限生成邀请码");
        }

        // 检查是否已经有邀请码了
        String code = (String) redisTemplate.opsForValue().get(RedisConstant.ROOM_INVITATION_ID + roomId);

        if (!StringUtils.hasText(code)) {
            // 没有邀请码
            code = "ToDo@" + RandomUtil.randomString(11);
            redisTemplate.opsForValue().set(RedisConstant.ROOM_INVITATION_CODE + code, roomId, 7, TimeUnit.DAYS);
            redisTemplate.opsForValue().set(RedisConstant.ROOM_INVITATION_ID + roomId, code, 7, TimeUnit.DAYS);
        }

        return code;
    }

    @Override
    public void acceptInvitation(String invitationCode) {
        User user = UserContextUtil.getUser();
        Long roomId = (Long) redisTemplate.opsForValue().get(RedisConstant.ROOM_INVITATION_CODE + invitationCode);
        if(roomId == null){
            throw new RuntimeException("邀请码不存在");
        }
        userRoomMapper.insert(new UserRoom(user.getUserId(), roomId));
    }

    @Override
    public List<UserVo> listUsers(Long roomId) {
        LambdaQueryWrapper<UserRoom> wrapper = new LambdaQueryWrapper<>(UserRoom.class)
                .eq(UserRoom::getRoomId, roomId);
        List<Long> ids = userRoomMapper.selectList(wrapper)
                .stream()
                .map(UserRoom::getUserId)
                .toList();

        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>(User.class)
                .in(User::getUserId, ids);
        return userMapper.selectList(userWrapper)
                .stream()
                .map(user -> new UserVo(user.getUserId(), user.getUserName(), user.getAvatar()))
                .toList();
    }

    @Override
    public List<RoomVo> listRooms() {
        User user = UserContextUtil.getUser();

        LambdaQueryWrapper<UserRoom> wrapper = new LambdaQueryWrapper<>(UserRoom.class)
                .eq(UserRoom::getUserId, user.getUserId());
        List<Long> ids = userRoomMapper.selectList(wrapper)
                .stream()
                .map(UserRoom::getRoomId)
                .toList();

        LambdaQueryWrapper<Room> roomWrapper = new LambdaQueryWrapper<>(Room.class)
                .in(Room::getRoomId, ids);
        return baseMapper.selectList(roomWrapper)
                .stream()
                .map(room -> new RoomVo(room.getRoomId(), room.getRoomName(), room.getRoomAvatar()))
                .collect(Collectors.toList());
    }

    @Override
    public void removeUser(Long roomId, Long userId) {
        User user = UserContextUtil.getUser();
        Room room = baseMapper.selectById(roomId);

        if (!user.getUserId().equals(room.getUserId())) {
            throw new RuntimeException("没有权限");
        }

        LambdaQueryWrapper<UserRoom> wrapper = new LambdaQueryWrapper<>(UserRoom.class)
                .eq(UserRoom::getRoomId, roomId)
                .eq(UserRoom::getUserId, userId);
        userRoomMapper.delete(wrapper);
    }

    @Override
    public void userExit(Long roomId) {
        User user = UserContextUtil.getUser();
        LambdaQueryWrapper<UserRoom> wrapper = new LambdaQueryWrapper<>(UserRoom.class)
                .eq(UserRoom::getRoomId, roomId)
                .eq(UserRoom::getUserId, user.getUserId());
        userRoomMapper.delete(wrapper);
    }

    @Override
    public void deleteRoom(Long roomId) {
        User user = UserContextUtil.getUser();
        Room room = baseMapper.selectById(roomId);

        if (!user.getUserId().equals(room.getUserId())) {
            throw new RuntimeException("没有权限");
        }

        baseMapper.deleteById(roomId);
    }

    @Override
    public void updateRoom(RoomDto roomDto) {
        User user = UserContextUtil.getUser();
        Room room = baseMapper.selectById(roomDto.getRoomId());

        if (!user.getUserId().equals(room.getUserId())) {
            throw new RuntimeException("没有权限");
        }

        // TODO: 2024/4/22  
        // LambdaUpdateWrapper<Room> wrapper = new LambdaUpdateWrapper<>(Room.class)
        //         .eq(Room::getRoomId, roomDto.getRoomId())
        //         .eq();
        // baseMapper.update()
    }
}




