package com.saint.netty.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @date: 2019/9/9 19:59
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatEntityInfo {
    private long userId;
    private long toUserId;
    private String content;
}
