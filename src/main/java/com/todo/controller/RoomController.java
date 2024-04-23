package com.todo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.todo.dto.RoomDto;
import com.todo.entity.Room;
import com.todo.service.RoomService;
import com.todo.vo.Result;
import com.todo.vo.RoomVo;
import com.todo.vo.UserVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

/**
 * 自习室 Controller
 *
 */
@Tag(name = "自习室API")
@RestController
@RequestMapping("/room")
public class RoomController {
    @Autowired
    private RoomService roomService;

    /**
     * 创建一个自习室
     * @return 自习室的信息
     */
    @Operation(summary = "创建自习室")
    @PostMapping("/create")
    public Result<?> createRoom(@RequestBody RoomDto roomDto) {
        roomService.createRoom(roomDto);
        return Result.success();
    }

    /**
     * 生成邀请码
     * @return 邀请码
     */
    @Operation(summary = "生成邀请码")
    @GetMapping("/generateInvitationCode")
    public Result<String> generateInvitationCode(Long roomId) {
        String code = roomService.generateInvitationCode(roomId);
        return Result.success(code);
    }

    /**
     * 接受邀请
     * @return 成功或失败
     */
    @Operation(summary = "接受邀请")
    @PostMapping("/acceptInvitation")
    public Result<String> acceptInvitation(String invitationCode) {
        roomService.acceptInvitation(invitationCode);
        return Result.success();
    }

    /**
     * 查询自习室中的所有用户
     * @return 用户列表
     */
    @Operation(summary = "查询自习室中的所有用户")
    @GetMapping("/user/list")
    public Result<List<UserVo>> listUsers(Long roomId) {
        List<UserVo> users = roomService.listUsers(roomId);
        return Result.success(users);
    }

    /**
     * 查询用户加入的所有自习室
     * @return 自习室列表
     */
    @Operation(summary = "查询用户加入的所有自习室")
    @GetMapping("/list")
    public Result<List<RoomVo>> listUsers() {
        List<RoomVo> rooms = roomService.listRooms();
        return Result.success(rooms);
    }

    /**
     * 管理员移除某位用户
     */
    @Operation(summary = "管理员移除用户")
    @DeleteMapping("/user/remove")
    public Result<?> removeUser(Long roomId, Long userId) {
        roomService.removeUser(roomId, userId);
        return Result.success();
    }

    /**
     * 查询用户加入的所有自习室
     * @return 自习室列表
     */
    @Operation(summary = "用户退出自习室")
    @DeleteMapping("/user/exit")
    public Result<?> userExit(Long roomId) {
        roomService.userExit(roomId);
        return Result.success();
    }

    /**
     * 删除自习室
     */
    @Operation(summary = "删除自习室")
    @DeleteMapping("/")
    public Result<?> deleteRoom(Long roomId) {
        roomService.deleteRoom(roomId);
        return Result.success();
    }

    /**
     * 修改自习室
     */
    @Operation(summary = "修改自习室")
    @PutMapping("/update")
    public Result<?> updateRoom(@RequestBody RoomDto roomDto) {
        roomService.updateRoom(roomDto);
        return Result.success();
    }

    /**
     * 分页查询自习室
     */
    @Operation(summary = "查询自习室")
    @GetMapping("/")
    public Result<Page<Room>> findRooms(
            RoomDto roomDto,
            @RequestParam(required = false, defaultValue = "0") int pageNum,
            @RequestParam(required = false, defaultValue = "20") int pageSize) {
        Page<Room> page = roomService.findRooms(roomDto, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 用户申请加入自习室
     */
    @Operation(summary = "用户申请加入自习室")
    @PostMapping("/user/requestJoin")
    public Result<?> requestJoin(Long roomId) {
        roomService.requestJoin(roomId);
        return Result.success();
    }

    /**
     * 管理员同意用户加入自习室
     */
    @Operation(summary = "管理员同意用户加入自习室")
    @PostMapping("/manager/acceptRequest")
    public Result<?> acceptRequest(Long roomId, Long userId) {
        roomService.acceptRequest(roomId, userId);
        return Result.success();
    }

    /**
     * 管理员查询所有的加入自习室的请求
     */
    @Operation(summary = "管理员查询所有的加入自习室的请求")
    @GetMapping("/manager/requests")
    public Result<List<UserVo>> findRequests(Long roomId) {
        List<UserVo> users = roomService.findRequests(roomId);
        return Result.success(users);
    }
}
