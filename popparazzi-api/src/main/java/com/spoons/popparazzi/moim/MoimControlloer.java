package com.spoons.popparazzi.moim;

import com.spoons.popparazzi.moim.dto.request.CreateMoimRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.spoons.popparazzi.moim.service.MoimService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/moim")
public class MoimControlloer {

    private final MoimService moimService;

    @PostMapping
    public ResponseEntity<Long> create(
            @RequestBody @Valid CreateMoimRequest request
    ) {
        Long meetingId = moimService.create(request.toCommand());
        return ResponseEntity.ok(meetingId);
    }
}
