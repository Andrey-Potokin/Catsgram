package ru.yandex.practicum.catsgram.dto.image;

import lombok.Data;

@Data
public class ImageUploadResponse {
    private long id;
    private long postId;
    private String fileName;
    private String filePath;
}