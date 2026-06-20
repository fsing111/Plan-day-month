package com.plansystem.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("attachment")
public class Attachment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long achievementId;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private LocalDateTime uploadedAt;
}
