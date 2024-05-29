package com.todo.dto;

import com.todo.constraints.AvatarLink;
import com.todo.constraints.ValidName;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomDto {
    @NotNull(message = "自习室id不能为空", groups = {UpdateRoom.class})
    @Min(value = 1, message = "自习室id必须大于等于1", groups = {UpdateRoom.class})
    private Long roomId;

    private Long userId;

//    @ValidName(min=2, max=16, message = "自习室名必须为2~16个字符", groups = {CreateRoom.class, UpdateRoom.class, FindRoom.class})
    @NotBlank(message = "自习室名不能为空", groups = CreateRoom.class)
    @Length(min = 1, max = 32, message = "自习室名必须为2~16个字符", groups = {CreateRoom.class, UpdateRoom.class, FindRoom.class})
    private String roomName;

    @AvatarLink(groups = {CreateRoom.class, UpdateRoom.class})
    private String roomAvatar;

    public interface CreateRoom {}
    public interface UpdateRoom {}
    public interface FindRoom {}
}
