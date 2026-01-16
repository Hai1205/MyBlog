package com.example.rabbitcommon.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RPCResponse<T> {
    private int code;
    private String message;
    private T data;
}