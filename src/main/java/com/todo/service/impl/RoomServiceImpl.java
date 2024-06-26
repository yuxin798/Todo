package com.todo.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.todo.constant.RedisConstant;
import com.todo.dto.RoomDto;
import com.todo.entity.*;
import com.todo.mapper.*;
import com.todo.service.RoomService;
import com.todo.util.PageUtil;
import com.todo.util.UserContextUtil;
import com.todo.vo.RoomVo;
import com.todo.vo.UserVo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
* @author 28080
* @description 针对表【room(自习室表)】的数据库操作Service实现
* @createDate 2024-04-22 15:37:48
*/
@Service
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room>
    implements RoomService {

    private final UserRoomMapper userRoomMapper;
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final TomatoClockMapper tomatoClockMapper;

    public RoomServiceImpl(UserRoomMapper userRoomMapper, UserMapper userMapper, RedisTemplate<String, Object> redisTemplate, TomatoClockMapper tomatoClockMapper) {
        this.userRoomMapper = userRoomMapper;
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
        this.tomatoClockMapper = tomatoClockMapper;
    }

    @Override
    public RoomVo createRoom(RoomDto roomDto) {
        Long userId = UserContextUtil.getUserId();

        UserRoom userRoom = userRoomMapper.selectOne(new LambdaQueryWrapper<>(UserRoom.class)
                .eq(UserRoom::getUserId, userId));

        if (userRoom != null){
            throw new RuntimeException("您已经加入了某个自习室，不能再加入其他自习室");
        }

        // 添加 room
        Room room = new Room(userId, roomDto.getRoomName(), roomDto.getRoomAvatar());
        baseMapper.insert(room);

        // 添加关系
        userRoomMapper.insert(new UserRoom(userId, room.getRoomId()));
        return new RoomVo(room.getRoomId(), room.getRoomName(), room.getRoomAvatar());
    }

    @Override
    public String generateInvitationCode(Long roomId) {
        User user = UserContextUtil.getUser();
        Room room = baseMapper.selectById(roomId);

        if (room == null) {
            throw new RuntimeException("自习室不存在");
        }

        UserRoom userRoom = userRoomMapper.selectOne(new LambdaQueryWrapper<>(UserRoom.class)
                .eq(UserRoom::getUserId, user.getUserId())
                .eq(UserRoom::getRoomId, roomId));

        // roomDto的信息用户是可以篡改的
        if (userRoom == null) {
            throw new RuntimeException("没有权限生成邀请码");
        }

        // 检查是否已经有邀请码了
        String code = (String) redisTemplate.opsForValue().get(RedisConstant.ROOM_INVITATION_ID + roomId);

        if (!StringUtils.hasText(code)) {
            // 没有邀请码
            do {
                code = RandomUtil.randomString("0123456789", 6);
            } while (redisTemplate.opsForValue().get(RedisConstant.ROOM_INVITATION_CODE + code) != null);
            redisTemplate.opsForValue().set(RedisConstant.ROOM_INVITATION_CODE + code, roomId, 7, TimeUnit.DAYS);
            redisTemplate.opsForValue().set(RedisConstant.ROOM_INVITATION_ID + roomId, code, 7, TimeUnit.DAYS);
        }

        return code;
    }

    @Override
    public void acceptInvitation(String invitationCode) {
        Long userId = UserContextUtil.getUser().getUserId();

        UserRoom userRoom = userRoomMapper.selectOne(new LambdaQueryWrapper<>(UserRoom.class)
                .eq(UserRoom::getUserId, userId));

        if (userRoom != null){
            throw new RuntimeException("您已经加入了某个自习室，不能再加入其他自习室");
        }

        Long roomId = (Long) redisTemplate.opsForValue().get(RedisConstant.ROOM_INVITATION_CODE + invitationCode);
        if(roomId == null){
            throw new RuntimeException("邀请码不存在");
        }
        userRoomMapper.insert(new UserRoom(userId, roomId));
        this.list().forEach(r -> redisTemplate.opsForList().remove(RedisConstant.ROOM_REQUEST_JOIN + r.getRoomId(), 0, userId.toString()));
    }

    @Override
    public List<UserVo> listUsers(Long roomId) {
        LocalDate date = LocalDate.ofInstant(Instant.now(), ZoneId.of("UTC+8"));
        Date start = Date.from(Instant.ofEpochSecond(date.toEpochSecond(LocalTime.MIN, ZoneOffset.of("+8"))));
        Date end = Date.from(Instant.ofEpochSecond(date.toEpochSecond(LocalTime.MAX, ZoneOffset.of("+8"))));

        LambdaQueryWrapper<UserRoom> wrapper = new LambdaQueryWrapper<>(UserRoom.class)
                .select(UserRoom::getUserId)
                .eq(UserRoom::getRoomId, roomId);
        List<Long> ids = userRoomMapper.selectObjs(wrapper);
        if (ids.isEmpty()) return new ArrayList<>();

        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>(User.class)
                .in(User::getUserId, ids);
        return userMapper.selectList(userWrapper)
                .stream()
                .map(UserVo::new)
                .peek(u -> {
                    AtomicLong tomatoDuration = new AtomicLong(0);
                    tomatoClockMapper.selectList(
                            new LambdaQueryWrapper<>(TomatoClock.class)
                                    .eq(TomatoClock::getUserId, u.getUserId())
                                    .eq(TomatoClock::getClockStatus, TomatoClock.Status.COMPLETED.getCode())
                                    .ge(TomatoClock::getStartedAt, start)
                                    .le(TomatoClock::getCompletedAt, end)
                            )
                            .forEach(t -> tomatoDuration.addAndGet(t.getClockDuration()));
                    u.setTomatoDuration(tomatoDuration.get());
                })
                .sorted(Comparator.comparing(UserVo::getTomatoDuration).reversed())
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
        Room room = baseMapper.selectById(roomId);

        if (room == null) {
            throw new RuntimeException("自习室不存在");
        }

        if (!UserContextUtil.getUserId().equals(room.getUserId())) {
            throw new RuntimeException("没有权限");
        }

        // 删除room
        baseMapper.deleteById(roomId);

        // 删除关系
        LambdaQueryWrapper<UserRoom> wrapper = new LambdaQueryWrapper<>(UserRoom.class)
                .eq(UserRoom::getRoomId, roomId);
        userRoomMapper.delete(wrapper);

        //删除redis 自习室相关数据
        Object o = redisTemplate.opsForValue().get(RedisConstant.ROOM_INVITATION_ID + roomId);
        redisTemplate.opsForValue().getAndDelete(RedisConstant.ROOM_INVITATION_CODE + o);
        redisTemplate.opsForValue().getAndDelete(RedisConstant.ROOM_INVITATION_CODE + roomId);
        redisTemplate.opsForList().rightPop(RedisConstant.ROOM_REQUEST_JOIN + roomId);
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
    public Page<RoomVo> findRooms(RoomDto roomDto, int pageNum, int pageSize) {
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>(Room.class)
                .like(StringUtils.hasText(roomDto.getRoomName()), Room::getRoomName, roomDto.getRoomName());

        Page<Room> roomPage = baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        List<RoomVo> list = roomPage
                .getRecords()
                .stream()
                .map(RoomVo::new)
                .toList();

        return PageUtil.of(roomPage, list);
    }

    @Override
    public void requestJoin(Long roomId) {
        User user = UserContextUtil.getUser();

        LambdaQueryWrapper<UserRoom> wrapper = new LambdaQueryWrapper<>(UserRoom.class)
                .eq(UserRoom::getUserId, user.getUserId());
        UserRoom userRoom = userRoomMapper.selectOne(wrapper);
        if (userRoom != null && Objects.equals(userRoom.getRoomId(), roomId)) {
            throw new RuntimeException("用户已在该自习室中");
        }else if (userRoom != null && !Objects.equals(userRoom.getRoomId(), roomId)){
            throw new RuntimeException("您已经加入了某个自习室，不能再加入其他自习室");
        }

        Room room = baseMapper.selectById(roomId);
        if (room == null) {
            throw new RuntimeException("自习室不存在");
        }

        Long index = redisTemplate.opsForList().indexOf(RedisConstant.ROOM_REQUEST_JOIN + roomId, user.getUserId().toString());
        if (index != null) {
            throw new RuntimeException("用户已申请");
        }

        // 存放到redis中
        redisTemplate.opsForList().leftPush(RedisConstant.ROOM_REQUEST_JOIN + roomId, user.getUserId().toString());
    }

    @Override
    public void handleRequest(Long roomId, Long userId, RoomRequestType roomRequestType) {
        if (roomRequestType == RoomRequestType.ACCEPT){
            UserRoom userRoom = userRoomMapper.selectOne(
                    new LambdaQueryWrapper<>(UserRoom.class)
                            .eq(UserRoom::getUserId, userId)
            );
            if (userRoom != null){
                throw new RuntimeException("该用户已经加入某个自习室");
            }
        }

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

        if (roomRequestType == RoomRequestType.ACCEPT){
            userRoomMapper.insert(new UserRoom(userId, roomId));
            this.list().forEach(r -> redisTemplate.opsForList().remove(RedisConstant.ROOM_REQUEST_JOIN + r.getRoomId(), 0, userId.toString()));
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
                .map(UserVo::new)
                .toList();
    }

    @Override
    public RoomVo getRoomInfo() {
        User user = UserContextUtil.getUser();

        LambdaQueryWrapper<UserRoom> wrapper = new LambdaQueryWrapper<>(UserRoom.class)
                .select(UserRoom::getRoomId)
                .eq(UserRoom::getUserId, user.getUserId());
        UserRoom userRoom = userRoomMapper.selectOne(wrapper);
        if (userRoom == null) {
            throw new RuntimeException("用户未加入自习室");
        }

        Room room = this.getOne(new LambdaQueryWrapper<>(Room.class)
                .eq(Room::getRoomId, userRoom.getRoomId()));
        return new RoomVo(room);
    }

    @Override
    public List<RoomVo> findRequests() {
        Long userId = UserContextUtil.getUserId();
        return this.list()
                .stream()
                .filter(room -> redisTemplate.opsForList().indexOf(RedisConstant.ROOM_REQUEST_JOIN + room.getRoomId(), userId.toString()) != null)
                .map(RoomVo::new)
                .collect(Collectors.toList());
    }

    public enum RoomRequestType {
        ACCEPT,
        REJECT
    }
}




