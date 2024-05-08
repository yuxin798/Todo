package com.todo.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
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
    public RoomVo createRoom(RoomDto roomDto) {
        User user = UserContextUtil.getUser();

        // 添加 room
        Room room = new Room(user.getUserId(), roomDto.getRoomName(), roomDto.getRoomAvatar());
        baseMapper.insert(room);

        // 添加关系
        userRoomMapper.insert(new UserRoom(user.getUserId(), room.getRoomId()));
        return new RoomVo(room.getRoomId(), room.getRoomName(), room.getRoomAvatar());
    }

    @Override
    public String generateInvitationCode(Long roomId) {
        User user = UserContextUtil.getUser();
        Room room = baseMapper.selectById(roomId);

        if (room == null) {
            throw new RuntimeException("自习室不存在");
        }

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

        LambdaQueryWrapper<UserRoom> wrapper = new LambdaQueryWrapper<>(UserRoom.class)
                .eq(UserRoom::getRoomId, roomId)
                .eq(UserRoom::getUserId, user.getUserId());

        if (userRoomMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("已加入自习室");
        }

        userRoomMapper.insert(new UserRoom(user.getUserId(), roomId));
    }

    @Override
    public List<UserVo> listUsers(Long roomId) {
        LambdaQueryWrapper<UserRoom> wrapper = new LambdaQueryWrapper<>(UserRoom.class)
                .select(UserRoom::getUserId)
                .eq(UserRoom::getRoomId, roomId);
        List<Long> ids = userRoomMapper.selectObjs(wrapper);
        if (ids.isEmpty()) return new ArrayList<>();

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
                .select(UserRoom::getRoomId)
                .eq(UserRoom::getUserId, user.getUserId());
        List<Long> ids = userRoomMapper.selectObjs(wrapper);

        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }

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

        if (room == null) {
            throw new RuntimeException("自习室不存在");
        }

        if (!user.getUserId().equals(room.getUserId())) {
            throw new RuntimeException("没有权限");
        }

        if (room.getUserId().equals(userId)) {
            throw new RuntimeException("创建者不能退出自习室，只能解散");
        }

        LambdaQueryWrapper<UserRoom> wrapper = new LambdaQueryWrapper<>(UserRoom.class)
                .eq(UserRoom::getRoomId, roomId)
                .eq(UserRoom::getUserId, userId);

        if (userRoomMapper.selectOne(wrapper) == null) {
            throw new RuntimeException("用户已被移除");
        }

        userRoomMapper.delete(wrapper);
    }

    @Override
    public void userExit(Long roomId) {
        User user = UserContextUtil.getUser();
        Room room = baseMapper.selectById(roomId);
        if (room == null) {
            throw new RuntimeException("自习室不存在");
        }

        if (room.getUserId().equals(user.getUserId())) {
            throw new RuntimeException("创建者不能退出自习室，只能解散");
        }

        LambdaQueryWrapper<UserRoom> wrapper;
        wrapper = new LambdaQueryWrapper<>(UserRoom.class)
                .eq(UserRoom::getUserId, user.getUserId())
                .eq(UserRoom::getRoomId, roomId);
        if (userRoomMapper.selectOne(wrapper) == null) {
            throw new RuntimeException("用户已不在自习室中");
        }

        wrapper = new LambdaQueryWrapper<>(UserRoom.class)
                .eq(UserRoom::getRoomId, roomId)
                .eq(UserRoom::getUserId, user.getUserId());
        userRoomMapper.delete(wrapper);
    }

    @Override
    public void deleteRoom(Long roomId) {
        User user = UserContextUtil.getUser();
        Room room = baseMapper.selectById(roomId);

        if (room == null) {
            throw new RuntimeException("自习室不存在");
        }

        if (!user.getUserId().equals(room.getUserId())) {
            throw new RuntimeException("没有权限");
        }

        // 删除room
        baseMapper.deleteById(roomId);

        // 删除关系
        LambdaQueryWrapper<UserRoom> wrapper = new LambdaQueryWrapper<>(UserRoom.class)
                .eq(UserRoom::getRoomId, roomId);
        userRoomMapper.delete(wrapper);
    }

    @Override
    public void updateRoom(RoomDto roomDto) {
        User user = UserContextUtil.getUser();
        Room room = baseMapper.selectById(roomDto.getRoomId());

        if (room == null) {
            throw new RuntimeException("自习室不存在");
        }

        if (!user.getUserId().equals(room.getUserId())) {
            throw new RuntimeException("没有权限");
        }

        LambdaUpdateWrapper<Room> wrapper = new LambdaUpdateWrapper<>(Room.class)
                .set(roomDto.getUserId() != null && !roomDto.getUserId().equals(room.getUserId()), Room::getUserId, roomDto.getUserId())
                .set(StringUtils.hasText(roomDto.getRoomName()), Room::getRoomName, roomDto.getRoomName())
                .set(StringUtils.hasText(roomDto.getRoomAvatar()), Room::getRoomAvatar, roomDto.getRoomAvatar())
                .eq(Room::getRoomId, roomDto.getRoomId());
        baseMapper.update(wrapper);
    }

    @Override
    public Page<Room> findRooms(RoomDto roomDto, int pageNum, int pageSize) {
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>(Room.class)
                .like(Room::getRoomName, roomDto.getRoomName());

        return baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public void requestJoin(Long roomId) {
        User user = UserContextUtil.getUser();
        Room room = baseMapper.selectById(roomId);

        if (room == null) {
            throw new RuntimeException("自习室不存在");
        }

        LambdaQueryWrapper<UserRoom> wrapper = new LambdaQueryWrapper<>(UserRoom.class)
                .eq(UserRoom::getUserId, user.getUserId())
                .eq(UserRoom::getRoomId, roomId);
        if (userRoomMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("用户已在自习室中");
        }

        Long index = redisTemplate.opsForList().indexOf(RedisConstant.ROOM_REQUEST_JOIN + roomId, user.getUserId().toString());
        if (index != null) {
            throw new RuntimeException("用户已申请");
        }

        // 存放到redis中
        redisTemplate.opsForList().leftPush(RedisConstant.ROOM_REQUEST_JOIN + roomId, user.getUserId().toString());
    }

    @Override
    public void acceptRequest(Long roomId, Long userId) {
        User user = UserContextUtil.getUser();
        Room room = baseMapper.selectById(roomId);

        if (room == null) {
            throw new RuntimeException("自习室不存在");
        }

        if (!user.getUserId().equals(room.getUserId())) {
            throw new RuntimeException("没有权限");
        }

        List<Object> requests = redisTemplate.opsForList().range(RedisConstant.ROOM_REQUEST_JOIN + roomId, 0, -1);

        if (CollectionUtils.isEmpty(requests)) {
            throw new RuntimeException("申请不存在");
        }

        List<String> ids = requests
                .stream()
                .map(o -> (String) o)
                .toList();

        if (!ids.contains(userId.toString())) {
            throw new RuntimeException("申请不存在");
        }

        LambdaQueryWrapper<UserRoom> wrapper = new LambdaQueryWrapper<>(UserRoom.class)
                .eq(UserRoom::getUserId, userId)
                .eq(UserRoom::getRoomId, roomId);
        if (userRoomMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("用户已在自习室中");
        }

        redisTemplate.opsForList().remove(RedisConstant.ROOM_REQUEST_JOIN + roomId, 0, userId.toString());
        Long size = redisTemplate.opsForList().size(RedisConstant.ROOM_REQUEST_JOIN + roomId);
        if (size != null && size == 0) {
            redisTemplate.delete(RedisConstant.ROOM_REQUEST_JOIN + roomId);
        }
    }

    @Override
    public List<UserVo> findRequests(Long roomId) {
        User user = UserContextUtil.getUser();
        Room room = baseMapper.selectById(roomId);

        if (room == null) {
            throw new RuntimeException("自习室不存在");
        }

        if (!user.getUserId().equals(room.getUserId())) {
            throw new RuntimeException("没有权限");
        }

        List<Object> objects = redisTemplate.opsForList().range(RedisConstant.ROOM_REQUEST_JOIN + roomId, 0, -1);
        if (CollectionUtils.isEmpty(objects)) {
            return new ArrayList<>();
        }

        List<String> ids = objects
                .stream()
                .map(o -> (String) o)
                .toList();
        if (ids.isEmpty()) return new ArrayList<>();

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>(User.class)
                .in(User::getUserId, ids);
        return userMapper.selectList(wrapper)
                .stream()
                .map(u -> new UserVo(u.getUserId(), u.getUserName(), u.getAvatar()))
                .toList();
    }
}




