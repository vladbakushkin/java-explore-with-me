package ru.practicum.ewm.mainservice.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.mainservice.participationrequest.model.RequestStatus;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateRequest {
    // Идентификаторы запросов на участие в событии текущего пользователя
    private List<Long> requestIds;

    // Новый статус запроса на участие в событии текущего пользователя
    private RequestStatus status;
}
